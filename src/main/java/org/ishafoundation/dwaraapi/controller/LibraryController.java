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
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.LibraryDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.process.Filetype;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.workflow.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ingest")
public class LibraryController {

	private static final Logger logger = LoggerFactory.getLogger(LibraryController.class);
	
	@Autowired
	private RequestDao requestDao;

	@Autowired
	private LibraryDao libraryDao;

	@Autowired
	private FileDao fileDao;
	
	@Autowired
	private JobManager workflowManager;
	
	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private Extns_FiletypeCacheUtil extns_FiletypeCacheUtil;
	
	
//    @PostMapping("/multi-ingest")
//    public List<MediaLibraryResp> multiIngest(@RequestBody List<IngestRequestParams> ingestRequestParamsList){}
//    
//    /**
//     * This method is a pre-ingest validation step and only when this step passes ingest workflow gets kicked in
//     * This method does the following
//     * 	1) Gets all the supported extensions from our system DB
//     *  2) Iterates through all Files under the mediaLibrary Directory and checks if their extensions are supported in our system.
//     *  3) Throws exception with unsupported extns list - if any
//     * 
//     * @param ingestRequestParams
//     * @return
//     * @throws Exception
//     */
//    private boolean validate(IngestRequestParams ingestRequestParams) throws Exception{}
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
    @PostMapping("/ingest")
    public ResponseEntity<String> ingest(
    		@NotNull @RequestParam int libraryclassId,
    		@NotNull @RequestParam String ingestPath,
            @NotNull @RequestParam String originalFileName,
            @RequestParam(required=false) String modifiedFileName,
            @RequestParam(required=false) String prevSequenceCode,
            @RequestParam(required=false) int requestTypeId,
            @RequestParam(required=false) int runModeId,
            @RequestParam(required=false) int recoveryPoolId){
    	
    	
    	String mediaLibraryFileName = originalFileName;
        if(StringUtils.isNotBlank(modifiedFileName) && !originalFileName.equals(modifiedFileName)){
        	mediaLibraryFileName = modifiedFileName;
        }
    	logger.trace("Now ingesting - " + mediaLibraryFileName);
    	File mediaLibraryFileInReadyToIngestDir = FileUtils.getFile(ingestPath, originalFileName);

        // TODO : For now hardcoded...
    	//String stagingSrcDirRoot = config.getStagingSrcDirRoot();
    	String stagingSrcDirRoot = ingestPath;
    	
    	String mediaLibraryFilePathInStagingDir = stagingSrcDirRoot + File.separator + mediaLibraryFileName;
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
    	
    	Request ir = new Request();
    	ir.setLibraryclassId(libraryclassId);
    	ir.setNewFilename(modifiedFileName);
    	ir.setOldFilename(originalFileName);
    	ir.setPrevSequenceCode(prevSequenceCode);
    	ir.setRequestedAt(requestedAt);
    	ir.setRequestedBy(requestedBy);
    	ir.setRequesttypeId(requestTypeId);
    	ir.setSourcePath(ingestPath);
    	ir.setStatusId(statusId);
    	ir.setOptimizeTapeAccess(true); // TODO Hardcoded for now... is it not possible to have this set to false for ingest???
    	logger.debug("DB Request Creation");
    	ir = requestDao.save(ir);
    	logger.debug("DB Request Creation - Success " + ir.getRequestId());
    	
    	Library lib = new Library();
    	lib.setFileCount(555);
    	lib.setFileStructureMd5("someFileStructureMd5Value");
    	lib.setLibraryclassId(libraryclassId);
    	lib.setName(modifiedFileName);
    	logger.debug("DB Library Creation");  
    	lib = libraryDao.save(lib);
    	logger.debug("DB Library Creation - Success " + lib.getLibraryId());
    	int libraryId = lib.getLibraryId();

    	
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

    	ir.setLibraryId(libraryId);
    	logger.debug("DB Request Updation");
    	requestDao.save(ir);
    	logger.debug("DB Request Updation - Success");
    	
    	setAttributes();
	    
    	createJobTableEntries(ir, lib);
    	
    	return null;
    }
    
    private void createJobTableEntries(Request ingestrequest, Library library) {
    	List<Job> jobList = workflowManager.createJobsForIngest(ingestrequest, library);
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

