package org.ishafoundation.dwaraapi.service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.resp.artifact.ArtifactResponse;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ArtifactclassVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepositoryUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
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
import org.ishafoundation.dwaraapi.service.common.ArtifactDeleter;
import org.ishafoundation.dwaraapi.staged.scan.Error;
import org.ishafoundation.dwaraapi.staged.scan.StagedFileEvaluator;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.ishafoundation.videopub.mam.MamUpdateTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArtifactService extends DwaraService{

	private static final Logger logger = LoggerFactory.getLogger(ArtifactService.class);

	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private TFileDao tFileDao;

	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private ArtifactclassVolumeDao artifactclassVolumeDao;

	@Autowired
	private DomainUtil domainUtil;

	@Autowired
	private MiscObjectMapper miscObjectMapper; 

	@Autowired
	private ArtifactDeleter artifactDeleter;
	
	@Autowired
    private StagedFileEvaluator stagedFileEvaluator;

	@Autowired
	private FileRepositoryUtil fileRepositoryUtil;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@Autowired
	private SequenceUtil sequenceUtil;
	
	@Autowired
	private MamUpdateTaskExecutor mamUpdateTaskExecutor;
	
	@Autowired
	private JobCreator jobCreator;

	public ArtifactResponse deleteArtifact(int artifactId) throws Exception{
		ArtifactRepository<Artifact> artifactRepository = null;
		Artifact artifact = null; // get the artifact details from DB
		Domain domain = null; 
		Domain[] domains = Domain.values();
		for (Domain nthDomain : domains) {
			artifactRepository = domainUtil.getDomainSpecificArtifactRepository(nthDomain);
			Optional<Artifact> artifactEntity = artifactRepository.findById(artifactId);
			if(artifactEntity.isPresent()) {
				artifact = artifactEntity.get();
				domain = nthDomain;
				break;
			}
		}

		artifactDeleter.validateArtifactclass(artifact.getArtifactclass().getId());

		Request request = artifact.getWriteRequest();//artifact.getqLatestRequest();

		artifactDeleter.validateRequest(request);

		artifactDeleter.validateJobsAndUpdateStatus(request);

		// Step 1 - Create User Request
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("artifactId", artifactId);
		Request userRequest = createUserRequest(Action.delete, Status.in_progress, data);

		artifactDeleter.cleanUp(userRequest, request,  domain, artifactRepository);

		userRequest.setStatus(Status.completed);
		requestDao.save(userRequest);
		logger.info(userRequest.getId() + " - Completed");

		ArtifactResponse dr = new ArtifactResponse();
		org.ishafoundation.dwaraapi.api.resp.artifact.Artifact artifactForResponse = miscObjectMapper.getArtifactForArtifactResponse(artifact);
		dr.setArtifact(artifactForResponse);
		dr.setUserRequestId(userRequest.getId());
		dr.setAction(Action.delete.name());
		dr.setRequestedAt(getDateForUI(userRequest.getRequestedAt()));
		dr.setRequestedBy(userRequest.getRequestedBy().getName());

		return dr;
	}

	public ArtifactResponse softRenameArtifact(int artifactId, String newName, Boolean force) throws Exception {	
		List<Error> errorList = stagedFileEvaluator.validateName(newName);
		if (errorList.size() > 0) {
			throw new Exception("Name validation failed!");
		};
			
		// 2. Change the artifact name in the artifact folder and artifact table/ file table entry for the artifact
		ArtifactRepository<Artifact> artifactRepository = null;
		Artifact artifactToRenameActualRow = null; // get the artifact details from DB
		Domain domain = null; 
		Domain[] domains = Domain.values();
		for (Domain nthDomain : domains) {
			artifactRepository = domainUtil.getDomainSpecificArtifactRepository(nthDomain);
			Optional<Artifact> artifactEntity = artifactRepository.findById(artifactId);
			if(artifactEntity.isPresent()) {
				artifactToRenameActualRow = artifactEntity.get();
				domain = nthDomain;
				break;
			}
		}

		// 2 (a) ------ Artifact level change
		// Check if the artifact id exists 
		// If artifact ID is null return error and escape into the unknown
		if (artifactToRenameActualRow == null) { 
			throw new Exception("Artifact doesnt exist!");
		}
			
		// Check if the system request is completed - only allow softrename on completed requests - NOTE with force option even when the request is not completed we allow softrename 
		Request systemRequest = artifactToRenameActualRow.getWriteRequest();
		Status systemRequestStatus =  systemRequest.getStatus();
		if (!force && systemRequestStatus != Status.completed && systemRequestStatus != Status.marked_completed) {
			throw new Exception("System request " + systemRequest.getId() + " not yet completed");
		}

		List<Job> jobList = jobDao.findAllByRequestId(systemRequest.getId());
		boolean queued = false;
		boolean inProgress = false;
		for (Job job : jobList) {
			Status status = job.getStatus();
			if(status == Status.queued) {
				queued = true;
				break;
			}
			else if(status == Status.in_progress) {
				inProgress = true;
				break;
			}
		}
		
		if (force && (queued || inProgress)) {
			throw new Exception("System request " + systemRequest.getId() + " has jobs in running state. So can't proceed further");
		}
		String artifactName = artifactToRenameActualRow.getName();
		String sequenceId = artifactToRenameActualRow.getSequenceCode();
		String artifactNewName = sequenceId + "_" + newName; 
	
		// Step 1 - Create User Request
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("artifactId", artifactId);
		data.put("artifactName",artifactName);
		data.put("artifactNewName",artifactNewName);
		
		Request userRequest = createUserRequest(Action.rename, Status.in_progress, data);
		
    	List<Artifact> artifactList = artifactRepository.findAllByWriteRequestId(systemRequest.getId());
    	for (Artifact artifact : artifactList) {
    		sequenceId = artifact.getSequenceCode();
    		artifactName = artifact.getName();
    		artifactNewName = sequenceId + "_" + newName; 
			
    		// 2 (a) (i) Change the artifact name entry in artifact table
    		artifact.setName(artifactNewName);
			// Save Artifact first
			try {
				artifactRepository.save(artifact);
			}
			catch (Exception e) {
				userRequest.setStatus(Status.failed);
				requestDao.save(userRequest);
				throw new Exception("Artifact Table rename failed " + e.getMessage());
			}
				
			// 2 (a) (ii) Change the File Table entries and the file names 
			// Step 4 - Flag all the file entries as soft-deleted
			String parentFolderReplaceRegex = "^"+artifactName; 
			List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList = fileRepositoryUtil.getAllArtifactFileList(artifact, domain);			
			for (org.ishafoundation.dwaraapi.db.model.transactional.domain.File eachfile : artifactFileList) { 
				// Getting the filename
				String filepath = eachfile.getPathname();
				// Change the parent folder name by replacing the older artifact name by newer name
				String correctedFilePathForArtifactFile = filepath.replaceAll(parentFolderReplaceRegex, artifactNewName); 
				eachfile.setPathname(correctedFilePathForArtifactFile);
				byte[] filePathChecksum = ChecksumUtil.getChecksum(correctedFilePathForArtifactFile);
				eachfile.setPathnameChecksum(filePathChecksum);
	
			} // File entry manipulation and renaming ends here 
				
			List<TFile> artifactTFileList  = tFileDao.findAllByArtifactId(artifact.getId()); // Including the Deleted Ones
			// TODO : what happens if Tfile records are purged...
			for (TFile nthTFile : artifactTFileList) {
				String filepath = nthTFile.getPathname();
				// Change the parent folder name by replacing the older artifact name by newer name
				String correctedFilePathForArtifactFile = filepath.replaceAll(parentFolderReplaceRegex, artifactNewName); 
				nthTFile.setPathname(correctedFilePathForArtifactFile);	
				byte[] filePathChecksum = ChecksumUtil.getChecksum(correctedFilePathForArtifactFile);
				nthTFile.setPathnameChecksum(filePathChecksum);
			}
				
			FileRepository<File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
			// Update the file table
			try {
				domainSpecificFileRepository.saveAll(artifactFileList);
				tFileDao.saveAll(artifactTFileList);
			}
			catch (Exception e) {
				userRequest.setStatus(Status.failed);
				requestDao.save(userRequest);
				throw new Exception("File Table rename failed " + e.getMessage());
			}
			
			if(!artifact.getArtifactclass().getSource())
				mamUpdateTaskExecutor.rename(artifactName, artifactNewName, artifact.getArtifactclass().getCategory());
    	}
    	
    	
    	
		userRequest.setStatus(Status.completed);
		requestDao.save(userRequest);

		// Return response
		ArtifactResponse dr = new ArtifactResponse();
		org.ishafoundation.dwaraapi.api.resp.artifact.Artifact artifactForResponse = miscObjectMapper.getArtifactForArtifactResponse(artifactToRenameActualRow);
		dr.setArtifact(artifactForResponse);
		dr.setUserRequestId(userRequest.getId());
		dr.setAction(Action.rename.name());
		dr.setRequestedAt(getDateForUI(userRequest.getRequestedAt()));
		dr.setRequestedBy(userRequest.getRequestedBy().getName());

		return dr;
	}

	public ArtifactResponse rewriteArtifact(int artifactId, int rewriteCopy, int goodCopy) throws Exception {
		
		ArtifactRepository<Artifact> artifactRepository = null;
		Artifact artifact = null; // get the artifact details from DB
		Domain domain = null; 
		Domain[] domains = Domain.values();
		for (Domain nthDomain : domains) {
			artifactRepository = domainUtil.getDomainSpecificArtifactRepository(nthDomain);
			Optional<Artifact> artifactEntity = artifactRepository.findById(artifactId);
			if(artifactEntity.isPresent()) {
				artifact = artifactEntity.get();
				if(artifact.isDeleted())
					throw new Exception("Deleted Artifact cannot be rewritten");
				domain = nthDomain;
				break;
			}
		}

		// 2 (a) ------ Artifact level change
		// Check if the artifact id exists 
		// If artifact ID is null return error and escape into the unknown
		if (artifact == null) { 
			throw new Exception("Artifact doesnt exist");
		}
		
		// validate copies
		if(rewriteCopy == goodCopy)
			throw new Exception("Both rewrite and good copy cannot be same");
		
		// Step 1 - Create User Request
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("artifactId", artifactId);
		data.put("rewriteCopy", rewriteCopy);
		data.put("goodCopy", goodCopy);
		
		Request userRequest = createUserRequest(Action.rewrite, Status.queued, data);
		
		Request systemrequest = new Request();
		systemrequest.setType(RequestType.system);
		systemrequest.setRequestRef(userRequest);
		systemrequest.setActionId(userRequest.getActionId());
		systemrequest.setStatus(Status.queued);
		systemrequest.setRequestedBy(userRequest.getRequestedBy());
		systemrequest.setRequestedAt(LocalDateTime.now());

		RequestDetails systemrequestDetails = new RequestDetails();
		systemrequestDetails.setArtifactId(artifactId);
		systemrequestDetails.setRewriteCopy(rewriteCopy);
		systemrequestDetails.setGoodCopy(goodCopy);
		
		systemrequest.setDetails(systemrequestDetails);
		systemrequest = requestDao.save(systemrequest);
		logger.info(DwaraConstants.SYSTEM_REQUEST + systemrequest.getId());

		jobCreator.createJobs(systemrequest, artifact);


		// Return response
		ArtifactResponse dr = new ArtifactResponse();
		org.ishafoundation.dwaraapi.api.resp.artifact.Artifact artifactForResponse = miscObjectMapper.getArtifactForArtifactResponse(artifact);
		dr.setArtifact(artifactForResponse);
		dr.setUserRequestId(userRequest.getId());
		dr.setAction(userRequest.getActionId().name());
		dr.setRequestedAt(getDateForUI(userRequest.getRequestedAt()));
		dr.setRequestedBy(userRequest.getRequestedBy().getName());

		return dr;
	}

	public ArtifactResponse changeArtifactclass(int artifactId, String newArtifactclassId, Boolean force) throws Exception {
		// validate 
		ArtifactRepository<Artifact> artifactRepository = null;
		Artifact requestedArtifact = null; // get the artifact details from DB
		Domain domain = null; 
		Domain[] domains = Domain.values();
		for (Domain nthDomain : domains) {
			artifactRepository = domainUtil.getDomainSpecificArtifactRepository(nthDomain);
			Optional<Artifact> artifactEntity = artifactRepository.findById(artifactId);
			if(artifactEntity.isPresent()) {
				requestedArtifact = artifactEntity.get();
				if(requestedArtifact.isDeleted())
					throw new Exception("Deleted Artifact cannot be changed");
				domain = nthDomain;
				break;
			}
		}

		// 2 (a) ------ Artifact level change
		// Check if the artifact id exists 
		// If artifact ID is null return error and escape into the unknown
		if (requestedArtifact == null) { 
			throw new Exception("Artifact doesnt exist");
		}

		// if same artifactclass
		Artifactclass currentArtifactclass = requestedArtifact.getArtifactclass();
		String currentArtifactclassId = currentArtifactclass.getId();
		if(currentArtifactclassId.equals(newArtifactclassId))
			throw new DwaraException("Artifactclass cannot be changed to same current artifactclass. Current " + currentArtifactclassId + " Requested " + newArtifactclassId);
		
		// if new artifactclass exists
		Artifactclass newArtifactclass = configurationTablesUtil.getArtifactclass(newArtifactclassId);
		
		if(newArtifactclass == null) {
			throw new DwaraException(newArtifactclassId + " not configured in artifactclass table. Please double check");
		}

		// Validate if current and new artifactclasses belong to same group check ??? Wont work if the following needed Eg., video-digi-edit* to video-digi*
		String currentSequenceRefId = currentArtifactclass.getSequence().getSequenceRef().getId();
		String newSequenceRefId = newArtifactclass.getSequence().getSequenceRef().getId();
		if(!force && !newSequenceRefId.equals(currentSequenceRefId))
			throw new DwaraException("Only changing within same sequence group is supported. Current " + currentSequenceRefId + " Requested " + newSequenceRefId + ". If you know what you are doing try force option");
			
		// status check on request
		Request request = requestedArtifact.getWriteRequest();
		Status requestStatus = request.getStatus();
		boolean isGoodToGo = false;
		if(requestStatus == Status.completed) {
			isGoodToGo = true;
		}
		else if(requestStatus == Status.on_hold) {
			ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
			List<ArtifactVolume> artifactVolumeList = domainSpecificArtifactVolumeRepository.findAllByIdArtifactId(artifactId);
			
			if(artifactVolumeList.size() > 0)
				throw new Exception("Some or all copies written to tape. Wait till the request gets completed");
			else
				isGoodToGo = true;
		}
		
		if(!isGoodToGo) {
			throw new DwaraException("Changing artifactclass not supported for running requests. Please put the jobs on hold");			
			// Lets not be tempted to complicate this and automate by putting the jobs first on hold for queued requests when no write happened...
		}
			
		// synchronous task so create userRequest
		// Step 1 - Create User Request
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("artifactId", artifactId);
		data.put("artifactclass", currentArtifactclassId);
		data.put("newArtifactclass", newArtifactclassId);
		
		Request userRequest = createUserRequest(Action.change_artifactclass, Status.in_progress, data);
				
		try {
			List<Artifact> artifactList = artifactRepository.findAllByWriteRequestId(request.getId());
			for (Artifact nthArtifact : artifactList) {
				if(nthArtifact.getId() != requestedArtifact.getId()) { // if the looping artifact is not the requested artifact then
					currentArtifactclass = nthArtifact.getArtifactclass();
					newArtifactclass = configurationTablesUtil.getArtifactclass(currentArtifactclass.getId().replace(currentArtifactclassId, newArtifactclassId));
				}
				
				// sequence code
				String artifactName = nthArtifact.getName();
				String currentSeqCode = StringUtils.substringBefore(artifactName,"_");
				String artifactNameWithoutSequence = StringUtils.substringAfter(artifactName,"_");
				String newSeqCode = null;
				
				currentSequenceRefId = currentArtifactclass.getSequence().getSequenceRef().getId();
				newSequenceRefId = newArtifactclass.getSequence().getSequenceRef().getId();
				
				if(newSequenceRefId.equals(currentSequenceRefId)) {
					// just replace the sequence number with the correct prefix
					newSeqCode = currentSeqCode.replace(currentArtifactclass.getSequence().getPrefix(), newArtifactclass.getSequence().getPrefix());
				}
				else if(force) {
					newSeqCode = sequenceUtil.getSequenceCode(newArtifactclass.getSequence(), artifactNameWithoutSequence);	
				}
		
				String toBeArtifactName = newSeqCode + "_" + artifactNameWithoutSequence;
		
				// update artifact table
				nthArtifact.setSequenceCode(newSeqCode);
				nthArtifact.setName(toBeArtifactName);
				nthArtifact.setArtifactclass(newArtifactclass);
				artifactRepository.save(nthArtifact);
				
				String parentFolderReplaceRegex = "^"+artifactName; 
				List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList = fileRepositoryUtil.getAllArtifactFileList(nthArtifact, domain);			
				for (org.ishafoundation.dwaraapi.db.model.transactional.domain.File eachfile : artifactFileList) { 
					// Getting the filename
					String filepath = eachfile.getPathname();
					// Change the parent folder name by replacing the older artifact name by newer name
					String correctedFilePathForArtifactFile = filepath.replaceAll(parentFolderReplaceRegex, toBeArtifactName); 
					eachfile.setPathname(correctedFilePathForArtifactFile);
					byte[] filePathChecksum = ChecksumUtil.getChecksum(correctedFilePathForArtifactFile);
					eachfile.setPathnameChecksum(filePathChecksum);
				} // File entry manipulation and renaming ends here 
					
				List<TFile> artifactTFileList  = tFileDao.findAllByArtifactId(nthArtifact.getId()); // Including the Deleted Ones
				// TODO : what happens if Tfile records are purged...
				for (TFile nthTFile : artifactTFileList) {
					String filepath = nthTFile.getPathname();
					// Change the parent folder name by replacing the older artifact name by newer name
					String correctedFilePathForArtifactFile = filepath.replaceAll(parentFolderReplaceRegex, toBeArtifactName); 
					nthTFile.setPathname(correctedFilePathForArtifactFile);	
					byte[] filePathChecksum = ChecksumUtil.getChecksum(correctedFilePathForArtifactFile);
					nthTFile.setPathnameChecksum(filePathChecksum);
				}
					
				FileRepository<File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
				// Update the file table
				domainSpecificFileRepository.saveAll(artifactFileList);
				tFileDao.saveAll(artifactTFileList);
		
				if(requestStatus == Status.on_hold) {
					// get the write jobs and change the group volume id
					List<Job> jobList = jobDao.findAllByRequestIdAndInputArtifactId(request.getId(), artifactId);
					
					Map<Integer, Volume> copyId_GrpVolume_Map = new HashMap<Integer, Volume>();
					List<ArtifactclassVolume> artifactclassVolumeList = artifactclassVolumeDao.findAllByArtifactclassIdAndActiveTrue(newArtifactclassId);
					for (ArtifactclassVolume artifactclassVolume : artifactclassVolumeList) {
						Volume grpVolume = artifactclassVolume.getVolume();
						copyId_GrpVolume_Map.put(grpVolume.getCopy().getId(), grpVolume);
					}
					
					for (Job nthJob : jobList) {
						if(nthJob.getStoragetaskActionId() == Action.write) {
							nthJob.setGroupVolume(copyId_GrpVolume_Map.get(nthJob.getGroupVolume().getCopy().getId()));
							jobDao.save(nthJob);
						}
					}
					
					// mv the physical folder
					String readyToIngestPath =  currentArtifactclass.getPathPrefix();
					String newReadyToIngestPath =  newArtifactclass.getPathPrefix();
					Files.move(Paths.get(readyToIngestPath, artifactName), Paths.get(newReadyToIngestPath, toBeArtifactName), StandardCopyOption.ATOMIC_MOVE);
		
				}
			}
		}catch (Exception e) {
			userRequest.setStatus(Status.failed);
			requestDao.save(userRequest);
			throw new Exception("Unable to change Artifactclass for " + artifactId + " : " + e.getMessage());
		}
		
		userRequest.setStatus(Status.completed);
		requestDao.save(userRequest);

		// Return response
		ArtifactResponse dr = new ArtifactResponse();
		org.ishafoundation.dwaraapi.api.resp.artifact.Artifact artifactForResponse = miscObjectMapper.getArtifactForArtifactResponse(requestedArtifact);
		dr.setArtifact(artifactForResponse);
		dr.setUserRequestId(userRequest.getId());
		dr.setAction(Action.change_artifactclass.name());
		dr.setRequestedAt(getDateForUI(userRequest.getRequestedAt()));
		dr.setRequestedBy(userRequest.getRequestedBy().getName());

		return dr;
	}
}
