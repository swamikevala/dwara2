package org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArtifactVolumeRepositoryUtil {

	private static final Logger logger = LoggerFactory.getLogger(ArtifactVolumeRepositoryUtil.class);
	
	@Autowired
	private DomainUtil domainUtil;
	
	public int getLastArtifactOnVolumeEndVolumeBlock(Domain domain, Volume volume) {
		ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
		int lastArtifactOnVolumeEndVolumeBlock = 0;
		List<ArtifactVolume> artifactVolumeList = domainSpecificArtifactVolumeRepository.findAllByIdVolumeId(volume.getId());
		for (ArtifactVolume artifactVolume : artifactVolumeList) {
			int avEVB = artifactVolume.getDetails().getEndVolumeBlock();
			if(lastArtifactOnVolumeEndVolumeBlock < avEVB) {
				lastArtifactOnVolumeEndVolumeBlock = avEVB;
			}
		}
		return lastArtifactOnVolumeEndVolumeBlock;
	}
	
	public ArtifactVolume getLastArtifactOnVolume(Domain domain, Volume volume) {
		ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
		ArtifactVolume lastArtifactOnVolume = null;
		int lastArtifactOnVolumeEndVolumeBlock = 0;
		List<ArtifactVolume> artifactVolumeList = domainSpecificArtifactVolumeRepository.findAllByIdVolumeId(volume.getId());
		for (ArtifactVolume artifactVolume : artifactVolumeList) {
			int avEVB = artifactVolume.getDetails().getEndVolumeBlock();
			if(lastArtifactOnVolumeEndVolumeBlock < avEVB) {
				lastArtifactOnVolumeEndVolumeBlock = avEVB;
				lastArtifactOnVolume = artifactVolume;
			}
		}
		return lastArtifactOnVolume;
	}
}
