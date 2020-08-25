package org.ishafoundation.dwaraapi.utils;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VolumeUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(VolumeUtil.class);
	
	@Autowired
	private VolumeDao volumeDao;
	
	@Autowired
	private CapacityCalculatorUtil capacityCalculatorUtil;
	
	public Volume getToBeUsedPhysicalVolume(Domain domain, String volumegroupId, long artifactSize) {
		Volume toBeUsedVolume = null;
		List<Volume> physicalVolumesList = volumeDao.findAllByGroupRefIdAndFinalizedIsFalseOrderByIdAsc(volumegroupId);
		for (Volume nthPhysicalVolume : physicalVolumesList) {

			long projectedArtifactSize = getProjectedArtifactSize(artifactSize, nthPhysicalVolume);
			long volumeUnusedCapacity = capacityCalculatorUtil.getVolumeUnusedCapacity(domain, nthPhysicalVolume);
			
			// The chosen volume may not have enough space because of queued write jobs using it and so we may have to get the volume again just before write(job selection)...
			if(volumeUnusedCapacity > projectedArtifactSize) {
				toBeUsedVolume = nthPhysicalVolume;
				logger.trace(toBeUsedVolume + " has enough space. Selecting it");
				break;
			}else {
				logger.trace(nthPhysicalVolume + " hasnt got enough space to fit this artifact. Skipping it");
			}
		}
		return toBeUsedVolume;		
	}

	public long getProjectedArtifactSize(long artifactSize, Volume volume) {
		float filesizeIncreaseRate = 0;
		int filesizeIncreaseConst = 0;
		
		if(volume.getArchiveformat() != null) {
			Float configuredFilesizeIncreaseRate = volume.getArchiveformat().getFilesizeIncreaseRate(); // bru has something like 12.5%
			if(configuredFilesizeIncreaseRate != null)
				filesizeIncreaseRate = configuredFilesizeIncreaseRate;
	
			Integer configuredFilesizeIncreaseConst = volume.getArchiveformat().getFilesizeIncreaseConst();
			if(configuredFilesizeIncreaseConst != null)
				filesizeIncreaseConst = configuredFilesizeIncreaseConst;
		}
		
		long projectedArtifactSize = artifactSize + (long) (artifactSize * filesizeIncreaseRate) + filesizeIncreaseConst; 
		logger.trace("projectedArtifactSize " + projectedArtifactSize);
		return projectedArtifactSize;
	}
}
