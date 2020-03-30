package org.ishafoundation.dwaraapi.entrypoint.resource.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.api.exception.DwaraException;
import org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams;
import org.ishafoundation.dwaraapi.api.resp.ingest.IngestFile;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.db.cacheutil.Extns_FiletypeCacheUtil;
import org.ishafoundation.dwaraapi.db.cacheutil.LibraryclassCacheUtil;
import org.ishafoundation.dwaraapi.db.cacheutil.RequesttypeCacheUtil;
import org.ishafoundation.dwaraapi.db.dao.master.ExtensionDao;
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
import org.ishafoundation.dwaraapi.db.model.master.Extension;
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
import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.utils.EntityToResourceMappingUtils;
import org.ishafoundation.dwaraapi.utils.JunkFilesMover;
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	private ExtensionDao extensionDao;

	@Autowired
	protected CommandLineExecuter commandLineExecuter;	
	
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

	@Autowired
	private EntityToResourceMappingUtils entityToResourceMappingUtils;
	
    @Autowired
	private JunkFilesMover junkFilesMover;
	
	private static final String DEFAULT_REQUESTTYPE = "ingest";
	private String stagingSrcDirRoot = null;
	
	@PostConstruct
	private void loadTasktypeList() {
		stagingSrcDirRoot = configuration.getStagingSrcDirRoot();
	}
	
	
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

		List<String> scanFolderBasePathList = new ArrayList<String>();
		Requesttype requesttypeObj = requesttypeCacheUtil.getRequesttype(DEFAULT_REQUESTTYPE);
		List<LibraryclassRequesttypeUser> libraryclassUserList = libraryclassRequesttypeUserDao.findAllByLibraryclassIdAndRequesttypeId(libraryclassId, requesttypeObj.getId());
		for (LibraryclassRequesttypeUser libraryclassRequesttypeUser : libraryclassUserList) {
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
    	org.ishafoundation.dwaraapi.api.resp.ingest.Request response = null;	
    	try {
	    	 // TODO get this from errortype DB
	    	String errorType = "Error";
	    	
	    	int libraryclassId = userRequest.getLibraryclassId();
	    	Libraryclass libraryclass = libraryclassCacheUtil.getLibraryclass(libraryclassId);
	    	int filetypeId = libraryclass.getFiletypeId();
	
	    	List<IngestFile> ingestFileList = new ArrayList<IngestFile>();
	    	// Iterating the request - LibraryParams
	    	List<LibraryParams> libraryParamsList = userRequest.getLibraryParamsList();
	    	for (Iterator<LibraryParams> iterator = libraryParamsList.iterator(); iterator.hasNext();) {
	    		LibraryParams nthLibraryParams = iterator.next();
	    		boolean isExtnSupported = true;
	    		IngestFile ingestFile = entityToResourceMappingUtils.frameIngestFileObject(nthLibraryParams);
	    		try {
	    			checkExtensionSupport(nthLibraryParams, filetypeId);
				} catch (Exception e) {
					ingestFile.setErrorType(errorType);
					ingestFile.setErrorMessage(e.getMessage());
					isExtnSupported = false;
					isAllValid = false;
				}
	
				if(isExtnSupported && configuration.isLibraryFileSystemPermissionsNeedToBeSet()) {
					
					String script = configuration.getLibraryFile_ChangePermissionsScriptPath();
					String oldFileName = nthLibraryParams.getOldFilename();
					String sourcePath = nthLibraryParams.getSourcePath();// holds something like /data/user/pgurumurthy/ingest/pub-video
		
					CommandLineExecutionResponse setPermsCommandLineExecutionResponse = changeFilePermissions(script, sourcePath, oldFileName);
					
					if(!setPermsCommandLineExecutionResponse.isComplete()) {					
						isAllValid = false;
						File toBeIngestedFile = FileUtils.getFile(sourcePath, oldFileName);
						String errorMsg = "Unable to set permissions to " + toBeIngestedFile + ". " + setPermsCommandLineExecutionResponse.getFailureReason();
						ingestFile.setErrorType(errorType);								        
						ingestFile.setErrorMessage(ingestFile.getErrorMessage() + " :: " + errorMsg);
				        logger.error(errorMsg);
					}
		    	} 
				ingestFileList.add(ingestFile);
	    	}
	    	
			if(isAllValid) {
		    	Requesttype requesttype = requesttypeCacheUtil.getRequesttype(DEFAULT_REQUESTTYPE);
	    	
		    	long requestedAt = System.currentTimeMillis();
		    	String requestedBy = getUserFromContext();
		    	Request request = new Request();
		    	request.setRequesttype(requesttype);
		    	request.setLibraryclass(libraryclass);
		    	request.setRequestedAt(requestedAt);
		    	request.setRequestedBy(requestedBy);
		    	logger.debug("DB Request Creation");
		    	request = requestDao.save(request);
		    	int requestId = request.getId();
		    	logger.debug("DB Request Creation - Success " + requestId);
	
		    	// Updating the ResponseHeaderWrappedRequest subrequest
		    	//List<org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest> responseHeaderWrappedSubrequest = new ArrayList<org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest>();
		    	List<org.ishafoundation.dwaraapi.entrypoint.resource.Subrequest> responseSubrequestList = new ArrayList<org.ishafoundation.dwaraapi.entrypoint.resource.Subrequest>();
	
		    	for (Iterator<LibraryParams> subrequestListIterator = libraryParamsList.iterator(); subrequestListIterator.hasNext();) {
		    		LibraryParams nthLibraryParams = subrequestListIterator.next();
					//org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest subrequestResp = ingest_internal(request, nthSubrequest);
					org.ishafoundation.dwaraapi.entrypoint.resource.Subrequest subrequestResp = ingest_internal(request, nthLibraryParams);
					responseSubrequestList.add(subrequestResp);
		    	}
	
		    	//org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedRequest response = requestResponseUtils.frameWrappedRequestObjectForResponse(request, responseSubrequestList);
		    	response = requestResponseUtils.frameRequestObjectForResponse(request);
		    	response.setSubrequestList(responseSubrequestList);
			}
			else {
	    		ObjectMapper mapper = new ObjectMapper(); 
	    		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	    		JsonNode jsonNode = mapper.valueToTree(ingestFileList);
	    		throw new DwaraException("Pre ingest validation failed", jsonNode);
			}
			
		}catch (Exception e) {
			if(e instanceof DwaraException)
				throw e;
			else
				throw new DwaraException(e.getMessage(), null);
		}
    	return response;
    }
    
    // TODO - need to do this check for Audio too...
    /**
     * This method is a pre-ingest validation step and only when this step passes ingest workflow gets kicked in
     * This method does the following
     * 	1) Gets all the supported extensions from our system DB
     *  2) Iterates through all Files under the library Directory and checks if their extensions are supported in our system.
     *  3) Throws exception with unsupported extns list - if any
     * 
     * @param ingestRequestParams
     * @return
     * @throws Exception with the list of unsupported extensions...
     */
    private void checkExtensionSupport(LibraryParams ingestRequestParams, int filetypeId) throws Exception{
    	// Step 0 - Exclude the junk files...
		List<Pattern> excludedFileNamesRegexList = new ArrayList<Pattern>();
		for (int i = 0; i < configuration.getJunkFilesFinderRegexPatternList().length; i++) {
			Pattern nthJunkFilesFinderRegexPattern = Pattern.compile(configuration.getJunkFilesFinderRegexPatternList()[i]);
			excludedFileNamesRegexList.add(nthJunkFilesFinderRegexPattern);
		}
    	
    	// Step 1 - get all supported extensions in the system
		List<Extension> extensionList = extensionDao.findAllByFiletypesFiletypeId(filetypeId);
		Set<String> supportedExtns =  new TreeSet<String>();
		for (Extension extension : extensionList) {
			supportedExtns.add(extension.getName().toUpperCase());
			supportedExtns.add(extension.getName().toLowerCase());
		}
    	
    	// Step 2 - Iterate through all Files under the library Directory and check if their extensions are supported in our system.
    	String originalFileName = ingestRequestParams.getOldFilename();
    	String originFolderPath = ingestRequestParams.getSourcePath();
    	
    	File mediaLibraryFile = FileUtils.getFile(originFolderPath, originalFileName);
    	
		Collection<File> allFilesInTheSystem = null;
        if(mediaLibraryFile.isDirectory()) {
        	allFilesInTheSystem = FileUtils.listFiles(mediaLibraryFile, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
	    }else {
	    	allFilesInTheSystem = new ArrayList<File>();
	    	allFilesInTheSystem.add(mediaLibraryFile);
	    }
        
		Set<String> unSupportedExtns =  new TreeSet<String>();
		// iterate the files and get extensions
		for (Iterator<File> iterator = allFilesInTheSystem.iterator(); iterator.hasNext();) {
			File nthFile = (File) iterator.next();
			String nthFileName = nthFile.getName();
			String nthFileExtn = FilenameUtils.getExtension(nthFileName);
			
//			// excluding known useless files
//			String[] excludedExtns = config.getFileExtensionsToBeExcludedFromValidation();
//			List<String> supportedVideoExtnsAsList =  new ArrayList<String>(Arrays.asList(excludedExtns));
//			if (supportedVideoExtnsAsList.contains(nthFileExtn.toUpperCase())) 
//				continue;
			
			// skipping junk files...
			boolean isJunkFile = false;
			for (Iterator<Pattern> iterator2 = excludedFileNamesRegexList.iterator(); iterator2.hasNext();) {
				Pattern nthJunkFilesFinderRegexPattern = iterator2.next();
				Matcher m = nthJunkFilesFinderRegexPattern.matcher(nthFileName);
				if(m.matches()) {
					isJunkFile = true;
					break;
				}
			}
			if(isJunkFile) 
				continue;
			
			// validate against the supported list of extensions in the system we have
			if(!supportedExtns.contains(nthFileExtn))
				unSupportedExtns.add(nthFileExtn);
		}
		
    	// Step 3 - Throw exception with unsupported extns list	
		if(unSupportedExtns.size() > 0)
			throw new Exception("The following extns are not supported in our system - " + unSupportedExtns.toString());
		
    }
    
    private CommandLineExecutionResponse changeFilePermissions(String script, String sourcePath, String oldFileName) {
		String parts[] = sourcePath.split("/");
		String user = parts[3];
		String libraryclassName = parts[5];

		List<String> setFilePermissionsCommandParamsList = new ArrayList<String>();
		setFilePermissionsCommandParamsList.add("sudo");
		setFilePermissionsCommandParamsList.add(script);
		setFilePermissionsCommandParamsList.add(user);
		setFilePermissionsCommandParamsList.add(libraryclassName);
		setFilePermissionsCommandParamsList.add(oldFileName);

		CommandLineExecutionResponse setPermsCommandLineExecutionResponse = commandLineExecuter.executeCommand(setFilePermissionsCommandParamsList, oldFileName + "-setPermsErr.out");
		return setPermsCommandLineExecutionResponse;
    }
	
	/*
	 * This method is responsible for the following
	 * 0) Grant Permissions on folders and the files...
	 * 1) Moves the file from ReadyToIngest directory to Staging directory
	 * 2) Moves Junk files to the configured hidden junk directory inside the medialibrary directory
	 * 3) Calculates MD5 for the folder content
	 * 4) Checks if library DB entry already present, if not adds it else updates the existing one
	 * 5) Creates request DB entry
	 * 6) Creates as many file DB entries as the list of files inside the library directory
	 * 7) Creates job DB entries for as many parent jobs
	 * 8) Creates event DB entry for catdv catalogs
	 * 9) Kicks off the Copy and Processing commands which will kickoff copy and transcoding parent jobs respectively
	 * 10) Then Responds with the created medialibrary DB entries ID.
	 * 
	 */
	//private org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest ingest_internal(Request request, org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams systemGenRequest){
    private org.ishafoundation.dwaraapi.entrypoint.resource.Subrequest ingest_internal(Request request, org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams systemGenRequest){
    	
    	String oldFileName = systemGenRequest.getOldFilename();
    	String newFileName = systemGenRequest.getNewFilename();
    	
    	String sourcePath = systemGenRequest.getSourcePath();
    	String libraryFileName = oldFileName;
        if(StringUtils.isNotBlank(newFileName) && !oldFileName.equals(newFileName)){
        	libraryFileName = newFileName;
        }
    	logger.trace("Now ingesting - " + libraryFileName);
		
    	// STEP 1 - Moves the file from ReadyToIngest directory to Staging directory
    	File libraryFileInStagingDir = null;
    	try {
    		libraryFileInStagingDir = moveFileToStaging(sourcePath, oldFileName, libraryFileName, systemGenRequest.isRerun());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	// STEP 2 - Moves Junk files
    	String junkFilesStagedDirName = configuration.getJunkFilesStagedDirName(); 
    	if(libraryFileInStagingDir.isDirectory())
    		junkFilesMover.moveJunkFilesFromMediaLibrary(libraryFileInStagingDir.getAbsolutePath());
    	
    	Collection<File> libraryFileAndDirsList = getFileList(libraryFileInStagingDir, junkFilesStagedDirName);
    	int fileCount = libraryFileAndDirsList.size();
        double size = FileUtils.sizeOf(libraryFileInStagingDir);
    	
    	Status status = Status.queued;
    	
    	Subrequest subrequest = new Subrequest();
    	subrequest.setNewFilename(newFileName);
    	subrequest.setOldFilename(oldFileName);
    	subrequest.setOptimizeTapeAccess(true); // TODO Hardcoded for now... is it not possible to have this set to false for ingest???
    	subrequest.setPrevSequenceCode(systemGenRequest.getPrevSequenceCode());
    	subrequest.setPriority(systemGenRequest.getPriority());
    	subrequest.setRequest(request);
    	subrequest.setRerun(systemGenRequest.isRerun());
    	int rerunNo = 0; // TODO : Hardcoded for now
    	subrequest.setRerunNo(rerunNo);
    	subrequest.setSkipTasks(systemGenRequest.getSkipTasks());
    	subrequest.setSourcePath(sourcePath);
    	subrequest.setStatus(status);

    	logger.debug("DB Subrequest Creation");
    	subrequest = subrequestDao.save(subrequest);
    	logger.debug("DB Subrequest Creation - Success " + subrequest.getId());
    	
    	Library library = new Library();
    	library.setFileCount(fileCount);
    	library.setFileStructureMd5("someFileStructureMd5Value"); // TODO : Hardcoded for now
    	library.setLibraryclass(request.getLibraryclass());
    	library.setName(newFileName);
    	library.setqLatestSubrequestId(subrequest.getId());
    	logger.debug("DB Library Creation");  
    	library = libraryDao.save(library);
    	int libraryId = library.getId();
    	logger.debug("DB Library Creation - Success " + libraryId);
    	

    	
	    List<org.ishafoundation.dwaraapi.db.model.transactional.File> toBeAddedFileTableEntries = new ArrayList<org.ishafoundation.dwaraapi.db.model.transactional.File>(); 
	    for (Iterator<File> iterator = libraryFileAndDirsList.iterator(); iterator.hasNext();) {
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
    	subrequest = subrequestDao.save(subrequest);
    	logger.debug("DB Subrequest Updation - Success");
    	
    	setAttributes();
	    
    	createJobTableEntries(request, subrequest, library);
    	
    	// TODO Handle failures - systemGeneratedSubRequestRespForResponse.setResponseCode(500);
    	//return requestResponseUtils.frameWrappedSubrequestObjectForResponse(subrequest, library);
    	return requestResponseUtils.frameSubrequestObjectForResponse(subrequest);
    }
	

    private File moveFileToStaging(String sourcePath, String oldFileName, String libraryFileName, boolean isRerun) throws Exception {
    	File libraryFileInReadyToIngestDir = FileUtils.getFile(sourcePath, oldFileName);
    	String libraryFilePathInStagingDir = stagingSrcDirRoot + File.separator + libraryFileName;
    	File libraryFileInStagingDir = FileUtils.getFile(libraryFilePathInStagingDir);
    	if(isRerun) {
    		logger.info("Skipped setting permissions and moving medialibrary directory from RTI to Staging area");
    	}
    	else {
    		// TODO USE MOVE using commandline
	    	// STEP 1 - Moves the file from ReadyToIngest directory to Staging directory
	    	try {
	    		if(libraryFileInReadyToIngestDir.isDirectory())
	    			FileUtils.moveDirectory(libraryFileInReadyToIngestDir, libraryFileInStagingDir);
	    		else if(libraryFileInReadyToIngestDir.isFile())
	    			FileUtils.moveFile(libraryFileInReadyToIngestDir, libraryFileInStagingDir);
			} catch (IOException e) {
				String errorMsg = "Unable to move file " + libraryFileInReadyToIngestDir + " to " + libraryFileInStagingDir + " - " + e.getMessage();
				logger.error(errorMsg, e);
				throw new Exception(errorMsg);
			}
    	}
		return libraryFileInStagingDir;
    }

    private Collection<File> getFileList(File libraryFileInStagingDir, String junkFilesStagedDirName) {
        IOFileFilter dirFilter = null;
        Collection<File> libraryFileAndDirsList = null;
	    if(libraryFileInStagingDir.isDirectory()) {
			dirFilter = FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(junkFilesStagedDirName, null));
	    	libraryFileAndDirsList = FileUtils.listFilesAndDirs(libraryFileInStagingDir, TrueFileFilter.INSTANCE, dirFilter);
	    }else {
	    	libraryFileAndDirsList = new ArrayList<File>();
	    	libraryFileAndDirsList.add(libraryFileInStagingDir);
	    }
	    return libraryFileAndDirsList;
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

