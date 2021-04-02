package org.ishafoundation.dwaraapi.service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.ishafoundation.dwaraapi.api.resp.artifact.ArtifactResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepositoryUtil;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.Status;
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
	private MamUpdateTaskExecutor mamUpdateTaskExecutor;

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
		org.ishafoundation.dwaraapi.api.resp.artifact.Artifact artifactForResponse = miscObjectMapper.getArtifactForDeleteArtifactResponse(artifact);
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
		Request writeRequestRowForTheArtifactToRename = artifactToRenameActualRow.getWriteRequest();
		Status statusOfTheWriteRequestPertainingToTheRenamedFolder =  writeRequestRowForTheArtifactToRename.getStatus();
		if (!force && statusOfTheWriteRequestPertainingToTheRenamedFolder != Status.completed) {
			throw new Exception("System request " + writeRequestRowForTheArtifactToRename.getId() + " not yet completed");
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
		
    	List<Artifact> artifactList = artifactRepository.findAllByWriteRequestId(writeRequestRowForTheArtifactToRename.getId());
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
				// Each file will now be renamed and the DB entry changed subsequently like butter through knife...
				String eachFilePath = eachfile.getPathname();
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
		org.ishafoundation.dwaraapi.api.resp.artifact.Artifact artifactForResponse = miscObjectMapper.getArtifactForDeleteArtifactResponse(artifactToRenameActualRow);
		dr.setArtifact(artifactForResponse);
		dr.setUserRequestId(userRequest.getId());
		dr.setAction(Action.rename.name());
		dr.setRequestedAt(getDateForUI(userRequest.getRequestedAt()));
		dr.setRequestedBy(userRequest.getRequestedBy().getName());

		return dr;
	}
}
