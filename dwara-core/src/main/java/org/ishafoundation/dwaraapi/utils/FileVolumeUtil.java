package org.ishafoundation.dwaraapi.utils;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.ArtifactVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.FileVolumeDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileVolume;
import org.ishafoundation.dwaraapi.enumreferences.ArtifactVolumeStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileVolumeUtil {

	@Autowired
	private FileDao fileDao;
	
	@Autowired
	private ArtifactVolumeDao artifactVolumeDao;
	
	@Autowired
	private FileVolumeDao fileVolumeDao;

	
	public FileVolume getFileVolume(int fileIdToBeRestored, int copyNumber) throws Exception {
    	List<FileVolume> fileVolumeList = fileVolumeDao.findAllByIdFileIdAndVolumeGroupRefCopyId(fileIdToBeRestored, copyNumber);
    	FileVolume fileVolume = null;
    	if(fileVolumeList.size() > 1) {
    		
    		org.ishafoundation.dwaraapi.db.model.transactional.File file = fileDao.findById(fileIdToBeRestored).get();
    		Artifact artifact = file.getArtifact();
	    	for (FileVolume nthFileVolume : fileVolumeList) {
				ArtifactVolume artifactVolume = artifactVolumeDao.findByIdArtifactIdAndIdVolumeId(artifact.getId(), nthFileVolume.getId().getVolumeId());
				if(artifactVolume.getStatus() == ArtifactVolumeStatus.current || artifactVolume.getStatus() == null) {
					fileVolume = nthFileVolume;
					break;
				}
			}
    	}
    	else {
    		fileVolume = fileVolumeList.get(0);
    	}
    	return fileVolume;
	}
}
