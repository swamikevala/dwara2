package org.ishafoundation.dwaraapi.utils;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepositoryUtil;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.VolumeHealthStatus;
import org.ishafoundation.dwaraapi.enumreferences.VolumeLifecyclestage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VolumeUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(VolumeUtil.class);
	
	@Autowired
	private VolumeDao volumeDao;
	
	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private ArtifactVolumeRepositoryUtil artifactVolumeRepositoryUtil;

	@Value("${volumeCapacity.watermarkLow}")
	private float watermarkLow;
	
	@Value("${volumeCapacity.watermarkHigh}")
	private float watermarkHigh;
	
	public long getVolumeUsedCapacity(Domain domain, Volume volume){ 
		// works only for blocks - TODO need to address other storagelevels...
		int lastArtifactOnVolumeEndVolumeBlock = artifactVolumeRepositoryUtil.getLastArtifactOnVolumeEndVolumeBlock(domain, volume);
		logger.trace("lastArtifactOnVolumeEndVolumeBlock " + lastArtifactOnVolumeEndVolumeBlock);
		int volumeBlocksize = volume.getDetails().getBlocksize();
		logger.trace("volumeBlocksize " + volumeBlocksize);
//		ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
//		List<ArtifactVolume> artifactVolumeList = domainSpecificArtifactVolumeRepository.findAllByIdVolumeId(volume.getId());
//		long usedCapacity = 0L;
//		for (ArtifactVolume artifactVolume : artifactVolumeList) {
//			long totalSize = domainUtil.getDomainSpecificArtifact(domain, artifactVolume.getId().getArtifactId()).getTotalSize();
//			usedCapacity = usedCapacity + totalSize;
//		}
		long volumeUsedCapacity = (long) lastArtifactOnVolumeEndVolumeBlock * volumeBlocksize; 
		logger.trace("volumeUsedCapacity " + volumeUsedCapacity);
		return volumeUsedCapacity;
	}

	// returns - in bytes...
	public long getVolumeUsableCapacity(Domain domain, Volume volume){
		long capacity = volume.getCapacity(); // something like 6TB
		logger.trace("capacity " + capacity);
		// but we dont use the full capacity but only till high water mark...
		return (long) (capacity * watermarkHigh);
	}
	
	// returns - in bytes...
	public long getVolumeUnusedCapacity(Domain domain, Volume volume){
		long useableCapacity = getVolumeUsableCapacity(domain, volume);
		logger.trace("useableCapacity " + useableCapacity);
		long usedCapacity = getVolumeUsedCapacity(domain, volume);
		long volumeUnUsedCapacity = useableCapacity - usedCapacity;
		logger.trace("volumeUnUsedCapacity " + volumeUnUsedCapacity);
		return volumeUnUsedCapacity;
	}
		
	public boolean isVolumeNeedToBeFinalized(Domain domain, Volume volume, long usedCapacity){
		boolean isReadyToBeFinalized = false;
//		long usedCapacity = 
				getVolumeUsedCapacity(domain, volume);
		
		long capacity = volume.getCapacity(); // something like 6TB
		long volumeWatermarkLow = (long) (capacity * watermarkLow);
		logger.trace("volumeWatermarkLow " + volumeWatermarkLow);
		if(usedCapacity > volumeWatermarkLow) {
			logger.trace("usedCapacity > volumeWatermarkLow. So " + volume.getId() + " ready to be finalized");
			isReadyToBeFinalized = true;
		}
		logger.trace("isReadyToBeFinalized " + isReadyToBeFinalized);		
		return isReadyToBeFinalized;
	}
	
	public Volume getToBeUsedPhysicalVolume(Domain domain, String volumegroupId, long artifactSize) {
		Volume toBeUsedVolume = null;
		List<Volume> physicalVolumesList = volumeDao.findAllByGroupRefIdAndFinalizedIsFalseAndHealthstatusAndLifecyclestageOrderByIdAsc(volumegroupId, VolumeHealthStatus.normal, VolumeLifecyclestage.active);
		for (Volume nthPhysicalVolume : physicalVolumesList) {

			long projectedArtifactSize = getProjectedArtifactSize(artifactSize, nthPhysicalVolume);
			long volumeUnusedCapacity = getVolumeUnusedCapacity(domain, nthPhysicalVolume);
			
			// The chosen volume may not have enough space because of queued write jobs using it and so we may have to get the volume again just before write(job selection)...
			if(volumeUnusedCapacity > projectedArtifactSize) {
				toBeUsedVolume = nthPhysicalVolume;
				logger.trace(toBeUsedVolume.getId() + " has enough space. Selecting it");
				break;
			}else {
				logger.trace(nthPhysicalVolume.getId() + " hasnt got enough space to fit this artifact. Skipping it");
			}
		}
		if(toBeUsedVolume == null)
			logger.debug(volumegroupId + " hasnt got enough capacity. Bump it");
		return toBeUsedVolume;		
	}

	public boolean isPhysicalVolumeGood(String physicalVolumeId) {
		Volume toBeUsedVolume = volumeDao.findById(physicalVolumeId).get();
		if(toBeUsedVolume.getHealthstatus() != VolumeHealthStatus.normal)
			return false;
		return true;		
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
		logger.trace("artifactSize " + artifactSize + ", filesizeIncreaseRate " + filesizeIncreaseRate + ", filesizeIncreaseConst " + filesizeIncreaseConst);
		logger.trace("projectedArtifactSize " + projectedArtifactSize);
		return projectedArtifactSize;
	}
	
	public TapeUsageStatus getTapeUsageStatus(String volumeId) {
		TapeUsageStatus tapeUsageStatus = null;
		long jobInProgressCount = jobDao.countByStoragetaskActionIdIsNotNullAndVolumeIdAndStatus(volumeId, Status.in_progress);
		
		if(jobInProgressCount > 0) {
			tapeUsageStatus = TapeUsageStatus.job_in_progress;
		}
		else if(isQueuedJobOnVolume(volumeId) || isQueuedJobOnGroupVolume(volumeDao.findById(volumeId).get().getGroupRef().getId())){ // for restores and writes respectively
			// any group job queued up
			tapeUsageStatus = TapeUsageStatus.job_queued;
		}
		else {
			// if no job lined up, means no job needs it  
			tapeUsageStatus = TapeUsageStatus.no_job_queued;
		}
		return tapeUsageStatus;
	}
	
	// for writes
	public boolean isQueuedJobOnGroupVolume(String volumeId){
		boolean queuedJob = false;
		long jobInQueueCount = jobDao.countByStoragetaskActionIdIsNotNullAndGroupVolumeIdAndStatus(volumeId, Status.queued);
		if(jobInQueueCount > 0)
			queuedJob = true;
		return queuedJob; 
	}
	
	// for restores
	public boolean isQueuedJobOnVolume(String volumeId){
		boolean queuedJob = false;
		long jobInQueueCount = jobDao.countByStoragetaskActionIdIsNotNullAndVolumeIdAndStatus(volumeId, Status.queued);
		if(jobInQueueCount > 0)
			queuedJob = true;
		return queuedJob; 
	}
}
