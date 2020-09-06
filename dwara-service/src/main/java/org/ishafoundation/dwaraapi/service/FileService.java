package org.ishafoundation.dwaraapi.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.ishafoundation.dwaraapi.api.req.restore.RestoreUserRequest;
import org.ishafoundation.dwaraapi.api.resp.restore.File;
import org.ishafoundation.dwaraapi.api.resp.restore.RestoreResponse;
import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.DomainAttributeConverter;
import org.ishafoundation.dwaraapi.db.dao.master.DomainDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class FileService extends DwaraService{

	private static final Logger logger = LoggerFactory.getLogger(FileService.class);
	
	@Autowired
	protected DomainDao domainDao;
	
	@Autowired
	protected RequestDao requestDao;
	
	@Autowired
	protected JobCreator jobCreator;
	
	@Autowired
	protected DomainUtil domainUtil;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@Autowired
	private DomainAttributeConverter domainAttributeConverter;

	public List<File> list(List<Integer> fileIds){
		List<File> fileList = new ArrayList<File>();
		int counter = 1;
		Domain domain = null;
//		if(restoreUserRequest.getDomain() != null)
//			domain = domainAttributeConverter.convertToEntityAttribute(restoreUserRequest.getDomain()+"");
//		else {
			org.ishafoundation.dwaraapi.db.model.master.configuration.Domain domainFromDB = domainDao.findByDefaultTrue();
			domain = domainAttributeConverter.convertToEntityAttribute(domainFromDB.getId()+"");
//		}

		for (Integer nthFileId : fileIds) {
			org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileFromDB = domainUtil.getDomainSpecificFile(domain, nthFileId);
			
			File file = new File();
			byte[] checksum = fileFromDB.getChecksum();
			if(checksum != null)
				file.setChecksum(Hex.encodeHexString(checksum));
			
			//file.setChecksumType(fileFromDB.getChecksum());
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
		    	
		Request userRequest = new Request();
    	userRequest.setType(RequestType.user);
		userRequest.setActionId(Action.restore);
		userRequest.setStatus(Status.queued);
    	User user = getUserObjFromContext();
    	String requestedBy = user.getName();
    	userRequest.setRequestedBy(user);
		userRequest.setRequestedAt(LocalDateTime.now());
		Domain domain = domainUtil.getDomain(restoreUserRequest.getDomain());
		userRequest.setDomain(domain);
		RequestDetails details = new RequestDetails();
		JsonNode postBodyJson = getRequestDetails(restoreUserRequest); 
		details.setBody(postBodyJson);
		userRequest.setDetails(details);
		
    	userRequest = requestDao.save(userRequest);
    	int userRequestId = userRequest.getId();
    	logger.info("Request - " + userRequestId);

	
    	Integer copyNumber = restoreUserRequest.getCopy();
    	String outputFolder = restoreUserRequest.getOutputFolder();
    	String destinationPath = restoreUserRequest.getDestinationPath();
    	boolean verify = restoreUserRequest.isVerify();
    	List<File> files = new ArrayList<File>();
    	
    	List<Integer> fileIds = restoreUserRequest.getFileIds();
    	int counter = 1;
    	for (Integer nthFileId : fileIds) {
    		org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileFromDB = domainUtil.getDomainSpecificFile(domain, nthFileId);
    		
			Request systemRequest = new Request();
			systemRequest.setType(RequestType.system);
			systemRequest.setStatus(Status.queued);
			systemRequest.setRequestRef(userRequest);
			systemRequest.setActionId(userRequest.getActionId());
			systemRequest.setRequestedBy(userRequest.getRequestedBy());
			systemRequest.setRequestedAt(LocalDateTime.now());
			systemRequest.setDomain(userRequest.getDomain());

    		RequestDetails systemrequestDetails = new RequestDetails();
    		systemrequestDetails.setFileId(nthFileId);
    		
    		systemrequestDetails.setCopyId(copyNumber);
			systemrequestDetails.setOutputFolder(outputFolder);
			systemrequestDetails.setDestinationPath(destinationPath);
			systemrequestDetails.setVerify(verify); // overwriting default archiveformat.verify during restore

			systemRequest.setDetails(systemrequestDetails);
			systemRequest = requestDao.save(systemRequest);
			logger.info("System request - " + systemRequest.getId());
			
			
			jobCreator.createJobs(systemRequest, null);
			
			File file = new File();
			//file.setArtifactclass(artifactclass);
			
			byte[] checksum = fileFromDB.getChecksum();
			if(checksum != null)
				file.setChecksum(Hex.encodeHexString(checksum));
			
			//file.setChecksumType(fileFromDB.getChecksum());
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
	
	// TODO - Unnecessary conversion happening...
	private JsonNode getRequestDetails(RestoreUserRequest userRequest) {
		JsonNode postBodyJson = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			String postBody = mapper.writeValueAsString(userRequest);
			postBodyJson = mapper.readValue(postBody, JsonNode.class);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return postBodyJson;
	}
}

