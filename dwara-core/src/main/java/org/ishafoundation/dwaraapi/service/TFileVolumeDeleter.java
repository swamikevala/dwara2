package org.ishafoundation.dwaraapi.service;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.FileVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TFileVolumeDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileVolume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TFileVolumeDeleter {
	
	private static final Logger logger = LoggerFactory.getLogger(TFileVolumeDeleter.class);

	@Autowired
	private TFileVolumeDao tFileVolumeDao;

	@Autowired
	private FileVolumeDao fileVolumeDao;
	
	public void softDeleteTFileVolumeEntries(List<org.ishafoundation.dwaraapi.db.model.transactional.File> artifactFileList, List<TFile> artifactTFileList, Artifact artifact){
		// softDelete Filevolume entries
		List<FileVolume> toBeUpdatedFileVolumeTableEntries = new ArrayList<FileVolume>();
		for (org.ishafoundation.dwaraapi.db.model.transactional.File nthFile : artifactFileList) {
			List<FileVolume> fileVolumeList = fileVolumeDao.findAllByIdFileId(nthFile.getId());
			for (FileVolume fileVolume : fileVolumeList) {
				if(fileVolume != null) {
					fileVolume.setDeleted(true);
					toBeUpdatedFileVolumeTableEntries.add(fileVolume);
				}
			}
		}
	    if(toBeUpdatedFileVolumeTableEntries.size() > 0) {
	    	fileVolumeDao.saveAll(toBeUpdatedFileVolumeTableEntries);
	    	logger.info("All FileVolume records for " + artifact.getName() + " [" + artifact.getId() + "] flagged deleted successfully");
	    }
	    
	    // softDelete TFileVolume entries
		if(artifactTFileList != null) { // An artifact can be deleted even after the tape is finalized at that point no TFile entries will be there
		    List<TFileVolume> toBeUpdatedTFileVolumeTableEntries = new ArrayList<TFileVolume>();
		    for (TFile nthTFile : artifactTFileList) {
		    	List<TFileVolume> tFileVolumeList = tFileVolumeDao.findAllByIdFileId(nthTFile.getId());
				for (TFileVolume tFileVolume : tFileVolumeList) {
			    	if(tFileVolume != null) {
						tFileVolume.setDeleted(true);
						toBeUpdatedTFileVolumeTableEntries.add(tFileVolume);
					}					
				}
		    }
		    if(toBeUpdatedTFileVolumeTableEntries.size() > 0) {
		    	tFileVolumeDao.saveAll(toBeUpdatedTFileVolumeTableEntries);
		    	logger.info("All TFileVolume records for " + artifact.getName() + " [" + artifact.getId() + "] flagged deleted successfully");
		    }
		}
	}
	
	public void softDeleteTFileVolumeEntries( List<org.ishafoundation.dwaraapi.db.model.transactional.File> artifactFileList, List<TFile> artifactTFileList, Artifact artifact, String volumeId){
		// softDelete Filevolume entries
		List<FileVolume> toBeUpdatedFileVolumeTableEntries = new ArrayList<FileVolume>();
		for (org.ishafoundation.dwaraapi.db.model.transactional.File nthFile : artifactFileList) {
			FileVolume fileVolume = fileVolumeDao.findByIdFileIdAndIdVolumeId(nthFile.getId(), volumeId);
			if(fileVolume != null) {
				fileVolume.setDeleted(true);
				toBeUpdatedFileVolumeTableEntries.add(fileVolume);
			}
		}
	    if(toBeUpdatedFileVolumeTableEntries.size() > 0) {
	    	fileVolumeDao.saveAll(toBeUpdatedFileVolumeTableEntries);
	    	logger.info("All FileVolume records for " + artifact.getName() + " [" + artifact.getId() + "] in volume " + volumeId + " flagged deleted successfully");
	    }
	    
	    // softDelete TFileVolume entries
		if(artifactTFileList != null) { // An artifact can be deleted even after the tape is finalized at that point no TFile entries will be there
		    List<TFileVolume> toBeUpdatedTFileVolumeTableEntries = new ArrayList<TFileVolume>();
		    for (TFile nthTFile : artifactTFileList) {
		    	TFileVolume tFileVolume = tFileVolumeDao.findByIdFileIdAndIdVolumeId(nthTFile.getId(), volumeId);
				if(tFileVolume != null) {
					tFileVolume.setDeleted(true);
					toBeUpdatedTFileVolumeTableEntries.add(tFileVolume);
				}
		    }
		    if(toBeUpdatedTFileVolumeTableEntries.size() > 0) {
		    	tFileVolumeDao.saveAll(toBeUpdatedTFileVolumeTableEntries);
		    	logger.info("All TFileVolume records for " + artifact.getName() + " [" + artifact.getId() + "] in volume " + volumeId + " flagged deleted successfully");
		    }
		}
	}
}

