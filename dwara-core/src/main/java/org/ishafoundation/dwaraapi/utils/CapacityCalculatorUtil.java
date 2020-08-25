package org.ishafoundation.dwaraapi.utils;

import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepositoryUtil;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CapacityCalculatorUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(CapacityCalculatorUtil.class);
	
	@Autowired
	private ArtifactVolumeRepositoryUtil artifactVolumeRepositoryUtil;
	
	
	public long getVolumeUsedCapacity(Domain domain, Volume volume){ 
		// works only for blocks - TODO need to address other storagelevels...
		int lastArtifactOnVolumeEndVolumeBlock = artifactVolumeRepositoryUtil.getLastArtifactOnVolumeEndVolumeBlock(domain, volume);
		int volumeBlocksize = volume.getDetails().getBlocksize();

//		ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
//		List<ArtifactVolume> artifactVolumeList = domainSpecificArtifactVolumeRepository.findAllByIdVolumeId(volume.getId());
//		long usedCapacity = 0L;
//		for (ArtifactVolume artifactVolume : artifactVolumeList) {
//			long totalSize = domainUtil.getDomainSpecificArtifact(domain, artifactVolume.getId().getArtifactId()).getTotalSize();
//			usedCapacity = usedCapacity + totalSize;
//		}
		long volumeUsedCapacity = lastArtifactOnVolumeEndVolumeBlock * volumeBlocksize; 
		logger.trace("volumeUsedCapacity " + volumeUsedCapacity);
		return volumeUsedCapacity;
	}
	
	// returns - in bytes...
	public long getVolumeUnusedCapacity(Domain domain, Volume volume){
		long capacity = volume.getCapacity(); // something like 6TB
		// but we dont use the full capacity but only till high water mark...
		long watermarkHigh = 10 * 1024 * 1024 * 1024;// TODO hardcoding to 10GB now. get it from db/configuration
		long useableCapacity = capacity - watermarkHigh;
		
		long usedCapacity = getVolumeUsedCapacity(domain, volume);
		long volumeUnUsedCapacity = useableCapacity - usedCapacity;
		logger.trace("volumeUnUsedCapacity " + volumeUnUsedCapacity);
		return volumeUnUsedCapacity;
	}
		
	public boolean isVolumeReadyToBeFinalized(Domain domain, Volume volume){
		boolean isReadyToBeFinalized = false;
		long usedCapacity = getVolumeUsedCapacity(domain, volume);
		
		long capacity = volume.getCapacity(); // something like 6TB
		// but we dont use the full capacity but only till high water mark...
		long watermarkLow = 15 * 1024 * 1024 * 1024;// TODO hardcoding to 15GB now. get it from db/configuration
		long volumeWatermarkLow = capacity - watermarkLow;
		logger.trace("volumeWatermarkLow " + volumeWatermarkLow);
		if(usedCapacity > volumeWatermarkLow) {
			logger.trace("usedCapacity > volumeWatermarkLow. So " + volume.getId() + " ready to be finalized");
			isReadyToBeFinalized = true;
		}
		logger.trace("isReadyToBeFinalized " + isReadyToBeFinalized);		
		return isReadyToBeFinalized;
	}
}
