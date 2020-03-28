package org.ishafoundation.dwaraapi.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
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
import org.ishafoundation.dwaraapi.db.cacheutil.LibraryclassCacheUtil;
import org.ishafoundation.dwaraapi.db.cacheutil.RequesttypeCacheUtil;
import org.ishafoundation.dwaraapi.db.dao.master.FiletypeDao;
import org.ishafoundation.dwaraapi.db.dao.master.LibraryclassDao;
import org.ishafoundation.dwaraapi.db.dao.master.RequesttypeDao;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.LibraryclassRequesttypeUserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.LibraryDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.model.master.Filetype;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassRequesttypeUser;
import org.ishafoundation.dwaraapi.db.model.master.reference.Requesttype;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.ingest.scan.SourceDirScanner;
import org.ishafoundation.dwaraapi.job.JobManager;
import org.ishafoundation.dwaraapi.utils.RequestResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public class IngestController {

	private static final Logger logger = LoggerFactory.getLogger(IngestController.class);

	@Autowired
	private LibraryclassDao libraryclassDao;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private LibraryclassRequesttypeUserDao libraryclassRequesttypeUserDao;	
	
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
	private FiletypeDao filetypeDao;
	
	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private RequesttypeDao requesttypeDao;	

	@Autowired
	private JobManager jobManager;

	@Autowired
	private RequestResponseUtils requestResponseUtils;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private Extns_FiletypeCacheUtil extns_FiletypeCacheUtil;
	
	@Autowired
	private RequesttypeCacheUtil requesttypeCacheUtil;

	@Autowired
	private LibraryclassCacheUtil libraryclassCacheUtil;

	private static final String DEFAULT_REQUESTTYPE = "ingest";
	
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
		
//		// gets all users upfront and holds it in memory thus avoiding as many db calls inside the loop
//		HashMap<Integer, User> userId_User_Map = new HashMap<Integer, User>();
//		Iterable<User> userList = userDao.findAll();
//		for (User nthUser : userList) {
//			userId_User_Map.put(nthUser.getId(), nthUser);
//		}
		
		List<String> scanFolderBasePathList = new ArrayList<String>();
		Requesttype requesttypeObj = requesttypeCacheUtil.getRequesttype(DEFAULT_REQUESTTYPE);
		List<LibraryclassRequesttypeUser> libraryclassUserList = libraryclassRequesttypeUserDao.findAllByLibraryclassIdAndRequesttypeId(libraryclassId, requesttypeObj.getId());
		for (LibraryclassRequesttypeUser libraryclassRequesttypeUser : libraryclassUserList) {
//			User nthUser = userId_User_Map.get(libraryclassRequesttypeUser.getUser().getId());
			scanFolderBasePathList.add(pathPrefix + File.separator + libraryclassRequesttypeUser.getUser().getName());
		}
		
		ingestFileList = sourceDirScanner.scanSourceDir(toBeIngestedLibraryclass, scanFolderBasePathList);
		
		if (ingestFileList.size() > 0) {
			return ResponseEntity.ok(ingestFileList);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}
	}
	
	@ApiOperation(value = "Ingest comment goes here")
	@ApiResponses(value = { 
		    @ApiResponse(code = 202, message = "Request submitted and queued up"),
		    @ApiResponse(code = 400, message = "Error")
	})
	@PostMapping("/ingest")
	//public org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedRequest ingest(@RequestBody org.ishafoundation.dwaraapi.api.req.ingest.UserRequest userRequest){
    public org.ishafoundation.dwaraapi.api.resp.ingest.Request ingest(@RequestBody org.ishafoundation.dwaraapi.api.req.ingest.UserRequest userRequest){	
    	boolean isAllValid = true;
    	
    	// TODO After validation and other related code ported
    	Requesttype requesttype = requesttypeCacheUtil.getRequesttype(DEFAULT_REQUESTTYPE);
    	int requesttypeId = requesttype.getId();
    	
    	int libraryclassId = userRequest.getLibraryclassId();
    	long requestedAt = System.currentTimeMillis();
    	String requestedBy = getUserFromContext();
    	Request request = new Request();
    	request.setRequesttype(requesttype);
    	request.setLibraryclass(libraryclassCacheUtil.getLibraryclass(libraryclassId));
    	request.setRequestedAt(requestedAt);
    	request.setRequestedBy(requestedBy);
    	logger.debug("DB Request Creation");
    	request = requestDao.save(request);
    	int requestId = request.getId();
    	logger.debug("DB Request Creation - Success " + requestId);

    	// Updating the ResponseHeaderWrappedRequest subrequest
    	//List<org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest> responseHeaderWrappedSubrequest = new ArrayList<org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest>();
    	List<org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest> responseSubrequestList = new ArrayList<org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest>();

    	// Iterating the request - LibraryParams
    	List<org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams> subrequestList = userRequest.getSubrequest();
    	for (Iterator<org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams> iterator = subrequestList.iterator(); iterator.hasNext();) {
    		org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams nthSubrequest = iterator.next();
			//org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest subrequestResp = ingest_internal(request, nthSubrequest);
			org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest subrequestResp = ingest_internal(request, nthSubrequest);
			responseSubrequestList.add(subrequestResp);
		}
    	
    	//org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedRequest response = requestResponseUtils.frameWrappedRequestObjectForResponse(request, responseSubrequestList);
    	org.ishafoundation.dwaraapi.api.resp.ingest.Request response = requestResponseUtils.frameRequestObjectForResponse(request, responseSubrequestList);
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
	//private org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest ingest_internal(Request request, org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams systemGenRequest){
    private org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest ingest_internal(Request request, org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams systemGenRequest){
    	
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
    	Status status = Status.queued;
    	
    	Subrequest subrequest = new Subrequest();
    	subrequest.setNewFilename(modifiedFileName);
    	subrequest.setOldFilename(originalFileName);
    	subrequest.setOptimizeTapeAccess(true); // TODO Hardcoded for now... is it not possible to have this set to false for ingest???
    	subrequest.setPrevSequenceCode(systemGenRequest.getPrevSequenceCode());
    	subrequest.setPriority(systemGenRequest.getPriority());
    	subrequest.setRequest(request);
    	subrequest.setRerun(systemGenRequest.isRerun());
    	// TODO - Hardcoded
    	int rerunNo = 0;
    	subrequest.setRerunNo(rerunNo);
    	subrequest.setSkipTasks(systemGenRequest.getSkipTasks());
    	subrequest.setSourcePath(ingestPath);
    	subrequest.setStatus(status);

    	logger.debug("DB Subrequest Creation");
    	subrequest = subrequestDao.save(subrequest);
    	logger.debug("DB Subrequest Creation - Success " + subrequest.getId());
    	
    	Library library = new Library();
    	library.setFileCount(555); // TODO : hardcoded
    	library.setFileStructureMd5("someFileStructureMd5Value");
    	library.setLibraryclass(request.getLibraryclass());
    	library.setName(modifiedFileName);
    	logger.debug("DB Library Creation");  
    	library = libraryDao.save(library);
    	int libraryId = library.getId();
    	logger.debug("DB Library Creation - Success " + libraryId);
    	

    	
	    List<org.ishafoundation.dwaraapi.db.model.transactional.File> toBeAddedFileTableEntries = new ArrayList<org.ishafoundation.dwaraapi.db.model.transactional.File>(); 
	    for (Iterator<File> iterator = medialibraryFileAndDirsList.iterator(); iterator.hasNext();) {
			File file = (File) iterator.next();
			String filePath = file.getAbsolutePath();
			filePath = filePath.replace(stagingSrcDirRoot + File.separator, "");
			org.ishafoundation.dwaraapi.db.model.transactional.File nthFileRowToBeInserted = new org.ishafoundation.dwaraapi.db.model.transactional.File();
			nthFileRowToBeInserted.setPathname(filePath);
			nthFileRowToBeInserted.setCrc(getCrc(file));
			nthFileRowToBeInserted.setFiletype(getFiletype(file));
			nthFileRowToBeInserted.setSize(size);
			
			
			nthFileRowToBeInserted.setLibrary(library);
			toBeAddedFileTableEntries.add(nthFileRowToBeInserted);			
		}
	    
	    if(toBeAddedFileTableEntries.size() > 0) {
	    	logger.debug("DB File rows Creation");   
	    	fileDao.saveAll(toBeAddedFileTableEntries);
	    	logger.debug("DB File rows Creation - Success");
	    }

    	subrequest.setLibrary(library);
    	logger.debug("DB Subrequest Updation");
    	subrequestDao.save(subrequest);
    	logger.debug("DB Subrequest Updation - Success");
    	
    	setAttributes();
	    
    	createJobTableEntries(request, subrequest, library);
    	
    	// TODO Handle failures - systemGeneratedSubRequestRespForResponse.setResponseCode(500);
    	//return requestResponseUtils.frameWrappedSubrequestObjectForResponse(subrequest, library);
    	return requestResponseUtils.frameSubrequestObjectForResponse(subrequest, library);
    }
	

	
    private void createJobTableEntries(Request request, Subrequest subrequest, Library library) {
    	List<Job> jobList = jobManager.createJobs(request, subrequest, library);
    	logger.debug("DB Job rows Creation");   
    	jobDao.saveAll(jobList);
    	logger.debug("DB Job rows Creation - Success");
    }

	private String getUserFromContext() {
		return "";//SecurityContextHolder.getContext().getAuthentication().getName();
	}
	
	private Filetype getFiletype(File file) {
		Filetype filetype = null;
		if(file.isFile()) {
			String extn = FilenameUtils.getExtension(file.getName()).toUpperCase();
			
			filetype = filetypeDao.findByExtensionsExtensionName(extn); // TODO : extns_FiletypeCacheUtil.getExtns_FiletypeMap().get(extn);
		}
		return filetype;
	}
	
	private String getCrc(File file) {
		return "crc:TODO";
	}
	
	private void setAttributes() {
		// TODO : The attribute set of tables need to be updated...
	}
}

