package org.ishafoundation.dwaraapi.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.db.cacheutil.Extns_FiletypeCacheUtil;
import org.ishafoundation.dwaraapi.db.dao.master.common.RequesttypeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.LibraryDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.model.master.common.Requesttype;
import org.ishafoundation.dwaraapi.db.model.master.process.Filetype;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.job.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IngestController {

	private static final Logger logger = LoggerFactory.getLogger(IngestController.class);
	
	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private SubrequestDao subrequestDao;

	@Autowired
	private LibraryDao libraryDao;

	@Autowired
	private FileDao fileDao;
	
	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private RequesttypeDao requesttypeDao;	

	@Autowired
	private JobManager jobManager;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private Extns_FiletypeCacheUtil extns_FiletypeCacheUtil;
	
	@PostMapping("ingest")
    public org.ishafoundation.dwaraapi.api.resp.ingest.Response ingest(@RequestBody org.ishafoundation.dwaraapi.api.req.ingest.UserRequest userRequest){	
    	boolean isAllValid = true;
    	
    	// TODO After validation and other related code ported
    	Requesttype requesttype = requesttypeDao.findByName("INGEST");
    	int requesttypeId = requesttype.getRequesttypeId();
    	
    	int libraryclassId = userRequest.getLibraryclassId();
    	long requestedAt = System.currentTimeMillis();
    	String requestedBy = getUserFromContext();
    	Request request = new Request();
    	request.setRequesttypeId(requesttypeId);
    	request.setLibraryclassId(libraryclassId);
    	request.setRequestedAt(requestedAt);
    	request.setRequestedBy(requestedBy);
    	logger.debug("DB Request Creation");
    	request = requestDao.save(request);
    	int requestId = request.getRequestId();
    	logger.debug("DB Request Creation - Success " + requestId);

    	// Updating the Response subrequest
    	List<org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestResp> subrequestRespListForResponse = new ArrayList<org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestResp>();

    	// Iterating the request - LibraryParams
    	List<org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams> subrequestList = userRequest.getSubrequest();
    	for (Iterator<org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams> iterator = subrequestList.iterator(); iterator.hasNext();) {
    		org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams nthSubrequest = iterator.next();
			
			org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestResp subrequestResp = ingest(requesttypeId, requestId, libraryclassId, nthSubrequest);
			subrequestRespListForResponse.add(subrequestResp);
		}
    	org.ishafoundation.dwaraapi.api.resp.ingest.Request requestForResponse = new org.ishafoundation.dwaraapi.api.resp.ingest.Request();
    	requestForResponse.setLibraryclassId(libraryclassId);
    	requestForResponse.setRequestedAt(requestedAt);
    	requestForResponse.setRequestedBy(requestedBy);
    	requestForResponse.setRequestId(requestId);
    	requestForResponse.setRequesttypeId(requesttypeId);
    	requestForResponse.setSubrequestResp(subrequestRespListForResponse);
    	
    	org.ishafoundation.dwaraapi.api.resp.ingest.Response response = new org.ishafoundation.dwaraapi.api.resp.ingest.Response();
    	response.setResponseCode(200);
    	response.setResponseMessage("Som message");
    	response.setResponseType("some responseType");
    	response.setRequest(requestForResponse);
    	return response;
    }
	
	/*
	 * This method is responsible for the following
	 * 0) Grant Permissions on folders and the files...
	 * 1) Moves the file from ReadyToIngest directory to Staging directory
	 * 2) Moves Junk files to the configured hidden junk directory inside the medialibrary directory
	 * 3) Calculates MD5 for the folder content
	 * 4) Checks if medialibrary DB entry already present, if not adds it else updates the existing one
	 * 5) Creates request DB entry
	 * 6) Creates as many file DB entries as the list of files inside the medialibrary directory
	 * 7) Creates job DB entries for as many parent jobs
	 * 8) Creates event DB entry for catdv catalogs
	 * 9) Kicks off the Copy and Processing commands which will kickoff copy and transcoding parent jobs respectively
	 * 10) Then Responds with the created medialibrary DB entries ID.
	 * 
	 */
    private org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestResp ingest(
    		@NotNull @RequestParam int requesttypeId,
    		@NotNull @RequestParam int requestId,
    		@NotNull @RequestParam int libraryclassId,
    		@NotNull @RequestParam org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams systemGenRequest){
    	
    	String originalFileName = systemGenRequest.getOldFilename();
    	String modifiedFileName = systemGenRequest.getNewFilename();
    	
    	String ingestPath = systemGenRequest.getSourcePath();
    	String libraryFileName = originalFileName;
        if(StringUtils.isNotBlank(modifiedFileName) && !originalFileName.equals(modifiedFileName)){
        	libraryFileName = modifiedFileName;
        }
    	logger.trace("Now ingesting - " + libraryFileName);
    	File mediaLibraryFileInReadyToIngestDir = FileUtils.getFile(ingestPath, originalFileName);

        // TODO : For now hardcoded...
    	//String stagingSrcDirRoot = config.getStagingSrcDirRoot();
    	String stagingSrcDirRoot = ingestPath;
    	
    	String mediaLibraryFilePathInStagingDir = stagingSrcDirRoot + File.separator + libraryFileName;
    	File mediaLibraryFileInStagingDir = FileUtils.getFile(mediaLibraryFilePathInStagingDir);    	
        int fileCount = 0;
        IOFileFilter dirFilter = null;
        Collection<File> medialibraryFileAndDirsList = null;
        String junkFilesStagedDirName = ".dwara-ignored";//config.getJunkFilesStagedDirName(); 
        

        if(mediaLibraryFileInStagingDir.isDirectory()) {
			dirFilter = FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(junkFilesStagedDirName, null));
	    	medialibraryFileAndDirsList = FileUtils.listFilesAndDirs(mediaLibraryFileInStagingDir, TrueFileFilter.INSTANCE, dirFilter);
	    	fileCount = medialibraryFileAndDirsList.size();
	    }else {
	    	medialibraryFileAndDirsList = new ArrayList<File>();
	    	medialibraryFileAndDirsList.add(mediaLibraryFileInStagingDir);
	    	fileCount = 1;
	    }
        double size = FileUtils.sizeOf(mediaLibraryFileInStagingDir);
    	
    	long requestedAt = Calendar.getInstance().getTimeInMillis();
    	String requestedBy = getUserFromContext();
    	int statusId = Status.QUEUED.getStatusId();
    	
    	Subrequest subrequest = new Subrequest();
    	subrequest.setNewFilename(modifiedFileName);
    	subrequest.setOldFilename(originalFileName);
    	subrequest.setOptimizeTapeAccess(true); // TODO Hardcoded for now... is it not possible to have this set to false for ingest???
    	subrequest.setPrevSequenceCode(systemGenRequest.getPrevSequenceCode());
    	subrequest.setPriority(systemGenRequest.getPriority());
    	subrequest.setRequestId(requestId);
    	subrequest.setRerun(systemGenRequest.isRerun());
    	// TODO - Hardcoded
    	int rerunNo = 0;
    	subrequest.setRerunNo(rerunNo);
    	subrequest.setSkipTasks(systemGenRequest.getSkipTasks());
    	subrequest.setSourcePath(ingestPath);
    	subrequest.setStatusId(statusId);

    	logger.debug("DB Subrequest Creation");
    	subrequest = subrequestDao.save(subrequest);
    	logger.debug("DB Subrequest Creation - Success " + subrequest.getRequestId());
    	
    	Library library = new Library();
    	library.setFileCount(555); // TODO : hardcoded
    	library.setFileStructureMd5("someFileStructureMd5Value");
    	library.setLibraryclassId(libraryclassId);
    	library.setName(modifiedFileName);
    	logger.debug("DB Library Creation");  
    	library = libraryDao.save(library);
    	logger.debug("DB Library Creation - Success " + library.getLibraryId());
    	int libraryId = library.getLibraryId();

    	
	    List<org.ishafoundation.dwaraapi.db.model.transactional.File> toBeAddedFileTableEntries = new ArrayList<org.ishafoundation.dwaraapi.db.model.transactional.File>(); 
	    for (Iterator<File> iterator = medialibraryFileAndDirsList.iterator(); iterator.hasNext();) {
			File file = (File) iterator.next();
			String filePath = file.getAbsolutePath();
			filePath = filePath.replace(stagingSrcDirRoot + File.separator, "");
			org.ishafoundation.dwaraapi.db.model.transactional.File nthFileRowToBeInserted = new org.ishafoundation.dwaraapi.db.model.transactional.File();
			nthFileRowToBeInserted.setPathname(filePath);
			nthFileRowToBeInserted.setCrc(getCrc(file));
			nthFileRowToBeInserted.setFiletypeId(getFiletypeId(file));
			nthFileRowToBeInserted.setSize(size);
			
			
			nthFileRowToBeInserted.setLibraryId(libraryId);
			toBeAddedFileTableEntries.add(nthFileRowToBeInserted);			
		}
	    
	    if(toBeAddedFileTableEntries.size() > 0) {
	    	logger.debug("DB File rows Creation");   
	    	fileDao.saveAll(toBeAddedFileTableEntries);
	    	logger.debug("DB File rows Creation - Success");
	    }

    	subrequest.setLibraryId(libraryId);
    	logger.debug("DB Subrequest Updation");
    	subrequestDao.save(subrequest);
    	logger.debug("DB Subrequest Updation - Success");
    	
    	setAttributes();
	    
    	createJobTableEntries(requesttypeId, libraryclassId, subrequest, library);
    	
    	// TODO _ Check on the 
    	// http://eloquentdeveloper.com/2016/09/28/automatically-mapping-java-objects/
    	// https://www.baeldung.com/mapstruct
    	// https://www.baeldung.com/java-performance-mapping-frameworks
    	org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest systemGeneratedSubRequestForResponse = new org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest();
    	systemGeneratedSubRequestForResponse.setLibrary(library);
    	systemGeneratedSubRequestForResponse.setSubrequestId(subrequest.getSubrequestId());
    	systemGeneratedSubRequestForResponse.setNewFilename(systemGenRequest.getNewFilename());
    	systemGeneratedSubRequestForResponse.setOldFilename(systemGenRequest.getOldFilename());
    	systemGeneratedSubRequestForResponse.setOptimizeTapeAccess(systemGenRequest.isOptimizeTapeAccess());
    	systemGeneratedSubRequestForResponse.setPrevSequenceCode(systemGenRequest.getPrevSequenceCode());
    	systemGeneratedSubRequestForResponse.setPriority(systemGenRequest.getPriority());
    	systemGeneratedSubRequestForResponse.setRequestId(requestId);
    	systemGeneratedSubRequestForResponse.setRerun(systemGenRequest.isRerun());
    	systemGeneratedSubRequestForResponse.setRerunNo(rerunNo);
    	systemGeneratedSubRequestForResponse.setSkipTasks(systemGenRequest.getSkipTasks());
    	systemGeneratedSubRequestForResponse.setSourcePath(systemGenRequest.getSourcePath());
    	systemGeneratedSubRequestForResponse.setStatusId(Status.QUEUED.getStatusId());
    	
    	org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestResp systemGeneratedSubRequestRespForResponse = new org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestResp();
    	
    	systemGeneratedSubRequestRespForResponse.setResponseCode(200);
    	systemGeneratedSubRequestRespForResponse.setResponseMessage("Resp message");
    	systemGeneratedSubRequestRespForResponse.setResponseType("Resp type");
    	systemGeneratedSubRequestRespForResponse.setSubrequest(systemGeneratedSubRequestForResponse);
    	
    	logger.trace(systemGeneratedSubRequestRespForResponse.toString());
    	// TODO Handle failures - systemGeneratedSubRequestRespForResponse.setResponseCode(500);
    	return systemGeneratedSubRequestRespForResponse;
    }
    
    private void createJobTableEntries(int requesttypeId, int libraryclassId, Subrequest subrequest, Library library) {
    	List<Job> jobList = jobManager.createJobs(requesttypeId, libraryclassId, subrequest, library);
    	logger.debug("DB Job rows Creation");   
    	jobDao.saveAll(jobList);
    	logger.debug("DB Job rows Creation - Success");
    }

	private String getUserFromContext() {
		return "";//SecurityContextHolder.getContext().getAuthentication().getName();
	}
	
	private int getFiletypeId(File file) {
		int filetypeId = 0;
		if(file.isFile()) {
			String extn = FilenameUtils.getExtension(file.getName()).toUpperCase();
			
			Filetype filetype = extns_FiletypeCacheUtil.getExtns_FiletypeMap().get(extn);
			if(filetype != null) {
				filetypeId = filetype.getFiletypeId();				
			}
		}
		return filetypeId;
	}
	
	private String getCrc(File file) {
		return "crc:TODO";
	}
	
	private void setAttributes() {
		// TODO : The attribute set of tables need to be updated...
	}
}

