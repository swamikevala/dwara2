package org.ishafoundation.dwaraapi.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.req.staged.ingest.IngestUserRequest;
import org.ishafoundation.dwaraapi.api.req.staged.ingest.StagedFile;
import org.ishafoundation.dwaraapi.api.req.staged.rename.StagedRenameFile;
import org.ishafoundation.dwaraapi.api.resp.staged.ingest.IngestResponse;
import org.ishafoundation.dwaraapi.api.resp.staged.ingest.IngestSystemRequest;
import org.ishafoundation.dwaraapi.api.resp.staged.rename.StagedRenameResponse;
import org.ishafoundation.dwaraapi.api.resp.staged.scan.StagedFileDetails;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.ExtensionDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ActionArtifactclassUserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileEntityUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Extension;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ActionArtifactclassUser;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.db.utils.SequenceUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.ishafoundation.dwaraapi.resource.mapper.MiscObjectMapper;
import org.ishafoundation.dwaraapi.resource.mapper.RequestToEntityObjectMapper;
import org.ishafoundation.dwaraapi.staged.StagedFileOperations;
import org.ishafoundation.dwaraapi.staged.scan.Error;
import org.ishafoundation.dwaraapi.staged.scan.Errortype;
import org.ishafoundation.dwaraapi.staged.scan.SourceDirScanner;
import org.ishafoundation.dwaraapi.staged.scan.StagedFileEvaluator;
import org.ishafoundation.dwaraapi.utils.ExtensionsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class StagedService extends DwaraService{

	private static final Logger logger = LoggerFactory.getLogger(StagedService.class);


	@Autowired
	protected RequestDao requestDao;
	
	@Autowired
	protected ExtensionDao extensionDao;
	
	@Autowired
	private ActionArtifactclassUserDao artifactclassActionUserDao;
	
	@Autowired
	protected SequenceUtil sequenceUtil;
	
	@Autowired
	protected JobCreator jobCreator;

	@Autowired
	private RequestToEntityObjectMapper requestToEntityObjectMapper; 

	@Autowired
	private MiscObjectMapper miscObjectMapper; 
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private FileEntityUtil fileEntityUtil;

	@Autowired
	private ExtensionsUtil extensionsUtil;
	
	@Autowired
    private StagedFileEvaluator stagedFileEvaluator;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private SourceDirScanner sourceDirScanner;

	@Autowired
    private StagedFileOperations stagedFileOperations;
	
    public List<StagedFileDetails> getAllIngestableFiles(String artifactclassId){
		Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(artifactclassId);
		List<String> scanFolderBasePathList = new ArrayList<String>();
		
		List<ActionArtifactclassUser> artifactclassActionUserList = artifactclassActionUserDao.findAllByArtifactclassIdAndActionId(artifactclassId, Action.ingest.name());
		for (ActionArtifactclassUser artifactclassActionUser : artifactclassActionUserList) {
			scanFolderBasePathList.add(configuration.getReadyToIngestSrcDirRoot() + java.io.File.separator + artifactclassActionUser.getUser().getName());
		}
		
		return sourceDirScanner.scanSourceDir(artifactclass, scanFolderBasePathList);
	}
	
    public StagedRenameResponse renameStagedFiles(List<StagedRenameFile> stagedRenameFileList) throws DwaraException{
    	StagedRenameResponse stagedRenameResponse= new StagedRenameResponse();

    	Request request = new Request();
    	request.setType(RequestType.user);
		request.setActionId(Action.rename_staged);
    	request.setRequestedBy(getUserObjFromContext());
		request.setRequestedAt(LocalDateTime.now());
		
		RequestDetails details = new RequestDetails();
		JsonNode postBodyJson = getRequestDetails(stagedRenameFileList); 
		details.setBody(postBodyJson);
		request.setDetails(details);
    	
		List<org.ishafoundation.dwaraapi.api.resp.staged.rename.StagedRenameFileForResponse> stagedRenameFileResponseList = new ArrayList<org.ishafoundation.dwaraapi.api.resp.staged.rename.StagedRenameFileForResponse>();
    	boolean hasCompleted = false;
    	boolean hasFailures = false;
    	for (StagedRenameFile stagedRenameFile : stagedRenameFileList) {
        	String sourcePath = stagedRenameFile.getPath();
    		String oldFileName = stagedRenameFile.getOldName();
    		String newFileName = stagedRenameFile.getNewName();
    		org.ishafoundation.dwaraapi.api.resp.staged.rename.StagedRenameFileForResponse stagedRenameFileResponse = new  org.ishafoundation.dwaraapi.api.resp.staged.rename.StagedRenameFileForResponse();
    		stagedRenameFileResponse.setPath(sourcePath);
    		stagedRenameFileResponse.setOldName(oldFileName);
    		stagedRenameFileResponse.setNewName(newFileName);
  
        	try {
        		Error error = stagedFileOperations.rename(sourcePath, oldFileName, newFileName);
        		if(error != null)
        			throw new Exception(error.getMessage());
    			stagedRenameFileResponse.setStatus(Status.completed.name());
    			hasCompleted = true;
    		}
    		catch (Exception e) {
    			hasFailures = true;
    			//throw new DwaraException("Unable to rename " + oldFileName + " to " + newFileName + " in " + sourcePath + " - " + e.getMessage(), null);
    	  		stagedRenameFileResponse.setStatus(Status.failed.name());
        		stagedRenameFileResponse.setErrorMessage("Unable to rename " + oldFileName + " to " + newFileName + " in " + sourcePath + " - " + e.getMessage());
    		}
        	stagedRenameFileResponseList.add(stagedRenameFileResponse);
		}
    	
    	stagedRenameResponse.setStagedFiles(stagedRenameFileResponseList);
    	if(hasCompleted && !hasFailures)
    		request.setStatus(Status.completed);
    	else if(!hasCompleted && hasFailures)
    		request.setStatus(Status.failed);
    	else if(hasCompleted && hasFailures)
    		request.setStatus(Status.completed_failures);
		
    	request = requestDao.save(request);
    	int requestId = request.getId();
    	logger.info("Request - " + requestId);
    	stagedRenameResponse.setUserRequestId(requestId);
    	stagedRenameResponse.setStatus(request.getStatus().name());
		return stagedRenameResponse;
	}
    
    public IngestResponse ingest(IngestUserRequest ingestUserRequest){	
    	IngestResponse ingestResponse = new IngestResponse();
    	try{
			String artifactclassId = ingestUserRequest.getArtifactclass();
			Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(artifactclassId);
			String readyToIngestPath =  artifactclass.getPathPrefix();
			Domain domain = artifactclass.getDomain();
			
			List<Integer> actionelementsToBeSkipped = ingestUserRequest.getSkipActionelements();
			
//	    	Request userRequest = new Request();
//	    	userRequest.setType(RequestType.user);
//			userRequest.setActionId(Action.ingest);
//			userRequest.setStatus(Status.queued);
//	    	userRequest.setRequestedBy(getUserObjFromContext());
//			userRequest.setRequestedAt(LocalDateTime.now());
//			
//			RequestDetails details = new RequestDetails();
//			JsonNode postBodyJson = getRequestDetails(ingestUserRequest); 
//			details.setBody(postBodyJson);
//			userRequest.setDetails(details);
//			
//	    	userRequest = requestDao.save(userRequest);
//	    	int userRequestId = userRequest.getId();
//	    	logger.info(DwaraConstants.USER_REQUEST + userRequestId);
			
			Request userRequest = createUserRequest(Action.ingest, ingestUserRequest);
	    	int userRequestId = userRequest.getId();
	    	
	    	// TODO Move it to validation class
	    	boolean isLevel0Pass = true;
	    	List<StagedFileDetails> stagedFileDetailsList = new ArrayList<StagedFileDetails>();
	    	
	    	List<StagedFile> stagedFileList = ingestUserRequest.getStagedFiles();
	    	for (StagedFile stagedFile : stagedFileList) {
				String artifactName = stagedFile.getName();
				if(artifactName.startsWith(DwaraConstants.VIDEO_DIGI_ARTIFACTCLASS_PREFIX)) {
					String path = stagedFile.getPath();// holds something like /data/user/pgurumurthy/ingest/pub-video
					Path mxfFilePath = Paths.get(path, artifactName, "mxf");
					if(!mxfFilePath.toFile().exists()) {
			        	isLevel0Pass = false;
						StagedFileDetails sfd = new StagedFileDetails();
						
						sfd.setPath(path);
						sfd.setName(artifactName);
	
			        	List<org.ishafoundation.dwaraapi.staged.scan.Error> errorList = new ArrayList<org.ishafoundation.dwaraapi.staged.scan.Error>();
	
						Error error = new Error();
						error.setType(Errortype.Error);
						error.setMessage(artifactName + " doesnt have a mxf subfolder in it");
						errorList.add(error);
						
			        	sfd.setErrors(errorList);
						
						stagedFileDetailsList.add(sfd);
			        }
				}
	    	}
	    	
	    	// Validation Level 1 - For empty folders
	    	boolean isLevel1Pass = true;
	    	if(!isLevel0Pass) {
	    		ingestResponse.setStagedFiles(stagedFileDetailsList);
	    		isLevel1Pass = false;
	    	}
	    	else { // Validation Level 2 - Set permissions - Only when level 1 is success we continue...
		    	for (StagedFile stagedFile : stagedFileList) {
					String artifactName = stagedFile.getName();
					String path = stagedFile.getPath();// holds something like /data/user/pgurumurthy/ingest/pub-video
					
					java.io.File nthIngestableFile = new java.io.File(path, artifactName);
					
			        long size = 0;
			        int fileCount = 0;
	
			        org.ishafoundation.dwaraapi.staged.scan.ArtifactFileDetails fd = stagedFileEvaluator.getDetails(nthIngestableFile);
			        size = fd.getTotalSize();
			        fileCount = fd.getCount();
			        
	//		        if(nthIngestableFile.isDirectory()) {
	//			        ArtifactFileDetails fd = junkFilesDealer.getJunkFilesExcludedFileDetails(nthIngestableFile.getAbsolutePath());
	//			        // added to try catch for the test api not to throw an error when looking for a non-existing folder
	//			        try {
	//			        	size = fd.getTotalSize();
	//			        }catch (Exception e) {
	//						// swallowing it...
	//			        	logger.trace("Unable to calculate size for " + nthIngestableFile.getAbsolutePath(), e.getMessage());
	//					}
	//		        
	//		            try {
	//		            	fileCount = fd.getCount();
	//		            }catch (Exception e) {
	//						// swallowing it...
	//		            	logger.trace("Unable to list files for " + nthIngestableFile.getAbsolutePath(), e.getMessage());
	//					}
	//		        }else {
	//		        	size = FileUtils.sizeOf(nthIngestableFile);
	//		        	fileCount = 1;
	//		        }
			        
			        if(fileCount == 0 || size == 0) {
			        	isLevel1Pass = false;
						StagedFileDetails sfd = new StagedFileDetails();
						
						sfd.setPath(path);
						sfd.setName(artifactName);
	
			        	List<org.ishafoundation.dwaraapi.staged.scan.Error> errorList = new ArrayList<org.ishafoundation.dwaraapi.staged.scan.Error>();
			        
			        	if(size == 0) {
							Error error = new Error();
							error.setType(Errortype.Error);
							error.setMessage(artifactName + " size is 0");
							errorList.add(error);
			        	}
			        	
			        	if(fileCount == 0) {
							Error error = new Error();
							error.setType(Errortype.Error);
							error.setMessage(artifactName + " has no files inside");
							errorList.add(error);
				        }
						
			        	sfd.setErrors(errorList);
						
						stagedFileDetailsList.add(sfd);
			        }
		    	}
	    	}
	    	
	    	boolean isLevel2Pass = true;
	    	if(!isLevel1Pass) {
	    		ingestResponse.setStagedFiles(stagedFileDetailsList);
	    		isLevel2Pass = false;
	    	}
	    	else { // Validation Level 2 - Set permissions - Only when level 1 is success we continue...
		    	for (StagedFile stagedFile : stagedFileList) {
		    		
					String artifactName = stagedFile.getName();
					String path = stagedFile.getPath();// holds something like /data/user/pgurumurthy/ingest/pub-video
					Error error = stagedFileOperations.setPermissions(path, artifactName);
					if(error != null) {
						isLevel2Pass = false;
						StagedFileDetails sfd = null;
				    	for (StagedFileDetails stagedFileDetails : stagedFileDetailsList) {
				    		if(stagedFileDetails.getName().equals(artifactName)) {
				    			List<org.ishafoundation.dwaraapi.staged.scan.Error> errorList = stagedFileDetails.getErrors();
				    			if(errorList == null)
				    				errorList = new ArrayList<org.ishafoundation.dwaraapi.staged.scan.Error>();
				    			
								errorList.add(error);
								sfd = stagedFileDetails;
								break;
				    		}
						}
						
				    	if(sfd == null) {
							sfd = new StagedFileDetails();
							
							sfd.setPath(path);
							sfd.setName(artifactName);

							List<org.ishafoundation.dwaraapi.staged.scan.Error> errorList = new ArrayList<org.ishafoundation.dwaraapi.staged.scan.Error>();
							errorList.add(error);

							sfd.setErrors(errorList);
				    	}
						stagedFileDetailsList.add(sfd);
					}
				}
	    	}
	    	
	    	// TODO : If we decide to scan for "Multiple Artifactclass directories" at one shot we might then need to have rollback of moved files if there is a failure... 
	    	// Usecase: If the artifactclass directory is not owned by dwara and dont have write permissions for group, then the move fails. Right now since its just one Artifactclass scan its all or none failure scenario
	    	// If multiple artifactclass scenario comes then there might be artifacts belonging to artifactclasses directory with the right permissions which succeed and some artifacts belonging to artifactclass directory without the right ownership/permissions failing
	    	// so we have to fail the request completely but rollback to its original state i.e. move files back from location B to location A...
	    	// BUT NOT NEEDED RIGHT NOW
	    	boolean isLevel3Pass = true;
	    	if(!isLevel2Pass) {
	    		ingestResponse.setStagedFiles(stagedFileDetailsList);
	    		isLevel3Pass = false;
	    	}
	    	else { // Validation Level 3 - Move file - Only when level 2 is success we continue...
		    	for (StagedFile stagedFile : stagedFileList) {

		    		String stagedFileName = stagedFile.getName();
		    		String stagedFilePath = stagedFile.getPath();
	
		        	// STEP 1 - Moves the file from User's Staging directory to Application's ReadyToIngest directory
		        	java.io.File stagedFileObj = FileUtils.getFile(stagedFilePath, stagedFileName);
		        	java.io.File appReadyToIngestFileObj = FileUtils.getFile(readyToIngestPath, stagedFileName);
		        	try {
		        		moveFile(stagedFileObj, appReadyToIngestFileObj); // NOTE : Here the failure would be for all artifacts in the request or nothing... So rollback etc., needed
			    	} catch (Exception e) {
			    		isLevel3Pass = false;
						
			    		Error error = new Error();
						error.setType(Errortype.Error);
						error.setMessage(e.getMessage());
						
						StagedFileDetails sfd = null;
				    	for (StagedFileDetails stagedFileDetails : stagedFileDetailsList) {
				    		if(stagedFileDetails.getName().equals(stagedFileName)) {
				    			List<org.ishafoundation.dwaraapi.staged.scan.Error> errorList = stagedFileDetails.getErrors();
				    			if(errorList == null)
				    				errorList = new ArrayList<org.ishafoundation.dwaraapi.staged.scan.Error>();
								errorList.add(error);
								sfd = stagedFileDetails;
								break;
				    		}
						}
						
				    	if(sfd == null) {
							sfd = new StagedFileDetails();
							
							sfd.setPath(stagedFilePath);
							sfd.setName(stagedFileName);

							List<org.ishafoundation.dwaraapi.staged.scan.Error> errorList = new ArrayList<org.ishafoundation.dwaraapi.staged.scan.Error>();
							errorList.add(error);

							sfd.setErrors(errorList);
				    	}
						stagedFileDetailsList.add(sfd);
			    	}
				}
	    	}	    	
	    	
	    	if(!isLevel3Pass)
	    		ingestResponse.setStagedFiles(stagedFileDetailsList);
	    	else { // Next steps on Ingest - Only when level 3 validation succeeds...
		    	List<IngestSystemRequest> ingestSystemRequests = new ArrayList<IngestSystemRequest>();
		    	for (StagedFile stagedFile : stagedFileList) {
		    		String stagedFileName = stagedFile.getName();
	
		        	java.io.File appReadyToIngestFileObj = FileUtils.getFile(readyToIngestPath, stagedFileName);
		        	
					Sequence sequence = artifactclass.getSequence();
					String extractedCode = sequenceUtil.getExtractedCode(sequence, stagedFileName);
					
					String sequenceCode = null;
					String prevSeqCode = null;
					String toBeArtifactName = null;
					// NOTE : forcematch is taken care of upfront during scan... No need to act on it...
					if(sequence.isKeepCode() && extractedCode != null) {
						// retaining the same name
						toBeArtifactName = stagedFileName;
					}
					else {
						prevSeqCode = extractedCode;
						sequenceCode = sequenceUtil.getSequenceCode(sequence, stagedFileName);	
						if(extractedCode != null && sequence.isReplaceCode())
							toBeArtifactName = stagedFileName.replace(extractedCode, sequenceCode);
						else
							toBeArtifactName = sequenceCode + "_" + stagedFileName;
					}
			        // Renames the artifact with the needed sequencecode...
					// NOTE : there should be not any error here - Dont have to catch exception and act on it...
			        java.io.File stagedFileInAppReadyToIngest = moveFile(appReadyToIngestFileObj, FileUtils.getFile(readyToIngestPath, toBeArtifactName));  
		    		
		        	// STEP 2 - Moves Junk files
			    	String junkFilesStagedDirName = configuration.getJunkFilesStagedDirName();
			    	int fileCount = 0;
			        long size = 0L;
			        org.ishafoundation.dwaraapi.staged.scan.ArtifactFileDetails afd = stagedFileEvaluator.moveJunkAndGetDetails(stagedFileInAppReadyToIngest);
			        fileCount = afd.getCount();
			        size = afd.getTotalSize();
//			    	if(stagedFileInAppReadyToIngest.isDirectory()) {
//			    		artifactDetails = junkFilesDealer.moveJunkFiles(stagedFileInAppReadyToIngest.getAbsolutePath());
//			    		fileCount = artifactDetails.getCount();
//			        	size = artifactDetails.getTotalSize();
//			    	} else {
//			    		fileCount = 1;
//			        	size = FileUtils.sizeOf(stagedFileInAppReadyToIngest);
//			    	}	
					Request systemrequest = new Request();
					systemrequest.setType(RequestType.system);
					systemrequest.setRequestRef(userRequest);
					systemrequest.setStatus(Status.queued);
					systemrequest.setActionId(userRequest.getActionId());
					systemrequest.setRequestedBy(userRequest.getRequestedBy());
					systemrequest.setRequestedAt(LocalDateTime.now());
		
		    		RequestDetails systemrequestDetails = requestToEntityObjectMapper.getRequestDetailsForIngest(stagedFile);
		    		
		    		// transitioning from global level on the request to artifact level...
		    		systemrequestDetails.setArtifactclassId(artifactclassId); 
		    		systemrequestDetails.setSkipActionelements(actionelementsToBeSkipped);
		    		
					systemrequest.setDetails(systemrequestDetails);
					
					systemrequest = requestDao.save(systemrequest);
					logger.info(DwaraConstants.SYSTEM_REQUEST + systemrequest.getId());
	
			    	Collection<java.io.File> libraryFileAndDirsList = getFileList(stagedFileInAppReadyToIngest, junkFilesStagedDirName);
	
					Artifact artifact = domainUtil.getDomainSpecificArtifactInstance(domain);
					artifact.setWriteRequest(systemrequest);
					artifact.setqLatestRequest(systemrequest);
					artifact.setName(toBeArtifactName);
					artifact.setArtifactclass(artifactclass);
					artifact.setFileCount(fileCount);
					artifact.setTotalSize(size);
					artifact.setSequenceCode(sequenceCode);
					artifact.setPrevSequenceCode(prevSeqCode);
					artifact = (Artifact) domainUtil.getDomainSpecificArtifactRepository(domain).save(artifact);
					
					logger.info(artifact.getClass().getSimpleName() + " - " + artifact.getId());
					
			        createFilesAndExtensions(readyToIngestPath, domain, artifact, size, libraryFileAndDirsList);
					
					jobCreator.createJobs(systemrequest, artifact);
					
					// framing output for response
					IngestSystemRequest ingestSystemRequest = new IngestSystemRequest();
					ingestSystemRequest.setId(systemrequest.getId());
					ingestSystemRequest.setStagedFilePath(systemrequest.getDetails().getStagedFilepath());
					ingestSystemRequest.setSkippedActionElements(actionelementsToBeSkipped);
					int rerunNo = 0; // TODO Hardcoded
					ingestSystemRequest.setRerunNo(rerunNo);
					
					org.ishafoundation.dwaraapi.api.resp.staged.ingest.Artifact artifactForResponse = miscObjectMapper.getArtifactForIngestResponse(artifact);
					artifactForResponse.setArtifactclass(artifactclassId);
					// TODO Needs work - artifactForResponse.setArtifactIdRef(artifactIdRef);
					
					ingestSystemRequest.setArtifact(artifactForResponse);
					ingestSystemRequests.add(ingestSystemRequest);
				}
		    	ingestResponse.setSystemRequests(ingestSystemRequests);
	    	}	
	    	
	    	ingestResponse.setUserRequestId(userRequestId);
	    	ingestResponse.setAction(userRequest.getActionId().name());
	    	ingestResponse.setArtifactclass(artifactclassId);
	    	ingestResponse.setRequestedAt(getDateForUI(userRequest.getRequestedAt()));
	    	ingestResponse.setRequestedBy(userRequest.getRequestedBy().getName());
	    	
    	}
    	catch (Exception e) {
    		logger.error(e.getMessage(), e);
			throw new DwaraException("Unable to ingest - " + e.getMessage(), null);
		}
    	return ingestResponse;
    }

    private java.io.File moveFile(java.io.File src, java.io.File dest) throws Exception {
    	try {
    		Files.move(src.toPath(), dest.toPath(), StandardCopyOption.ATOMIC_MOVE);
//    		if(src.isDirectory())
//    			FileUtils.moveDirectory(src, dest);
//    		else if(src.isFile())
//    			FileUtils.moveFile(src, dest);
		} catch (Exception e) {
			String errorMsg = "Unable to move file " + src + " to " + dest + " - " + e.getMessage();
			logger.error(errorMsg, e);
			throw new Exception(errorMsg);
		}

		return dest;
	}
    
    // made public so tests can access it...
	public void createFilesAndExtensions(String pathPrefix, Domain domain, Artifact artifact, long artifactSize, Collection<java.io.File> libraryFileAndDirsList) throws Exception {
		Set<String> extnsOnArtifactFolder =  new TreeSet<String>();
	    List<File> toBeAddedFileTableEntries = new ArrayList<File>(); 
	    for (Iterator<java.io.File> iterator = libraryFileAndDirsList.iterator(); iterator.hasNext();) {
			
			java.io.File file = (java.io.File) iterator.next();
			String fileName = file.getName();
			String filePath = file.getAbsolutePath();
			filePath = filePath.replace(pathPrefix + java.io.File.separator, ""); // just holding the file path from the artifact folder and not the absolute path.
			logger.trace("filePath - " + filePath);
			File nthFileRowToBeInserted = domainUtil.getDomainSpecificFileInstance(domain);
			if(file.isDirectory())
				nthFileRowToBeInserted.setDirectory(true);
			else
				extnsOnArtifactFolder.add(FilenameUtils.getExtension(fileName)); // assumes there arent any file without extension - Checking and excluding it is the role of junkFilesMover...
			
			nthFileRowToBeInserted.setPathname(filePath);
			fileEntityUtil.setDomainSpecificFileArtifact(nthFileRowToBeInserted, artifact);

			if(fileName.equals(artifact.getName())) // if file is the artifact file itself, set the artifact size calculated upfront...
				nthFileRowToBeInserted.setSize(artifactSize);
			else
				nthFileRowToBeInserted.setSize(FileUtils.sizeOf(file));
			
			toBeAddedFileTableEntries.add(nthFileRowToBeInserted);			
		}
		
	    if(toBeAddedFileTableEntries.size() > 0) {
	    	FileRepository<File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
	    	domainSpecificFileRepository.saveAll(toBeAddedFileTableEntries);
	    	logger.info("File records created successfully");
	    }
	    
	    // Step 1 - get all supported extensions in the system
	    Set<String> alreadySupportedExtensions = extensionsUtil.getAllSupportedExtensions();
	    // getting the not supported extensions by removing already existing extensions
	    extnsOnArtifactFolder.removeAll(alreadySupportedExtensions);
	    Set<Extension> toBeAddedExtensionTableEntries = new HashSet<Extension>();
	    for (String extn : extnsOnArtifactFolder) {
			Extension extension = new Extension();
			extension.setId(extn);
			// NOTE name and description will be set by the admin where he/she chooses to ignore or link it to a filetype
			toBeAddedExtensionTableEntries.add(extension);
		}
	    
	    if(toBeAddedExtensionTableEntries.size() > 0) {
	    	extensionDao.saveAll(toBeAddedExtensionTableEntries);
	    	logger.info("Extension records created successfully");
	    }
	    	
	    
	}
	
    private Collection<java.io.File> getFileList(java.io.File libraryFileInStagingDir, String junkFilesStagedDirName) {
        IOFileFilter dirFilter = null;
        Collection<java.io.File> libraryFileAndDirsList = null;
	    if(libraryFileInStagingDir.isDirectory()) {
			dirFilter = FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(junkFilesStagedDirName, null));
	    	libraryFileAndDirsList = FileUtils.listFilesAndDirs(libraryFileInStagingDir, TrueFileFilter.INSTANCE, dirFilter);
	    }else {
	    	libraryFileAndDirsList = new ArrayList<java.io.File>();
	    	libraryFileAndDirsList.add(libraryFileInStagingDir);
	    }
	    Collections.sort((List<java.io.File>) libraryFileAndDirsList);
	    return libraryFileAndDirsList;
	}

}

