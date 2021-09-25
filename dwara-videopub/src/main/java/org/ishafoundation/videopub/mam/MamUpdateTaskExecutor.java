package org.ishafoundation.videopub.mam;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.commandline.remote.sch.RemoteCommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.remote.scp.SecuredCopier;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.Artifact;
import org.ishafoundation.dwaraapi.process.request.Artifactclass;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.ishafoundation.dwaraapi.utils.DateAndTimeUtil;
import org.ishafoundation.dwaraapi.utils.JsonPathUtil;
import org.ishafoundation.videopub.mam.authn.Authenticator;
import org.ishafoundation.videopub.mam.ingest.CatalogChecker;
import org.ishafoundation.videopub.mam.ingest.CatalogCreator;
import org.ishafoundation.videopub.mam.ingest.CatalogDeleter;
import org.ishafoundation.videopub.mam.ingest.CatalogNameUpdater;
import org.ishafoundation.videopub.mam.ingest.ClipIdsGetter;
import org.ishafoundation.videopub.mam.ingest.ClipInserter;
import org.ishafoundation.videopub.mam.ingest.ClipMediaPathUpdater;
import org.ishafoundation.videopub.mam.ingest.ClipUpdater;
import org.ishafoundation.videopub.mam.ingest.ThumbnailInserter;
import org.ishafoundation.videopub.mam.sch.CatdvSshSessionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.Session;

@Component("video-mam-update")
@Primary
//@Profile({ "!dev & !stage" })
public class MamUpdateTaskExecutor implements IProcessingTask {
    
    private static final Logger logger = LoggerFactory.getLogger(MamUpdateTaskExecutor.class);

	@Autowired
	private CatDVConfiguration catDVConfiguration;
	
	@Autowired
	private Environment env;
	
	@Autowired
	private Authenticator authenticator;
	
	@Autowired
    private CatdvSshSessionHelper catdvSshSessionHelper;
	
	@Autowired
	private CatalogChecker catalogChecker;
	
	@Autowired
	private CatalogNameUpdater catalogNameUpdater;
	
	@Autowired
	private ClipMediaPathUpdater clipMediaPathUpdater;
	
	@Autowired
	private ClipIdsGetter clipIdsGetter;

	@Autowired
	private CatalogCreator cc;
	
	@Autowired
	private ClipInserter ci;
	
	@Autowired
	private ThumbnailInserter ti;
	
	@Autowired
	private ClipUpdater cu;	
	
	@Autowired
	private CatalogDeleter cd;	
	
	@Autowired
    private RemoteCommandLineExecuter remoteCommandLineExecuter;
	
	@Autowired
    private SecuredCopier securedCopier;
	
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		Artifact inputArtifact = processContext.getJob().getInputArtifact();
		String inputArtifactName = inputArtifact.getName();
		Artifactclass inputArtifactclass = inputArtifact.getArtifactclass();
		String inputArtifactClass = inputArtifactclass.getId(); 
		String category = inputArtifactclass.getCategory();
		
		LogicalFile logicalFile = processContext.getLogicalFile();
		org.ishafoundation.dwaraapi.process.request.File file = processContext.getFile();
		
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		String catdvSessionId = null;
		Session jSchSession = null;
		
		// Use case 1 - For fresh ingest
		// Use case 2 - For rerun with previous request failing in transcoding
		// Use case 3 - For rerun with previous request failing in catdv 
		// Use case 4 - missed out extn
		// Check is simple if catdv reference is available in mediafile table then it means the mediafile is already inserted into catdv
		try {		
			logger.trace("inputArtifactClass " + inputArtifactClass);
			String groupIdAsString = env.getProperty("catdv.groupId."+ inputArtifactClass);
			logger.trace("groupIdAsString " + groupIdAsString);
			// TODO : get groupId using libraryCategory
			int groupId = groupIdAsString != null ? Integer.parseInt(groupIdAsString) : 0;// default it to some groupId	
			logger.trace("groupId " + groupId);
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
			File jpgSidecarFile = logicalFile.getSidecarFile("jpg");
			File ffprobeSidecarFile = logicalFile.getSidecarFile("mp4_ffprobe_out");
			if(jpgSidecarFile == null || ffprobeSidecarFile == null)
				throw new Exception("Mam expects jpg and mp4_ffprobe_out sidecar files for videos... Pls configure it");
			String generatedThumbnailPath = jpgSidecarFile.getAbsolutePath(); // Thumbnails will be something like - /data/transcode/13491_HYTT_Yogasanas-Demo_AYA-IYC_02-Nov-2017_EX1_Mod11/1533/VIVA0985_01.jpg
			String generatedProxyMetaDataFilePath = ffprobeSidecarFile.getAbsolutePath();				
			
			String proxyFilePathOnMamServer = generatedProxyFilePath.replace(StringUtils.substringBefore(generatedProxyFilePath, File.separator + category + File.separator), catDVConfiguration.getSshProxiesRootLocation());
			jSchSession = catdvSshSessionHelper.getSession();
			String parentDir = FilenameUtils.getFullPathNoEndSeparator(proxyFilePathOnMamServer);
			String command1 = "mkdir -p \"" + parentDir + "\"";
			remoteCommandLineExecuter.executeCommandRemotelyOnServer(jSchSession, command1, processContext.getJob().getId() + ".out_mkdir_mamErr");
			
			logger.info("Now Copying the Proxy file " + generatedProxyFilePath + " over to catdv server location " + proxyFilePathOnMamServer);
			securedCopier.copyTo(jSchSession, generatedProxyFilePath, proxyFilePathOnMamServer);
			String catalogName = getCatalogName(inputArtifactName);
			
			int catalogId = catalogChecker.getCatalogId(catdvSessionId, catalogName);
			if(catalogId == 0) { // If there isnt the catalog exist - create new one...
				String catalogComment = inputArtifactName;
				String createdCatalogJsonResp = cc.createCatalog(catdvSessionId, catalogName, groupId, catalogComment);
				String createdCatalogRespStatus = JsonPathUtil.getValue(createdCatalogJsonResp, "status");
				if("OK".equals(createdCatalogRespStatus)) {
					// TODO what do we do with the clip id...
					catalogId = JsonPathUtil.getInteger(createdCatalogJsonResp, "data");
					logger.info("Successfully created catalog - " + catalogId);
				}
			}
			logger.info("Now inserting the clip into catdv server");

			int catdvClipId = insertClip(catdvSessionId, file.getFileRef().getId(), catalogId, generatedProxyMetaDataFilePath, proxyFilePathOnMamServer, generatedThumbnailPath); //(proxyGenerationCompletedEvent.getMediaLibraryId(), StorageType.PRIMARY_COPY);
			processingtaskResponse.setIsComplete(true); 
			processingtaskResponse.setStdOutResponse("catdvClipId - " + catdvClipId);// TODO : where/how do we update externalrefid in db ...
			processingtaskResponse.setAppId(catdvClipId + ""); 
		} catch (Throwable e) {
			String failureReason = "insert Clip failed - " + e.getMessage();
			processingtaskResponse.setFailureReason(failureReason);
			processingtaskResponse.setIsComplete(false);
			logger.error(failureReason, e);
			throw new Exception(failureReason);
		}finally {
			if (jSchSession != null) 
				catdvSshSessionHelper.disconnectSession(jSchSession);
			if(catdvSessionId != null)
				deleteSession(catdvSessionId);
		}
		return processingtaskResponse;		
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
				String updatedClipResp = cu.updateClip(jsessionId, insertedClipJsonResp, insertedThumbnailID, metaDataJson, videoFilePath);
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
	
	public void cleanUp(int jobId, String artifactName, String category) {
		Session jSchSession = null;
		try {
			// STEP 2 - REMOVE THE CATALOG FROM CATDV DB...
			deleteCatalog(artifactName);		
			
			// STEP 3 - DELETE THE PROXY FOLDER IN CATDV SERVER
			String copiedProxyFilePath = catDVConfiguration.getSshProxiesRootLocation() + File.separator + category + File.separator + artifactName;
			// delete the tar remotely
			String removeCommand = "rm -rf " + copiedProxyFilePath;
			jSchSession = catdvSshSessionHelper.getSession();
			remoteCommandLineExecuter.executeCommandRemotelyOnServer(jSchSession, removeCommand, jobId + ".rmProxyFolderErr");
			catdvSshSessionHelper.disconnectSession(jSchSession);
			logger.info("Deleted the proxy folder on MAM server");
		} catch (Exception e) {
			logger.error("Unable to cleanup the cancelled medialibrary artifacts " + e.getMessage());
		}finally {
    		if (jSchSession != null) 
    			catdvSshSessionHelper.disconnectSession(jSchSession);
		}
	}
	
	private void deleteCatalog(String artifactName){
		String catdvSessionId = null;
		try {
			catdvSessionId = getSessionId();
			String catalogName = getCatalogName(artifactName);
			int catalogId = catalogChecker.getCatalogId(catdvSessionId, catalogName);
			if(catalogId != 0) { // If catalog exists
				cd.deleteCatalog(catdvSessionId, catalogId);
				logger.info("Removed the catalog from MAM DB");
			}else {
				logger.debug("No catalog found on MAM side");
			}
		} catch (Exception e) {
			logger.error("Unable to remove catalog on MAM Side " + e.getMessage());
		}finally {
			if(catdvSessionId != null)
				deleteSession(catdvSessionId);
		}
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
			else
				throw new Exception("Problem with session creation " + authJsonResp);
			
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
	
	private String getCatalogName(String artifactName) {
		
		// MediaLibraryDirectoryName follows a pattern like this...
		// <<SeqId>>_<<EventName>>_<<Venue>>_<<Date>>_<<CameraDetails>>
		// For eg., 
		// 10449_Golf-Jaunt-With-Sadhguru-For-Sadhguru-Schools_Uganda-Golf-Club-Kampala-Uganda-Africa_13-Jun-2016
		// 10446_ICWTM-Alan-Kasujja-With-Sadhguru_Speke-Resort-Munyonyo-Kampala-Uganda-Africa_12-Jun-2016_Cam3_5D
		
		String catalogName = "NO_DATE/" + artifactName;

//		String[] mediaLibraryFolderNameParts = artifactName.split("_"); 
//		String eventDate = mediaLibraryFolderNameParts[3];
		
		Matcher m = datePattern.matcher(artifactName); 	
		if(m.find()) {
			String eventDate = m.group(1);
			String month = m.group(3);
			String year = m.group(4);

			if(eventDate.contains("-")) {
				Calendar formattedDate = DateAndTimeUtil.getDateForCatalogNamePrefix(month + "-" + year);
				if(formattedDate != null)
					catalogName = formattedDate.get(Calendar.YEAR) + "/" + (formattedDate.get(Calendar.MONTH) + 1) + "/" + artifactName;
			}
		}		
		return catalogName;
	}
	
	
	public void rename(String existingArtifactName, String newArtifactName, String category) {
		Session jSchSession = null;
		String catdvSessionId = null;
		try {
			// STEP 1 - RENAME THE CATALOG IN CATDV DB...
			catdvSessionId = getSessionId();
			String catalogName = getCatalogName(existingArtifactName);
			String response = catalogChecker.getCatalog(catdvSessionId, catalogName);
			int catalogId = 0;
			List<Map<String, Object>> catalogs = JsonPathUtil.getArray(response , "data");
			
			if(catalogs.size() > 0) {
				Map<String, Object> catalog = catalogs.get(0);
				catalogId = (int) catalog.get("ID");			
			}else {
				logger.warn("Catalog not found and hence would not be able to rename");
				return;
			}
			
		    String renamedCatalogName = catalogName.replace(existingArtifactName, newArtifactName);
			catalogNameUpdater.updateCatalogName(catdvSessionId, response, renamedCatalogName);
			logger.debug("Renamed the catalog on MAM");
		
			// STEP 2 - UPDATE ALL THE PATHS...
    		// Get all media files that are needed to be executed for this request...
			if(catalogId > 0) {
				List<Integer> clipIdsList = clipIdsGetter.getClipIds(catdvSessionId, catalogId);
				for (Integer nthClipId : clipIdsList) {
					clipMediaPathUpdater.updateClip(catdvSessionId, nthClipId+"", existingArtifactName, newArtifactName);	
				}
			}
    		logger.debug("updated all the clips path on MAM");
    		
			// STEP 3 - RENAME THE PROXY FOLDER 
			String existingProxiesPath = catDVConfiguration.getSshProxiesRootLocation() + File.separator + category + File.separator + existingArtifactName;
			String newProxyPath = catDVConfiguration.getSshProxiesRootLocation() + File.separator + category + File.separator + newArtifactName;
			
			// renaming the folder remotely
			String renameCommand = "mv " + existingProxiesPath + " " + newProxyPath;
			jSchSession = catdvSshSessionHelper.getSession();
			remoteCommandLineExecuter.executeCommandRemotelyOnServer(jSchSession, renameCommand,  newArtifactName + ".out");
			catdvSshSessionHelper.disconnectSession(jSchSession);
			
			logger.debug("renamed the proxy folder");
		} catch (Exception e) {
			logger.error("Unable to rename the artifact " + e.getMessage());
		}finally {
			if (jSchSession != null) 
				catdvSshSessionHelper.disconnectSession(jSchSession);
			if(catdvSessionId != null)
				deleteSession(catdvSessionId);
		}

	}
}
