package org.ishafoundation.dwaraapi.process.thread.task.mam;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.commandline.remote.sch.CatdvSshSessionHelper;
import org.ishafoundation.dwaraapi.commandline.remote.sch.RemoteCommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.remote.scp.SecuredCopier;
import org.ishafoundation.dwaraapi.configuration.CatDVConfiguration;
import org.ishafoundation.dwaraapi.model.LogicalFile;
import org.ishafoundation.dwaraapi.model.TaskResponse;
import org.ishafoundation.dwaraapi.process.factory.TaskFactory;
import org.ishafoundation.dwaraapi.process.mam.authn.Authenticator;
import org.ishafoundation.dwaraapi.process.mam.ingest.CatalogChecker;
import org.ishafoundation.dwaraapi.process.mam.ingest.CatalogCreator;
import org.ishafoundation.dwaraapi.process.mam.ingest.ClipInserter;
import org.ishafoundation.dwaraapi.process.mam.ingest.ClipUpdater;
import org.ishafoundation.dwaraapi.process.mam.ingest.ThumbnailInserter;
import org.ishafoundation.dwaraapi.process.thread.task.ITaskExecutor;
import org.ishafoundation.dwaraapi.utils.DateAndTimeUtil;
import org.ishafoundation.dwaraapi.utils.JsonPathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.Session;

@Component
public class MamUpdateTaskExecutor implements ITaskExecutor {
    static {
    	TaskFactory.register("mam_update", MamUpdateTaskExecutor.class);
    }
    
    private static final Logger logger = LoggerFactory.getLogger(MamUpdateTaskExecutor.class);

	@Autowired
	private CatDVConfiguration catDVConfiguration;
	
	@Autowired
	private Authenticator authenticator;
	
	@Autowired
    private CatdvSshSessionHelper catdvSshSessionHelper;
	
	@Autowired
	private CatalogChecker catalogChecker;

	@Autowired
	private CatalogCreator cc;
	
	@Autowired
	private ClipInserter ci;
	
	@Autowired
	private ThumbnailInserter ti;
	
	@Autowired
	private ClipUpdater cu;	
	
	@Autowired
    private RemoteCommandLineExecuter remoteCommandLineExecuter;
	
	@Autowired
    private SecuredCopier securedCopier;
		
	@Autowired
	private Environment env;
	
	
	@Override
	public TaskResponse execute(String taskName, String libraryName, int fileId, LogicalFile logicalFile, String category,
			String destinationDirPath) throws Exception {
		
		TaskResponse taskResponse = new TaskResponse();
		String catdvSessionId = null;
		Session jSchSession = null;
		
		// Use case 1 - For fresh ingest
		// Use case 2 - For rerun with previous request failing in transcoding
		// Use case 3 - For rerun with previous request failing in catdv 
		// Use case 4 - missed out extn
		// Check is simple if catdv reference is available in mediafile table then it means the mediafile is already inserted into catdv
		try {		
			// TODO : get groupId using libraryCategory
			int groupId = catDVConfiguration.getPublicGroupId();	
			if(category.equals("private")) { // TODO: private1/2/3?
				groupId = catDVConfiguration.getPrivateGroupId();
			}
			
			catdvSessionId = getSessionId();
	    	
			// TODO Should happen in calling class
			/*
			ThreadNameHelper threadNameHelper = new ThreadNameHelper();
			threadNameHelper.setThreadName(mediaLibraryId, mediaFile.getMediaFileId() + "-" + jobTypeName);
			
			if(CANCELLATION_INITIATED_JOB_SET.contains(job.getIngestJobId()))
				return ???;
			 */

			long startms = System.currentTimeMillis();
				
			String generatedProxyFilePath = logicalFile.getAbsolutePath(); // Proxies will be something like - /data/transcode/13491_HYTT_Yogasanas-Demo_AYA-IYC_02-Nov-2017_EX1_Mod11/1533/VIVA0985_01.mp4
			String generatedThumbnailPath = logicalFile.getSidecarFile("jpg").getAbsolutePath(); // Thumbnails will be something like - /data/transcode/13491_HYTT_Yogasanas-Demo_AYA-IYC_02-Nov-2017_EX1_Mod11/1533/VIVA0985_01.jpg
			String generatedProxyMetaDataFilePath = logicalFile.getSidecarFile("mp4_ffprobe_out").getAbsolutePath();				
			
			String proxyFilePathOnMamServer = generatedProxyFilePath.replace(StringUtils.substringBefore(generatedProxyFilePath, File.separator + category + File.separator), catDVConfiguration.getSshProxiesRootLocation());
			if(ArrayUtils.contains(env.getActiveProfiles(), "dev")) {
				logger.info("Now Copying the Proxy file " + generatedProxyFilePath + " over to " + proxyFilePathOnMamServer);
				FileUtils.copyFile(new File(generatedProxyFilePath), new File(proxyFilePathOnMamServer));
			} else {
				jSchSession = catdvSshSessionHelper.getSession();
				String parentDir = FilenameUtils.getFullPathNoEndSeparator(proxyFilePathOnMamServer);
				String command1 = "mkdir -p " + parentDir;
				remoteCommandLineExecuter.executeCommandRemotelyOnServer(jSchSession, command1, libraryName + ".out_mkdir_mamErr");
				
				logger.info("Now Copying the Proxy file " + generatedProxyFilePath + " over to catdv server location " + proxyFilePathOnMamServer);
				securedCopier.copyTo(jSchSession, generatedProxyFilePath, proxyFilePathOnMamServer);
			}
			String catalogName = getCatalogName(libraryName);
			
			int catalogId = catalogChecker.getCatalogId(catdvSessionId, catalogName);
			if(catalogId == 0) { // If there isnt the catalog exist - create new one...
				String catalogComment = libraryName;
				String createdCatalogJsonResp = cc.createCatalog(catdvSessionId, catalogName, groupId, catalogComment);
				String createdCatalogRespStatus = JsonPathUtil.getValue(createdCatalogJsonResp, "status");
				if("OK".equals(createdCatalogRespStatus)) {
					// TODO what do we do with the clip id...
					catalogId = JsonPathUtil.getInteger(createdCatalogJsonResp, "data");
					logger.info("Successfully created catalog - " + catalogId);
				}
			}
			logger.info("Now inserting the clip into catdv server");

			int catdvClipId = insertClip(catdvSessionId, fileId, catalogId, generatedProxyMetaDataFilePath, proxyFilePathOnMamServer, generatedThumbnailPath); //(proxyGenerationCompletedEvent.getMediaLibraryId(), StorageType.PRIMARY_COPY);
			taskResponse.setIsComplete(true); 
			taskResponse.setStdOutResponse("catdvClipId - " + catdvClipId);// TODO : where/how do we update externalrefid in db ...
			taskResponse.setNeedDbUpdate(true);
			taskResponse.setAppId(catdvClipId + ""); 
		} catch (Throwable e) {
			String failureReason = "insert Clip failed - " + e.getMessage();
			taskResponse.setFailureReason(failureReason);
			taskResponse.setIsComplete(false);
			logger.error(failureReason, e);
		}finally {
			if (jSchSession != null) 
				catdvSshSessionHelper.disconnectSession(jSchSession);
			if(catdvSessionId != null)
				deleteSession(catdvSessionId);
		}
		return taskResponse;		
	}

	private int insertClip(String jsessionId, int fileId, int catalogId, String metaDataFilePath, String videoFilePath, String thumbnailPath) throws Exception {
		logger.info("Inserting - " + videoFilePath);
		int insertedClipID = 0;
		String metaDataJson = null;
		try {
			metaDataJson = FileUtils.readFileToString(new File(metaDataFilePath), "UTF-8");
		} catch (IOException e) {
			throw new Exception("Unable to read the " + metaDataFilePath + " for meta generation");
		}
		
		
		String insertedClipJsonResp = ci.insertClip(jsessionId, fileId, catalogId, metaDataJson, videoFilePath);
		String insertedClipRespStatus = JsonPathUtil.getValue(insertedClipJsonResp, "status");
		if("OK".equals(insertedClipRespStatus)) {
			// TODO what do we do with the clip id...
			insertedClipID = JsonPathUtil.getInteger(insertedClipJsonResp, "data.ID");
			logger.info("Successfully inserted clip - " + insertedClipID);
			
			// insert thumbnail 
			Integer sourceMediaId = JsonPathUtil.getInteger(insertedClipJsonResp, "data.sourceMediaID");
			String insertedThumbnailResp = ti.insertThumbnail(jsessionId, sourceMediaId, thumbnailPath);
			String insertedThumbnailStatus = JsonPathUtil.getValue(insertedThumbnailResp, "status");
			Integer insertedThumbnailID = null;
			if("OK".equals(insertedThumbnailStatus)) {
				insertedThumbnailID = JsonPathUtil.getInteger(insertedThumbnailResp, "data.ID");
				
				logger.info("Successfully inserted thumbnail - " + insertedThumbnailID);
				// update clip with thumbnail as poster
				String updatedClipResp = cu.updateClip(jsessionId, insertedClipJsonResp, insertedThumbnailID);
				String updatedClipStatus = JsonPathUtil.getValue(updatedClipResp, "status");
				if("OK".equals(updatedClipStatus))				
					logger.info("Successfully updated clip " + insertedClipID + " with thumbnail " + insertedThumbnailID);
				else
					throw new Exception("Unable to update clip with " + insertedClipID + " with thumbnail " + insertedThumbnailID + " - " + updatedClipResp);
			}
			else {
				throw new Exception("Unable to insert thumbnail - " + insertedThumbnailID + " - " + insertedThumbnailResp);
			}
		}else {
			throw new Exception("Insert clip failed - " + insertedClipJsonResp);
		}
		return insertedClipID;
	}
	
	// Auth and get session id....
	private String getSessionId() throws Exception {
		String jsessionId = null;
		try {
			// TODO : get credentials from Context and cascade it - for now its all hardcoded
			//authenticator.authenticate(uid, pwd);
			String authJsonResp = authenticator.authenticate();
			String status = JsonPathUtil.getValue(authJsonResp, "status");

			if("OK".equals(status))
				jsessionId = JsonPathUtil.getValue(authJsonResp, "data.jsessionid");

			logger.debug("jsessionId :: " + jsessionId);
		} catch (Exception e) {
			logger.error("Unable to generate a session " + e.getMessage());
			throw new Exception("Unable to generate a session " + e.getMessage(), e);
		}
		return jsessionId;
	}
	
	private void deleteSession(String jsessionId) {
		authenticator.deleteSession(jsessionId);
		logger.debug("session deleted :: " + jsessionId);
	}
	
	private Pattern datePattern = Pattern.compile("_([0-9]{1,2}(-[0-9]{1,2})?-([A-Za-z]{3})-([0-9]{2,4}))(_)?");	
	
	private String getCatalogName(String mediaLibraryFolderName) {
		
		// MediaLibraryDirectoryName follows a pattern like this...
		// <<SeqId>>_<<EventName>>_<<Venue>>_<<Date>>_<<CameraDetails>>
		// For eg., 
		// 10449_Golf-Jaunt-With-Sadhguru-For-Sadhguru-Schools_Uganda-Golf-Club-Kampala-Uganda-Africa_13-Jun-2016
		// 10446_ICWTM-Alan-Kasujja-With-Sadhguru_Speke-Resort-Munyonyo-Kampala-Uganda-Africa_12-Jun-2016_Cam3_5D
		
		String catalogName = "NO_DATE/" + mediaLibraryFolderName;

//		String[] mediaLibraryFolderNameParts = mediaLibraryFolderName.split("_"); 
//		String eventDate = mediaLibraryFolderNameParts[3];
		
		Matcher m = datePattern.matcher(mediaLibraryFolderName); 	
		if(m.find()) {
			String eventDate = m.group(1);
			String month = m.group(3);
			String year = m.group(4);

			if(eventDate.contains("-")) {
				Calendar formattedDate = DateAndTimeUtil.getDateForCatalogNamePrefix(month + "-" + year);
				if(formattedDate != null)
					catalogName = formattedDate.get(Calendar.YEAR) + "/" + (formattedDate.get(Calendar.MONTH) + 1) + "/" + mediaLibraryFolderName;
			}
		}		
		return catalogName;
	}
	
}
