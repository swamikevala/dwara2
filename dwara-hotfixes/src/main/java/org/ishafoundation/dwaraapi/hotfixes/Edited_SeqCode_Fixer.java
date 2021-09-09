package org.ishafoundation.dwaraapi.hotfixes;


import java.util.List;

import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.ishafoundation.dwaraapi.db.dao.master.SequenceDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileRepositoryUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.File;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * For some reason sequencenumber after 9350 got reset to 9265 again for sequence group video-edit-grp - This is to fix it...
 * 
 */
@CrossOrigin
@RestController
public class Edited_SeqCode_Fixer {

	private static final Logger logger = LoggerFactory.getLogger(Edited_SeqCode_Fixer.class);

	@Autowired
	private SequenceDao sequenceDao;
	
	@Autowired
	private TFileDao tFileDao;

	@Autowired
	private ArtifactRepository artifactRepository; 
	
	@Autowired
	private FileRepository fileRepository;

	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@SuppressWarnings("rawtypes")
	@Autowired
	private DBMasterTablesCacheManager dBMasterTablesCacheManager;
	
	@PostMapping(value = "/fixSeqCodeForEdited", produces = "application/json")
	public ResponseEntity<String> fixSeqCodeForEdited(){
		// Hardcoded rather than parameterized so we know whats done
		int[] impactedAritfactIds = {17622,17623,17624,17625,17626,17627,17628,17629,17630,17631,17632,17633,17634,17635,17636,17637,17638,17639,17640,17641,17642,17643,17644,17645,17646,17647,17648,17649,17650,17651,17652,17653,17654,17655,17656,17657,17658,17659,17660,17661,17663,17664,17665,17666,17667,17668,17669,17670,17671,17672,17673,17674,17675,17676,17677,17678,17679,17680,18090,18091,18092,18093,18094,18095,19046,19047,19048,19049,19050,19051,19052,19053,19054,19055,19056,19057,19058,19059,19060,19061,19062,19063,19064,19065,19066,19067};
		int resetNumber = 9264; // 9264, because from 9350 it got reset to 9265  
 
		logger.info("/artifact/fixSeqCodeForEdited");
		
		Sequence grpSequence = configurationTablesUtil.getSequence("video-edit-grp");
		int seqCurrentNumber = grpSequence.getCurrrentNumber();
		logger.info("Sequence.CurrentNumber for video-edit-grp - " + seqCurrentNumber);
		int seqNumberToBeIncremented = grpSequence.getCurrrentNumber() - resetNumber;
		logger.info("Seq Number To Be Incremented With on impacted artifacts - " + seqNumberToBeIncremented);
		
		String status = "Done";
		for (int i = 0; i < impactedAritfactIds.length; i++) {
			int artifactId = impactedAritfactIds[i];
			try {
				logger.info("Updating - " + artifactId);
				updateArtifact(artifactId, seqNumberToBeIncremented);
				logger.info("Completed Updating - " + artifactId);
			}catch (Exception e) {
				logger.error("Unable to update " + artifactId + " : " + e.getMessage());
				status = "Check app logs";
			}
		}
		
		if(status.equals("Done")) {
			
			int newSeqCurrentNumber = seqCurrentNumber + impactedAritfactIds.length;
			grpSequence.setCurrrentNumber(newSeqCurrentNumber);
			sequenceDao.save(grpSequence);
			logger.info("Sequence.CurrentNumber for video-edit-grp reset to - " + newSeqCurrentNumber);
			
			// clear cache as sequence is cached...
			dBMasterTablesCacheManager.clearAll();
			dBMasterTablesCacheManager.loadAll();
			logger.info("Cache refreshed");
		}

		return ResponseEntity.status(HttpStatus.OK).body(status);
	}

	private void updateArtifact(int artifactId, int seqNumberToBeIncremented) throws Exception {
		// 2. Change the artifact name in the artifact folder and artifact table/ file table entry for the artifact
		ArtifactRepository<Artifact> artifactRepository = null;
		Artifact artifactToRenameActualRow = null; // get the artifact details from DB
		artifactToRenameActualRow = artifactRepository.findById(artifactId);
		/*Domain domain = null; 
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
*/
		Request systemRequest = artifactToRenameActualRow.getWriteRequest();
		
    	List<Artifact> artifactList = artifactRepository.findAllByWriteRequestId(systemRequest.getId());
    	for (Artifact artifact : artifactList) {
			String artifactName = artifact.getName();
			logger.info("Now Updating " + artifactName);
			String sequenceCode = artifact.getSequenceCode();
			
			String seqPrefix = null; // artifact.getArtifactclass().getSequence().getPrefix();
			
			if(artifact.getArtifactclass().isSource()) {
				seqPrefix = "Z";
			}
			else {
				seqPrefix = "ZL";
			}

			String newSequenceCode = seqPrefix + (Integer.parseInt(sequenceCode.replace(seqPrefix, "")) + seqNumberToBeIncremented);
			
    		String artifactNewName = artifactName.replace(sequenceCode + "_", newSequenceCode + "_"); 
			
    		// 2 (a) (i) Change the artifact name entry in artifact table
    		artifact.setName(artifactNewName);
    		artifact.setSequenceCode(newSequenceCode);
			// Save Artifact first
			artifactRepository.save(artifact);
			logger.info("Artifact table updated for " + artifactName + " with " + artifactNewName);
				
			// 2 (a) (ii) Change the File Table entries and the file names 
			// Step 4 - Flag all the file entries as soft-deleted
			String parentFolderReplaceRegex = "^"+artifactName; 
			List<org.ishafoundation.dwaraapi.db.model.transactional.File> artifactFileList = fileRepository.findAllByArtifactId(artifact.getId());			
			for (org.ishafoundation.dwaraapi.db.model.transactional.File eachfile : artifactFileList) { 
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
			for (TFile nthTFile : artifactTFileList) {
				String filepath = nthTFile.getPathname();
				// Change the parent folder name by replacing the older artifact name by newer name
				String correctedFilePathForArtifactFile = filepath.replaceAll(parentFolderReplaceRegex, artifactNewName); 
				nthTFile.setPathname(correctedFilePathForArtifactFile);	
				byte[] filePathChecksum = ChecksumUtil.getChecksum(correctedFilePathForArtifactFile);
				nthTFile.setPathnameChecksum(filePathChecksum);
			}
				
			//FileRepository<File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
			// Update the file table
			try {
				fileRepository.saveAll(artifactFileList);
				tFileDao.saveAll(artifactTFileList);
				logger.info("T/File tables updated for " + artifactName + " with " + artifactNewName);
			}
			catch (Exception e) {
				throw new Exception("File Table rename failed " + e.getMessage());
			}
    	}
	}
}	