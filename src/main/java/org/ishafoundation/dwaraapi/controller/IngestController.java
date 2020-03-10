package org.ishafoundation.dwaraapi.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.api.resp.ingest.IngestFile;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.db.cacheutil.Extns_FiletypeCacheUtil;
import org.ishafoundation.dwaraapi.db.dao.master.common.RequesttypeDao;
import org.ishafoundation.dwaraapi.db.dao.master.common.UserDao;
import org.ishafoundation.dwaraapi.db.dao.master.ingest.LibraryclassDao;
import org.ishafoundation.dwaraapi.db.dao.master.ingest.LibraryclassUserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.LibraryDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.model.master.common.Requesttype;
import org.ishafoundation.dwaraapi.db.model.master.common.User;
import org.ishafoundation.dwaraapi.db.model.master.ingest.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.ingest.LibraryclassUser;
import org.ishafoundation.dwaraapi.db.model.master.process.Filetype;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.ingest.scan.SourceDirScanner;
import org.ishafoundation.dwaraapi.job.JobManager;
import org.ishafoundation.dwaraapi.utils.ResponseRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
public class IngestController {

	private static final Logger logger = LoggerFactory.getLogger(IngestController.class);

	@Autowired
	private LibraryclassDao libraryclassDao;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private LibraryclassUserDao libraryclassUserDao;
	
	@Autowired
	private SourceDirScanner sourceDirScanner;
	
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
	private ResponseRequestUtils requestUtils;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private Extns_FiletypeCacheUtil extns_FiletypeCacheUtil;
	
	@ApiOperation(value = "Scans the selected directory path chosen in the dropdown and lists all candidate folders for users to ingest it.", response = List.class)
	@ApiResponses(value = { 
		    @ApiResponse(code = 200, message = "Ok"),
		    @ApiResponse(code = 204, message = "No folders found to be ingested"),
		    @ApiResponse(code = 401, message = "Unauthorized"),
		    @ApiResponse(code = 403, message = "Forbidden"),
		    @ApiResponse(code = 404, message = "Not Found")
	})
	@GetMapping("/ingest/library")
	public ResponseEntity<List<IngestFile>> getAllIngestableFiles(@RequestParam int libraryclassId) {
		List<IngestFile> ingestFileList = new ArrayList<IngestFile>();
		
		Libraryclass toBeIngestedLibraryclass = libraryclassDao.findById(libraryclassId).get();
		String pathPrefix = toBeIngestedLibraryclass.getPathPrefix();
		
		// gets all users upfront and holds it in memory thus avoiding as many db calls inside the loop
		HashMap<Integer, User> userId_User_Map = new HashMap<Integer, User>();
		Iterable<User> userList = userDao.findAll();
		for (User nthUser : userList) {
			userId_User_Map.put(nthUser.getUserId(), nthUser);
		}
		
		List<String> scanFolderBasePathList = new ArrayList<String>();
		List<LibraryclassUser> libraryclassUserList = libraryclassUserDao.findAllByLibraryclassId(libraryclassId);
		for (LibraryclassUser nthLibraryclassUser : libraryclassUserList) {
			User nthUser = userId_User_Map.get(nthLibraryclassUser.getUserId());
			scanFolderBasePathList.add(pathPrefix + File.separator + nthUser.getName());
		}
		
		ingestFileList = sourceDirScanner.scanSourceDir(toBeIngestedLibraryclass, scanFolderBasePathList);
		
		if (ingestFileList.size() > 0) {
			return ResponseEntity.ok(ingestFileList);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}
	}
	
	@PostMapping("/ingest")
    public org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedRequest ingest(@RequestBody org.ishafoundation.dwaraapi.api.req.ingest.UserRequest userRequest){	
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

    	// Updating the ResponseHeaderWrappedRequest subrequest
    	List<org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest> responseHeaderWrappedSubrequest = new ArrayList<org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest>();

    	// Iterating the request - LibraryParams
    	List<org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams> subrequestList = userRequest.getSubrequest();
    	for (Iterator<org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams> iterator = subrequestList.iterator(); iterator.hasNext();) {
    		org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams nthSubrequest = iterator.next();
			
			org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest subrequestResp = ingest_internal(request, nthSubrequest);
			responseHeaderWrappedSubrequest.add(subrequestResp);
		}
    	
    	org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedRequest response = requestUtils.frameWrappedRequestObjectForResponse(request, responseHeaderWrappedSubrequest);
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
    private org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest ingest_internal(Request request, org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams systemGenRequest){
    	
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
    	
    	long requestedAt = request.getRequestedAt();
    	String requestedBy = request.getRequestedBy();
    	int statusId = Status.QUEUED.getStatusId();
    	
    	Subrequest subrequest = new Subrequest();
    	subrequest.setNewFilename(modifiedFileName);
    	subrequest.setOldFilename(originalFileName);
    	subrequest.setOptimizeTapeAccess(true); // TODO Hardcoded for now... is it not possible to have this set to false for ingest???
    	subrequest.setPrevSequenceCode(systemGenRequest.getPrevSequenceCode());
    	subrequest.setPriority(systemGenRequest.getPriority());
    	subrequest.setRequestId(request.getRequestId());
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
    	library.setLibraryclassId(request.getLibraryclassId());
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
	    
    	createJobTableEntries(request.getRequesttypeId(), request.getLibraryclassId(), subrequest, library);
    	
    	// TODO Handle failures - systemGeneratedSubRequestRespForResponse.setResponseCode(500);
    	return requestUtils.frameWrappedSubrequestObjectForResponse(subrequest, library);
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

