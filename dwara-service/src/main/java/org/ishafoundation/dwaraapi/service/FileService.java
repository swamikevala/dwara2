package org.ishafoundation.dwaraapi.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.req.restore.FileDetails;
import org.ishafoundation.dwaraapi.api.req.restore.PFRestoreUserRequest;
import org.ishafoundation.dwaraapi.api.req.restore.RestoreUserRequest;
import org.ishafoundation.dwaraapi.api.resp.restore.File;
import org.ishafoundation.dwaraapi.api.resp.restore.RestoreResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class FileService extends DwaraService{

	private static final Logger logger = LoggerFactory.getLogger(FileService.class);
	
	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private JobCreator jobCreator;
	
	@Autowired
	private DomainUtil domainUtil;
	
	public List<File> list(List<Integer> fileIds){


    	Map<Integer, org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileId_FileObj_Map = new HashMap<Integer, org.ishafoundation.dwaraapi.db.model.transactional.domain.File>();
    	Map<Integer, Domain> fileId_Domain_Map = new HashMap<Integer, Domain>();
    	validate(fileIds, fileId_FileObj_Map, fileId_Domain_Map);
    	
		List<File> fileList = new ArrayList<File>();
		int counter = 1;
		for (Integer nthFileId : fileIds) {
			org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileFromDB = fileId_FileObj_Map.get(nthFileId);
			
			File file = new File();
			byte[] checksum = fileFromDB.getChecksum();
			if(checksum != null)
				file.setChecksum(Hex.encodeHexString(checksum));
			file.setId(fileFromDB.getId());
			file.setPathname(fileFromDB.getPathname());
			file.setPriority(counter);
			file.setSize(fileFromDB.getSize());
			fileList.add(file);
			counter = counter + 1;
		}
		
		return fileList;
		
	}
	
    public RestoreResponse restore(RestoreUserRequest restoreUserRequest) throws Exception{	
    	RestoreResponse restoreResponse = new RestoreResponse();

    	List<Integer> fileIds = restoreUserRequest.getFileIds();

    	Map<Integer, org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileId_FileObj_Map = new HashMap<Integer, org.ishafoundation.dwaraapi.db.model.transactional.domain.File>();
    	Map<Integer, Domain> fileId_Domain_Map = new HashMap<Integer, Domain>();
    	validate(fileIds, fileId_FileObj_Map, fileId_Domain_Map);
    	
//		Request userRequest = new Request();
//    	userRequest.setType(RequestType.user);
//		userRequest.setActionId(Action.restore);
//		userRequest.setStatus(Status.queued);
//    	User user = getUserObjFromContext();
//    	String requestedBy = user.getName();
//    	userRequest.setRequestedBy(user);
//		userRequest.setRequestedAt(LocalDateTime.now());
//		RequestDetails details = new RequestDetails();
//		JsonNode postBodyJson = getRequestDetails(restoreUserRequest); 
//		details.setBody(postBodyJson);
//		userRequest.setDetails(details);
//		
//    	userRequest = requestDao.save(userRequest);
//    	int userRequestId = userRequest.getId();
//    	logger.info(DwaraConstants.USER_REQUEST + userRequestId);
    	Request userRequest = createUserRequest(Action.restore, restoreUserRequest);
    	int userRequestId = userRequest.getId();
	
    	Integer copyNumber = restoreUserRequest.getCopy();
    	String outputFolder = restoreUserRequest.getOutputFolder();
    	String destinationPath = restoreUserRequest.getDestinationPath();
    	boolean verify = restoreUserRequest.isVerify();
    	List<File> files = new ArrayList<File>();
    	

    	int counter = 1;
    	for (Integer nthFileId : fileIds) {
    		
    		org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileFromDB = fileId_FileObj_Map.get(nthFileId);
    			
			Request systemRequest = new Request();
			systemRequest.setType(RequestType.system);
			systemRequest.setStatus(Status.queued);
			systemRequest.setRequestRef(userRequest);
			systemRequest.setActionId(userRequest.getActionId());
			systemRequest.setRequestedBy(userRequest.getRequestedBy());
			systemRequest.setRequestedAt(LocalDateTime.now());
			
    		RequestDetails systemrequestDetails = new RequestDetails();
    		systemrequestDetails.setFileId(nthFileId);
    		systemrequestDetails.setCopyId(copyNumber);
			systemrequestDetails.setOutputFolder(outputFolder);
			systemrequestDetails.setDestinationPath(destinationPath);
			systemrequestDetails.setVerify(verify); // overwriting default archiveformat.verify during restore
			systemrequestDetails.setDomainId(domainUtil.getDomainId(fileId_Domain_Map.get(nthFileId)));
			
			systemRequest.setDetails(systemrequestDetails);
			systemRequest = requestDao.save(systemRequest);
			logger.info(DwaraConstants.SYSTEM_REQUEST + systemRequest.getId());
			
			
			jobCreator.createJobs(systemRequest, null);
			
			File file = new File();
			//file.setArtifactclass(artifactclass);
			
			byte[] checksum = fileFromDB.getChecksum();
			if(checksum != null)
				file.setChecksum(Hex.encodeHexString(checksum));
			file.setId(fileFromDB.getId());
			file.setPathname(fileFromDB.getPathname());
			file.setPriority(counter);
			file.setSize(fileFromDB.getSize());
			file.setSystemRequestId(systemRequest.getId());
			files.add(file);
			counter = counter + 1;
		}

    	
    	restoreResponse.setUserRequestId(userRequestId);
    	restoreResponse.setAction(userRequest.getActionId().name());
    	restoreResponse.setRequestedAt(getDateForUI(userRequest.getRequestedAt()));
    	restoreResponse.setRequestedBy(userRequest.getRequestedBy().getName());

    	restoreResponse.setDestinationPath(destinationPath);
    	restoreResponse.setVerify(verify);
    	restoreResponse.setOutputFolder(outputFolder);
    	
    	restoreResponse.setFiles(files);    	
    	return restoreResponse;
    }
    
    public RestoreResponse partialFileRestore(PFRestoreUserRequest pfRestoreUserRequest) throws Exception{	
    	RestoreResponse restoreResponse = new RestoreResponse();

    	List<Integer> fileIds = new ArrayList<Integer>();
    	List<FileDetails> fileDetailsList = pfRestoreUserRequest.getFiles();
		for (FileDetails nthFileDetails : fileDetailsList) {
			fileIds.add(nthFileDetails.getFileId());
		}
    	Map<Integer, org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileId_FileObj_Map = new HashMap<Integer, org.ishafoundation.dwaraapi.db.model.transactional.domain.File>();
    	Map<Integer, Domain> fileId_Domain_Map = new HashMap<Integer, Domain>();
    	validate(fileIds, fileId_FileObj_Map, fileId_Domain_Map);
    	
//		Request userRequest = new Request();
//    	userRequest.setType(RequestType.user);
//		userRequest.setActionId(Action.restore);
//		userRequest.setStatus(Status.queued);
//    	User user = getUserObjFromContext();
//    	String requestedBy = user.getName();
//    	userRequest.setRequestedBy(user);
//		userRequest.setRequestedAt(LocalDateTime.now());
//		RequestDetails details = new RequestDetails();
//		JsonNode postBodyJson = getRequestDetails(pfRestoreUserRequest); 
//		details.setBody(postBodyJson);
//		userRequest.setDetails(details);
//		
//    	userRequest = requestDao.save(userRequest);
//    	int userRequestId = userRequest.getId();
//    	logger.info(DwaraConstants.USER_REQUEST + userRequestId);
    	
    	Request userRequest = createUserRequest(Action.restore, pfRestoreUserRequest);
    	int userRequestId = userRequest.getId();
	
    	Integer copyNumber = pfRestoreUserRequest.getCopy();
    	String outputFolder = pfRestoreUserRequest.getOutputFolder();
    	String destinationPath = pfRestoreUserRequest.getDestinationPath();
    	boolean verify = pfRestoreUserRequest.isVerify();
    	List<File> files = new ArrayList<File>();
    	

    	int counter = 1;
    	for (FileDetails nthFileDetails : fileDetailsList) {
    		Integer nthFileId = nthFileDetails.getFileId();
    		org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileFromDB = fileId_FileObj_Map.get(nthFileId);
    			
			Request systemRequest = new Request();
			systemRequest.setType(RequestType.system);
			systemRequest.setStatus(Status.queued);
			systemRequest.setRequestRef(userRequest);
			systemRequest.setActionId(userRequest.getActionId());
			systemRequest.setRequestedBy(userRequest.getRequestedBy());
			systemRequest.setRequestedAt(LocalDateTime.now());
			
    		RequestDetails systemrequestDetails = new RequestDetails();
    		systemrequestDetails.setFileId(nthFileId);
    		systemrequestDetails.setCopyId(copyNumber);
			systemrequestDetails.setOutputFolder(outputFolder);
			systemrequestDetails.setDestinationPath(destinationPath);
			systemrequestDetails.setVerify(verify); // overwriting default archiveformat.verify during restore
			systemrequestDetails.setDomainId(domainUtil.getDomainId(fileId_Domain_Map.get(nthFileId)));
			systemrequestDetails.setTimecodeStart(nthFileDetails.getTimecodeStart());
			systemrequestDetails.setTimecodeEnd(nthFileDetails.getTimecodeEnd());
			
			systemRequest.setDetails(systemrequestDetails);
			systemRequest = requestDao.save(systemRequest);
			logger.info(DwaraConstants.SYSTEM_REQUEST + systemRequest.getId());
			
			
			jobCreator.createJobs(systemRequest, null);
			
			File file = new File();
			//file.setArtifactclass(artifactclass);
			
			byte[] checksum = fileFromDB.getChecksum();
			if(checksum != null)
				file.setChecksum(Hex.encodeHexString(checksum));
			file.setId(fileFromDB.getId());
			file.setPathname(fileFromDB.getPathname());
			file.setPriority(counter);
			file.setSize(fileFromDB.getSize());
			file.setSystemRequestId(systemRequest.getId());
			files.add(file);
			counter = counter + 1;
		}

    	
    	restoreResponse.setUserRequestId(userRequestId);
    	restoreResponse.setAction(userRequest.getActionId().name());
    	restoreResponse.setRequestedAt(getDateForUI(userRequest.getRequestedAt()));
    	restoreResponse.setRequestedBy(userRequest.getRequestedBy().getName());

    	restoreResponse.setDestinationPath(destinationPath);
    	restoreResponse.setVerify(verify);
    	restoreResponse.setOutputFolder(outputFolder);
    	
    	restoreResponse.setFiles(files);    	
    	return restoreResponse;
    }


    private void validate(List<Integer> fileIds, Map<Integer, org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileId_FileObj_Map, Map<Integer, Domain> fileId_Domain_Map) {
    	Domain[] domains = Domain.values();
    	List<String> errorFileList = new ArrayList<String>();
    	boolean hasErrors = false;
    	for (Integer nthFileId : fileIds) {
    		org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileFromDB = null;
    		
    		for (Domain nthDomain : domains) {
    			fileFromDB = domainUtil.getDomainSpecificFile(nthDomain, nthFileId);
    			if(fileFromDB != null) {
    				fileId_FileObj_Map.put(nthFileId, fileFromDB);
    				fileId_Domain_Map.put(nthFileId, nthDomain);
    				break;
    			}
			}
    		
    		if(fileFromDB == null) {
    			errorFileList.add(nthFileId + " is invalid");
    			hasErrors = true;
    		}
    	}
    	
    	if(hasErrors) {
    		ObjectMapper mapper = new ObjectMapper(); 
    		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    		JsonNode jsonNode = mapper.valueToTree(errorFileList);
    		throw new DwaraException("Files not in system requested...", jsonNode);
    	}
    }
}

