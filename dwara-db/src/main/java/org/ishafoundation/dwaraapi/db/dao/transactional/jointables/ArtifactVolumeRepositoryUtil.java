package org.ishafoundation.dwaraapi.db.dao.transactional.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactVolume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArtifactVolumeRepositoryUtil {

	private static final Logger logger = LoggerFactory.getLogger(ArtifactVolumeRepositoryUtil.class);
	
	@Autowired
	private ArtifactVolumeRepository<ArtifactVolume> artifactVolumeRepository;
	
	public int getLastArtifactOnVolumeEndVolumeBlock( Volume volume) {
		ArtifactVolume artifactVolume = getLastArtifactOnVolume( volume);
		int lastArtifactOnVolumeEndVolumeBlock = 0;
		if(artifactVolume != null)
			lastArtifactOnVolumeEndVolumeBlock = artifactVolume.getDetails().getEndVolumeBlock();

		return lastArtifactOnVolumeEndVolumeBlock;
	}


	public ArtifactVolume getLastArtifactOnVolume( Volume volume) {
		//ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
		ArtifactVolume lastArtifactOnVolume = null;
		int lastArtifactOnVolumeEndVolumeBlock = 0;
		List<ArtifactVolume> artifactVolumeList = artifactVolumeRepository.findAllByIdVolumeId(volume.getId());
		for (ArtifactVolume artifactVolume : artifactVolumeList) {
			int avEVB = artifactVolume.getDetails().getEndVolumeBlock(); 	// TODO - For non block storages we shouldnt be looking at block details to determine this
			if(lastArtifactOnVolumeEndVolumeBlock < avEVB) {
				lastArtifactOnVolumeEndVolumeBlock = avEVB;
				lastArtifactOnVolume = artifactVolume;
			}
		}
		return lastArtifactOnVolume;
	}
}
