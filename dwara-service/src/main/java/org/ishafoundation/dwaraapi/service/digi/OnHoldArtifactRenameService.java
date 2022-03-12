package org.ishafoundation.dwaraapi.service.digi;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ishafoundation.dwaraapi.api.resp.artifact.ArtifactResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.ArtifactVolumeDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactVolume;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.resource.mapper.MiscObjectMapper;
import org.ishafoundation.dwaraapi.service.DwaraService;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OnHoldArtifactRenameService extends DwaraService{

	private static final Logger logger = LoggerFactory.getLogger(OnHoldArtifactRenameService.class);

	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private TFileDao tFileDao;

	@Autowired
	private ArtifactDao artifactDao;
	
	@Autowired
	private ArtifactVolumeDao artifactVolumeDao;

	@Autowired
	private MiscObjectMapper miscObjectMapper; 

	@Autowired
	private FileDao fileDao;

	// Artifact rename function to rename artifacts - 1) Soft rename 2) hard rename for held jobs (renames the folder also)
	public ArtifactResponse hardSoftrenameArtifact(int artifactId, String newName) throws Exception {	
		
		if (!validateArtifactName(newName)) {
			throw new Exception("Name validation failed!");
		};
		
		
		// 2. Change the artifact name in the artifact folder and artifact table/ file table entry for the artifact
		Artifact artifactToRenameActualRow = null; // get the artifact details from DB
		Optional<Artifact> artifactEntity = artifactDao.findById(artifactId);
		if(artifactEntity.isPresent()) {
			artifactToRenameActualRow = artifactEntity.get();
		}

		// 2 (a) ------ Artifact level change
		// Check if the artifact id exists 
		// If artifact ID is null return error and escape into the unknown
		if (artifactToRenameActualRow == null) { 
			throw new Exception("Artifact doesnt exist!");
		}
		
		// Check if the folder belongs to a held job  
		Request writeRequestRowForTheArtifactToRename = artifactToRenameActualRow.getWriteRequest();
		Status statusOfTheWriteRequestPertainingToTheRenamedFolder =  writeRequestRowForTheArtifactToRename.getStatus();
		if (statusOfTheWriteRequestPertainingToTheRenamedFolder != Status.on_hold) {
			// The request is not held Quit.. later only allow soft rename for non held status
			// EXIT
			throw new Exception("Request/Artifact not on hold");
		}
		
		// Check if any write has already happened... 
		List<ArtifactVolume> artifactVolumeList = artifactVolumeDao.findAllByIdArtifactId(artifactId);
		
		if(artifactVolumeList.size() > 0)
			throw new Exception("Artifact already written to tape. Rename on hold not supported for already written artifacts...");
	
		
		String artifactName = artifactToRenameActualRow.getName();
		String sequenceId = artifactToRenameActualRow.getSequenceCode();
		String artifactNewName = sequenceId + "_" + newName; 
		// Step 1 - Create User Request
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("artifactId", artifactId);
		data.put("artifactName",artifactName);
		data.put("artifactNewName",artifactNewName);
		Request userRequest = createUserRequest(Action.rename, Status.in_progress, data);
		
		Request systemRequest = artifactToRenameActualRow.getWriteRequest();
    	List<Artifact> artifactList = artifactDao.findAllByWriteRequestId(systemRequest.getId());
    	for (Artifact artifact : artifactList) {
    		artifactName = artifact.getName();
    		sequenceId = artifact.getSequenceCode();
    		artifactNewName = sequenceId + "_" + newName; 
    		
			// 2 (a) (i) Change the artifact name entry in artifact table
			artifact.setName(artifactNewName);
			
			// 2 (a) (ii) Change the File Table entries and the file names 
			// Step 4 - Flag all the file entries as soft-deleted
			String parentFolderReplaceRegex = "^"+artifactName; 
			List<org.ishafoundation.dwaraapi.db.model.transactional.File> artifactFileList = fileDao.findAllByArtifactId(artifact.getId());			
			List<org.ishafoundation.dwaraapi.db.model.transactional.File> artifactFileListForRollback = fileDao.findAllByArtifactId(artifact.getId());			
			for (org.ishafoundation.dwaraapi.db.model.transactional.File eachfile : artifactFileList) { 
				// Getting the filename
				String filepath = eachfile.getPathname() ;
				// Change the parent folder name by replacing the older artifact name by newer name
				String correctedFilePathForArtifactFile = filepath.replaceAll(parentFolderReplaceRegex, artifactNewName); 
				eachfile.setPathname(correctedFilePathForArtifactFile);
				byte[] filePathChecksum = ChecksumUtil.getFilePathnameMD5Checksum(correctedFilePathForArtifactFile);
				eachfile.setPathnameChecksum(filePathChecksum);
	
			} // File entry manipulation and renaming ends here 
			
			List<TFile> artifactTFileList  = tFileDao.findAllByArtifactId(artifact.getId()); // Including the Deleted Ones
			for (TFile nthTFile : artifactTFileList) {
				String filepath = nthTFile.getPathname() ;
				// Change the parent folder name by replacing the older artifact name by newer name
				String correctedFilePathForArtifactFile = filepath.replaceAll(parentFolderReplaceRegex, artifactNewName); 
				nthTFile.setPathname(correctedFilePathForArtifactFile);	
				byte[] filePathChecksum = ChecksumUtil.getFilePathnameMD5Checksum(correctedFilePathForArtifactFile);
				nthTFile.setPathnameChecksum(filePathChecksum);
			}
			
			// Save Artifact first
			try {
				artifactDao.save(artifact);
			}
			catch (Exception e) {
				userRequest.setStatus(Status.failed);
				requestDao.save(userRequest);
				throw new Exception("Artifact Table rename failed "+e.getMessage());
			}
			
			// Update the file table
			try {
				fileDao.saveAll(artifactFileList);
				tFileDao.saveAll(artifactTFileList);
			}
			catch (Exception e) {
				// Roll-back the artifact table update change
				artifact.setName(artifactName);
				artifactDao.save(artifact);
				userRequest.setStatus(Status.failed);
				requestDao.save(userRequest);
				throw new Exception("File Table rename failed "+e.getMessage());
			}
			
			
			//Change the filename
			try {
				fileRename(artifact.getArtifactclass().getPath(), artifactName, artifactNewName);
			}catch (Exception e) {
				// Roll-back the artifact table update change and the file table update change
				artifact.setName(artifactName);
				artifactDao.save(artifact);
				
				fileDao.saveAll(artifactFileListForRollback);
				userRequest.setStatus(Status.failed);
				requestDao.save(userRequest);
				throw new Exception("Rename failed on folder level!");
				// EXIT With Errors
			} // If Rename has error in it completes here 	 
    	}
		userRequest.setStatus(Status.completed);
		requestDao.save(userRequest);
		// Return response
		ArtifactResponse dr = new ArtifactResponse();
		org.ishafoundation.dwaraapi.api.resp.artifact.Artifact artifactForResponse = miscObjectMapper.getArtifactForArtifactResponse(artifactToRenameActualRow);
		dr.setArtifact(artifactForResponse);
		dr.setAction(Action.rename.name());        
		return dr;
	} // rename function completes here 

	// Validate artifact name - It should not contain anything other than [A-z0-9-_]
	boolean validateArtifactName(String artifactNameToValidate) throws Exception {
		// Run the Regexp of coolness on it. [A-z0-9_-] are cool. Everything else UNCOOL! Uncool names will not be accepted. 
		boolean b = false;
		Pattern p = Pattern.compile("[^a-z0-9\\-_]", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(artifactNameToValidate);
		b = m.find();        	
		if (b) { return false;} else { return true;}    	
	}

	private void fileRename(String source, String fileName,String newFileName) throws Exception {

		String filePath = source + java.io.File.separator + fileName;
		String newFilePath = source + java.io.File.separator + newFileName;
		java.io.File file= new java.io.File(filePath);
		if (!file.exists()) {
			throw new Exception("File doesnt exist: "+filePath);
		}
		boolean renameResult = file.renameTo(new java.io.File(newFilePath));
		if(!renameResult)
			throw new Exception("Rename failed on FS");
	}
}
