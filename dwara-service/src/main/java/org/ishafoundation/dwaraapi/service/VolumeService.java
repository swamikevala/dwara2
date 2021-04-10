package org.ishafoundation.dwaraapi.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.req.initialize.InitializeUserRequest;
import org.ishafoundation.dwaraapi.api.resp.initialize.InitializeResponse;
import org.ishafoundation.dwaraapi.api.resp.initialize.SystemRequestForInitializeResponse;
import org.ishafoundation.dwaraapi.api.resp.volume.Details;
import org.ishafoundation.dwaraapi.api.resp.volume.VolumeResponse;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.ishafoundation.dwaraapi.resource.mapper.RequestToEntityObjectMapper;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.VolumeindexManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.VolumeFinalizer;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.VolumeInitializer;
import org.ishafoundation.dwaraapi.utils.VolumeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VolumeService extends DwaraService {

	private static final Logger logger = LoggerFactory.getLogger(VolumeService.class);
	
	@Autowired
	private VolumeDao volumeDao;

	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private VolumeUtil volumeUtil;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@Autowired
	private VolumeInitializer volumeInitializer;

	@Autowired
	private VolumeFinalizer volumeFinalizer;
	
	@Autowired
	private VolumeindexManager volumeindexManager;
	
	@Value("${filesystem.temporarylocation}")
	private String filesystemTemporarylocation;

	public List<VolumeResponse> getVolumeByVolumetype(String volumetype){
		List<VolumeResponse> volumeResponseList = null;
			// validate
			Volumetype neededVolumetype = Volumetype.valueOf(volumetype);
			
			volumeResponseList = new ArrayList<VolumeResponse>();
			List<Volume> volumeGroupList = volumeDao.findAllByType(neededVolumetype);
			for (Volume volume : volumeGroupList) {
				volumeResponseList.add(getVolume_Internal(volume));
			}
		return volumeResponseList;
	}

	public VolumeResponse getVolume(String volumeId) {
		Optional<Volume> volumeEntity = volumeDao.findById(volumeId);
		if(volumeEntity.isPresent()) {
			Volume volume = volumeEntity.get();
			return getVolume_Internal(volume);
		}else
			return null;
	}
	
	private VolumeResponse getVolume_Internal(Volume volume) {
		VolumeResponse volResp = new VolumeResponse();
		volResp.setId(volume.getId());
		volResp.setVolumetype(volume.getType().name());
		volResp.setStoragetype(volume.getStoragetype().name());
		volResp.setStoragelevel(volume.getStoragelevel().name());
		//volResp.setVolumeRef(volumeRef);
		//volResp.setChecksumtype(checksumtype);
		//volResp.setInitializedAt(initializetedAt);
		volResp.setFinalized(volume.isFinalized());
		volResp.setImported(volume.isImported());
		if(volume.getArchiveformat() != null)
			volResp.setArchiveformat(volume.getArchiveformat().getId());

		if(volume.getType() == Volumetype.group) {

			Domain domain = null;
			long groupVolumeCapacity = 0L;
			long groupVolumeUnusedCapacity = 0L;
			long groupVolumeUsedCapacity = 0L;
			long maxPhysicalUnusedCapacity = 0L;
			List<Volume> physicalVolumeList = volumeDao.findAllByGroupRefIdAndFinalizedIsFalseAndDefectiveIsFalseAndSuspectIsFalseOrderByIdAsc(volume.getId()); // get all not finalized physical volume in the group
			
			for (Volume nthPhyscialVolume : physicalVolumeList) { // iterate all physical volume from the group and sum up for total/used/unused cap
				logger.trace("Dashboard - " + nthPhyscialVolume.getId());
				if(domain == null) {
			   		Domain[] domains = Domain.values();
			   	
					for (Domain nthDomain : domains) {
					    ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(nthDomain);
					    int artifactVolumeCount = domainSpecificArtifactVolumeRepository.countByIdVolumeId(nthPhyscialVolume.getId());
						if(artifactVolumeCount > 0) {
							domain = nthDomain;
							break;
						}
					}
				}
				
//				long nthPhysicalVolumeCapacity = nthPhyscialVolume.getCapacity();
//				long nthPhysicalVolumeUsedCapacity = volumeUtil.getVolumeUsedCapacity(domain, nthPhyscialVolume);
				groupVolumeCapacity += volumeUtil.getVolumeUsableCapacity(domain, nthPhyscialVolume);//nthPhyscialVolume.getCapacity();
				logger.trace("Dashboard -groupVolumeCapacity - " + groupVolumeCapacity);
				logger.trace("Dashboard -groupVolumeCapacity in GiB - " + groupVolumeCapacity/1073741824);
				long nthPhysicalVolumeUnusedCapacity = volumeUtil.getVolumeUnusedCapacity(domain, nthPhyscialVolume);
				groupVolumeUnusedCapacity += nthPhysicalVolumeUnusedCapacity;
				logger.trace("Dashboard -groupVolumeUnusedCapacity - " + groupVolumeUnusedCapacity);
				logger.trace("Dashboard -groupVolumeUnusedCapacity in GiB - " + groupVolumeUnusedCapacity/1073741824);
				if(maxPhysicalUnusedCapacity < nthPhysicalVolumeUnusedCapacity)
					maxPhysicalUnusedCapacity = nthPhysicalVolumeUnusedCapacity;
				logger.trace("Dashboard -maxPhysicalUnusedCapacity - " + maxPhysicalUnusedCapacity);
				logger.trace("Dashboard -maxPhysicalUnusedCapacity in GiB - " + maxPhysicalUnusedCapacity/1073741824);
				groupVolumeUsedCapacity += volumeUtil.getVolumeUsedCapacity(domain, nthPhyscialVolume);
				logger.trace("Dashboard -groupVolumeUsedCapacity - " + groupVolumeUsedCapacity);
				logger.trace("Dashboard -groupVolumeUsedCapacity in GiB - " + groupVolumeUsedCapacity/1073741824);
			}

			volResp.setTotalCapacity(groupVolumeCapacity/1073741824);
			volResp.setUsedCapacity(groupVolumeUsedCapacity/1073741824);
			volResp.setUnusedCapacity(groupVolumeUnusedCapacity/1073741824);
			volResp.setMaxPhysicalUnusedCapacity(maxPhysicalUnusedCapacity/1073741824);
			volResp.setSizeUnit("GiB"); // 1 GiB = 1073741824 bytes...
		}
		
		if(volume.getLocation() != null)
			volResp.setLocation(volume.getLocation().getId());
		
		VolumeDetails volumeDetails = volume.getDetails();
		if(volumeDetails != null) {
			Details details = new Details();
			
			//details.setBarcoded(volumeDetails.isBarcoded());
			if(volumeDetails.getBlocksize() != null)
				details.setBlocksize(volumeDetails.getBlocksize()/1024);
			details.setBlocksizeUnit("KiB");
			
			details.setStoragesubtype(volume.getStoragesubtype());
			//details.setMountPoint(mountPoint);
			//details.setProvider(provider);
			//details.setRemoveAfterJob(removeAfterJob);
			volResp.setDetails(details);
		}
		return volResp;
	}
	
	public InitializeResponse initialize(List<InitializeUserRequest> initializeRequestList) throws Exception{	
		Action requestedBusinessAction = Action.initialize;
		org.ishafoundation.dwaraapi.db.model.master.reference.Action action = configurationTablesUtil.getAction(requestedBusinessAction);
		if(action == null)
			throw new Exception("Action for " + requestedBusinessAction.name() + " not configured in DB properly. Please set it first");

		return volumeInitializer.initialize(getUserFromContext(), initializeRequestList);
	}
	
	public String finalize(String volumeId) throws Exception{
		return volumeFinalizer.finalize(volumeId, getUserFromContext());
	}

	public String generateVolumeindex(String volumeId) throws Exception {
		Volume volume = volumeDao.findById(volumeId).get();
		
		String label = volumeindexManager.createVolumeindex(volume, Domain.ONE);
		
		File file = new File(filesystemTemporarylocation + File.separator + volumeId + "_index.xml");
		FileUtils.writeStringToFile(file, label);
		String response = file.getAbsolutePath() + " created"; 
		logger.trace(response);
		
		return response;
	}
	
	
	
}

