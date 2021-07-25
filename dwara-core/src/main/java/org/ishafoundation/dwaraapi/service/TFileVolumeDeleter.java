package org.ishafoundation.dwaraapi.service;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TFileVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
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
	private DomainUtil domainUtil;
	
	public void softDeleteTFileVolumeEntries(Domain domain, List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList, List<TFile> artifactTFileList, Artifact artifact, String volumeId){
		// softDelete Filevolume entries
		List<FileVolume> toBeUpdatedFileVolumeTableEntries = new ArrayList<FileVolume>();
		for (org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile : artifactFileList) {
			FileVolume fileVolume = domainUtil.getDomainSpecificFileVolume(domain, nthFile.getId(), volumeId);
			if(fileVolume != null) {
				fileVolume.setDeleted(true);
				toBeUpdatedFileVolumeTableEntries.add(fileVolume);
			}
		}
	    if(toBeUpdatedFileVolumeTableEntries.size() > 0) {
	    	FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
	    	domainSpecificFileVolumeRepository.saveAll(toBeUpdatedFileVolumeTableEntries);
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

