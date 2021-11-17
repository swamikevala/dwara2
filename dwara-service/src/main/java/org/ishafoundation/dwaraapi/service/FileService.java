package org.ishafoundation.dwaraapi.service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.codec.binary.Hex;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.req.restore.FileDetails;
import org.ishafoundation.dwaraapi.api.req.restore.PFRestoreUserRequest;
import org.ishafoundation.dwaraapi.api.req.restore.RestoreUserRequest;
import org.ishafoundation.dwaraapi.api.resp.restore.File;
import org.ishafoundation.dwaraapi.api.resp.restore.RestoreResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileEntityUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TTFileJobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TTFileJob;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.ArtifactVolumeStatus;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.ishafoundation.dwaraapi.storage.storagetask.Restore;
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
	
    @PersistenceContext
    private EntityManager entityManager;
    
	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private JobCreator jobCreator;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private FileEntityUtil fileEntityUtil;
	
	@Autowired
	private TTFileJobDao ttFileJobDao;
	
	@Autowired
	private TFileDao tfileDao; 
	
	@Autowired
	private Restore restoreStorageTask;

	
	@Deprecated
//	public List<File> list(List<Integer> fileIds){
//
//
//    	Map<Integer, org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileId_FileObj_Map = new HashMap<Integer, org.ishafoundation.dwaraapi.db.model.transactional.domain.File>();
//    	Map<Integer, Domain> fileId_Domain_Map = new HashMap<Integer, Domain>();
//    	validate(fileIds, fileId_FileObj_Map, fileId_Domain_Map);
//    	
//		List<File> fileList = new ArrayList<File>();
//		int counter = 1;
//		for (Integer nthFileId : fileIds) {
//			org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileFromDB = fileId_FileObj_Map.get(nthFileId);
//			
//			File file = new File();
//			byte[] checksum = fileFromDB.getChecksum();
//			if(checksum != null)
//				file.setChecksum(Hex.encodeHexString(checksum));
//			file.setId(fileFromDB.getId());
//			file.setPathname(fileFromDB.getPathname());
//			file.setPriority(counter);
//			file.setSize(fileFromDB.getSize());
//			fileList.add(file);
//			counter = counter + 1;
//		}
//		
//		return fileList;
//		
//	}

	/**
	 * join vs subquery
	 * 
	 * joins take longer to respond when there is more resultset
	 * select file1.id, artifact1.name from artifact1 join file1 on artifact1.id = file1.artifact_id where artifact1.name like '%Sadhguru%Instagram%02-Apr-2021%' and artifact1.artifactclass_id not like '%proxy-low' and file1.pathname = artifact1.name;
	 * when the order of the searchstr is expected to be haphazard
	 * select file1.id, artifact1.name from artifact1 join file1 on artifact1.id = file1.artifact_id where artifact1.name like '%02-Apr-2021%' and artifact1.name like '%Sadhguru%' and artifact1.artifactclass_id not like '%proxy-low' and file1.pathname = artifact1.name;
	 * 
	 * subqueries take longer when the searchstr is complicated
	 * select id, pathname from file1 where pathname in (select name from artifact1 where name like '%Sadhguru%Instagram%02-Apr-2021%' and artifactclass_id not like '%proxy-low');
	 * 
	 * No specific reason but will go for joins
	 * 
	 * @param spaceSeparatedArtifactSearchString - eg. something like '02-Apr-2021 Sadhguru Instagram'
	 * @return
	 */
	public List<File> listV2(String spaceSeparatedArtifactSearchString){
		String[] searchParts = spaceSeparatedArtifactSearchString.split(" ");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < searchParts.length; i++) {
			if(i > 0)
				sb.append(" and ");
			sb.append("artifact1.name like '%");
			sb.append(searchParts[i]);
			sb.append("%'");
		}
		
		//String query="select file1.id, file1.pathname, file1.size from artifact1 join file1 on artifact1.id = file1.artifact_id where " + sb.toString() + " and artifact1.artifactclass_id not like '%proxy-low' and file1.pathname = artifact1.name";
		String query="select file1.id, file1.pathname, file1.size, artifact1.id as artifactId, artifact1.artifactclass_id from artifact1 join file1 on artifact1.id = file1.artifact_id where " + sb.toString() + " and artifact1.artifactclass_id not like '%proxy-low' and file1.pathname = artifact1.name";
		Query q = entityManager.createNativeQuery(query);
        List<Object[]> results = q.getResultList();
        List<File> list = new ArrayList<File>();
        results.stream().forEach((record) -> {
            int fileId = ((Integer) record[0]).intValue();
            String artifactName = (String) record[1];
            long size = ((BigInteger)record[2]).longValue();
            int artifactId = ((Integer) record[3]).intValue();
            String artifactclass = (String) record[4];
			File file = new File();
			file.setId(fileId);
			file.setPathname(artifactName);
			file.setSize(size);
			file.setArtifactId(artifactId);
			file.setArtifactclass(artifactclass);
			list.add(file);
        });
		
		return list;
	}
	
    public RestoreResponse restore(RestoreUserRequest restoreUserRequest, Action action, String flow ) throws Exception{
    	RestoreResponse restoreResponse = new RestoreResponse();

    	List<Integer> fileIds = restoreUserRequest.getFileIds();
    	Integer copyNumber = restoreUserRequest.getCopy();
    	String outputFolder = restoreUserRequest.getOutputFolder();
    	String destinationPath = restoreUserRequest.getDestinationPath();
    	
    	if(fileIds.size() == 0)
    		   		throw new Exception("Invalid request.  No File Id passed");
    	Map<Integer, org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileId_FileObj_Map = new HashMap<Integer, org.ishafoundation.dwaraapi.db.model.transactional.domain.File>();
    	Map<Integer, Domain> fileId_Domain_Map = new HashMap<Integer, Domain>();
    	validate(fileIds, copyNumber, destinationPath, outputFolder, fileId_FileObj_Map, fileId_Domain_Map);
    	
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
    	Request userRequest = createUserRequest(action, restoreUserRequest);
    	int userRequestId = userRequest.getId();

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
			systemrequestDetails.setDomainId(domainUtil.getDomainId(fileId_Domain_Map.get(nthFileId)));
			systemrequestDetails.setFlowId(flow);
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
    	restoreResponse.setFlow(flow);
    	restoreResponse.setOutputFolder(outputFolder);
    	
    	restoreResponse.setFiles(files);    	
    	return restoreResponse;
    }
    //overload for scheduler call
	public RestoreResponse restore(RestoreUserRequest restoreUserRequest, Action action, String flow , User user ) throws Exception{
		RestoreResponse restoreResponse = new RestoreResponse();

		List<Integer> fileIds = restoreUserRequest.getFileIds();
		Integer copyNumber = restoreUserRequest.getCopy();
		String outputFolder = restoreUserRequest.getOutputFolder();
		String destinationPath = restoreUserRequest.getDestinationPath();

		if(fileIds.size() == 0)
			throw new Exception("Invalid request.  No File Id passed");
		Map<Integer, org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileId_FileObj_Map = new HashMap<Integer, org.ishafoundation.dwaraapi.db.model.transactional.domain.File>();
		Map<Integer, Domain> fileId_Domain_Map = new HashMap<Integer, Domain>();
		validate(fileIds, copyNumber, destinationPath, outputFolder, fileId_FileObj_Map, fileId_Domain_Map);

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
		Request userRequest = createUserRequest(action, restoreUserRequest ,  user);
		int userRequestId = userRequest.getId();

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
			systemrequestDetails.setDomainId(domainUtil.getDomainId(fileId_Domain_Map.get(nthFileId)));
			systemrequestDetails.setFlowId(flow);
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
		restoreResponse.setFlow(flow);
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
    	validate(fileIds, pfRestoreUserRequest.getCopy(), pfRestoreUserRequest.getDestinationPath(), pfRestoreUserRequest.getOutputFolder(), fileId_FileObj_Map, fileId_Domain_Map);
    	
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
    	restoreResponse.setOutputFolder(outputFolder);
    	
    	restoreResponse.setFiles(files);    	
    	return restoreResponse;
    }
    
    private void validate(List<Integer> fileIds, int copyNumber, String destinationPath, String outputFolder, Map<Integer, org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileId_FileObj_Map, Map<Integer, Domain> fileId_Domain_Map) {
    	Domain[] domains = Domain.values();
    	List<String> requestedDirectoriesPathNameList = new ArrayList<String>();
    	List<String> errorFileList = new ArrayList<String>();
    	boolean hasErrors = false;
    	
    	ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(Domain.ONE);
    	
    	// if any requested file is a directory and if any other file or directory requested has the same conflicting path throw error... 
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
    		
    		// Getting the artifactname from artifactvolume as the artifact could have been softRenamed...
    		String artifactName = null;
    		Artifact artifact;
			try {
				artifact = fileEntityUtil.getArtifact(fileFromDB, Domain.ONE);
				ArtifactVolume alreadyExistingArtifactVolume = domainSpecificArtifactVolumeRepository.findByIdArtifactIdAndVolumeGroupRefCopyIdAndStatus(artifact.getId(), copyNumber, ArtifactVolumeStatus.current);
				if(alreadyExistingArtifactVolume != null)
					artifactName = alreadyExistingArtifactVolume.getName();

				String restoreLocation = destinationPath + java.io.File.separator + outputFolder + java.io.File.separator + artifactName;
				java.io.File javaIO = new java.io.File(restoreLocation);
				
				if(javaIO.exists()) {
					errorFileList.add(nthFileId + " is already restored in " + restoreLocation);
					hasErrors = true;
				}
			} catch (Exception e) {
				logger.warn("Unable to validate if artifact already restored or not " + nthFileId);
			}
			
    		String filePathname = fileFromDB.getPathname();
    		for (String alreadyRequestedDirectoriesPathName : requestedDirectoriesPathNameList) {
    			if(filePathname.startsWith(alreadyRequestedDirectoriesPathName)) {
        			errorFileList.add(nthFileId + " is a file of the already requested directory " + alreadyRequestedDirectoriesPathName + ". Please remove it from the request");
        			hasErrors = true;
    			}
			}
    		
    		if(fileFromDB.isDirectory())// StringUtils.isBlank(FilenameUtils.getExtension(filePathname))) // if the file's is a directory - NOTE (checksum could be blank for files too if they are imported...)
    			requestedDirectoriesPathNameList.add(filePathname);
    	}
    	
    	if(hasErrors) {
    		ObjectMapper mapper = new ObjectMapper(); 
    		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    		JsonNode jsonNode = mapper.valueToTree(errorFileList);
    		throw new DwaraException("Validation failed for file(s) to be restored ...", jsonNode);
    	}
    }
    
    public void deleteFile(int fileId){
    	Domain[] domains = Domain.values();
		for (Domain nthDomain : domains) {
			org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileFromDB = domainUtil.getDomainSpecificFile(nthDomain, fileId);
			if(fileFromDB != null) {
				fileFromDB.setDeleted(true);
		    	FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(nthDomain);
		    	domainSpecificFileRepository.save(fileFromDB);
				break;
			}
		}
    }
    public void markBad(int fileId , String reason , boolean dealWithJob) throws Exception{
    	Domain[] domains = Domain.values();
    	HashMap<String, Object> data = new HashMap<String, Object>();
    	
    	
		Request userRequest = null;
		data.put("fileId", fileId);
		data.put("bad", true);
		data.put("reason",reason);
		userRequest = createUserRequest(Action.mark_corrupted, data);
		org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileFromDB = null;
		TTFileJob ttFileJob = null;
		Domain domain = null;

    	
    	for (Domain nthDomain : domains) {
						
			
			 fileFromDB = domainUtil.getDomainSpecificFile(nthDomain, fileId);
			if(fileFromDB != null) {
				domain=nthDomain;
		    	break;
			}
		}

		fileFromDB.setBad(true);
		fileFromDB.setReason(reason);
		TFile tfile =  tfileDao.findById(fileId).get();
		tfile.setBad(true);
		tfile.setReason(reason);
		tfileDao.save(tfile);
    	FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
    	domainSpecificFileRepository.save(fileFromDB);
		
    	if( dealWithJob) {
    	List<TTFileJob> ttFileJobs = ttFileJobDao.findAllByIdFileId(fileId);
		if(ttFileJobs.size()==1) {
			ttFileJob = ttFileJobs.get(0);
			int jobID = ttFileJob.getJob().getId();
			ttFileJobDao.delete(ttFileJob);
			
			List<TTFileJob> ttFileJobswithId = ttFileJobDao.findAllByJobId(jobID);
			boolean delete = true;
			for(TTFileJob ttfileJob : ttFileJobswithId ) {
				if(ttfileJob.getStatus()!=Status.completed) {
					delete=false;
				}
			}
			if(delete ) {
				ttFileJobDao.deleteAll(ttFileJobswithId);
			}
		}
		
		else {
			
			throw new Exception("Multiple jobs references the file "+fileId+". Not able to deal with t_file_job and job. Please do it manually");
		}
    	
    	}
	
    	
    	userRequest.setStatus(Status.completed);
		userRequest = requestDao.save(userRequest);
    }

}

