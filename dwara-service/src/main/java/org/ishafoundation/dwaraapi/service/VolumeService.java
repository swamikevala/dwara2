package org.ishafoundation.dwaraapi.service;

import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.req.RewriteRequest;
import org.ishafoundation.dwaraapi.api.req.initialize.InitializeUserRequest;
import org.ishafoundation.dwaraapi.api.req.volume.MarkVolumeStatusRequest;
import org.ishafoundation.dwaraapi.api.resp.initialize.InitializeResponse;
import org.ishafoundation.dwaraapi.api.resp.volume.Details;
import org.ishafoundation.dwaraapi.api.resp.volume.MarkVolumeStatusResponse;
import org.ishafoundation.dwaraapi.api.resp.volume.VolumeResponse;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.ArtifactVolumeStatus;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.RewriteMode;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.VolumeHealthStatus;
import org.ishafoundation.dwaraapi.enumreferences.VolumeLifecyclestage;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.job.JobCreator;
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
	private JobCreator jobCreator;
	
	@Autowired
	private VolumeindexManager volumeindexManager;
	
	@Value("${filesystem.temporarylocation}")
	private String filesystemTemporarylocation;

	private static DecimalFormat df = new DecimalFormat("0.00");

	public List<Volume> getVolumeGroupByCopyNumber(int copyId) {
		List<Volume> list = volumeDao.findAllByCopyId(copyId);
		return list;
	}
	
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
		double GiB = 1073741824; // 1 GiB = 1073741824 bytes...
		double TiB = 1099511627776.00;
		double TB = 1000000000000.00;
		String sizeUnit = "TB"; // "GiB"; 
		double sizeUnitDivisor = TB;
		
		VolumeResponse volResp = new VolumeResponse();
		volResp.setId(volume.getId());
		volResp.setCopyNumber(volume.getCopy().getId());
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

			List<Volume> physicalVolumeList = volumeDao.findAllByGroupRefIdAndImportedIsFalseAndFinalizedIsFalseAndHealthstatusAndLifecyclestageOrderByIdAsc(volume.getId(), VolumeHealthStatus.normal, VolumeLifecyclestage.active); // get all not finalized physical volume in the group
			
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
				logger.trace("Dashboard -groupVolumeCapacity in " + sizeUnit + " - " + groupVolumeCapacity/sizeUnitDivisor);
				long nthPhysicalVolumeUnusedCapacity = volumeUtil.getVolumeUnusedCapacity(domain, nthPhyscialVolume);
				groupVolumeUnusedCapacity += nthPhysicalVolumeUnusedCapacity;
				logger.trace("Dashboard -groupVolumeUnusedCapacity - " + groupVolumeUnusedCapacity);
				logger.trace("Dashboard -groupVolumeUnusedCapacity in " + sizeUnit + " - "  + groupVolumeUnusedCapacity/sizeUnitDivisor);
				if(maxPhysicalUnusedCapacity < nthPhysicalVolumeUnusedCapacity)
					maxPhysicalUnusedCapacity = nthPhysicalVolumeUnusedCapacity;
				logger.trace("Dashboard -maxPhysicalUnusedCapacity - " + maxPhysicalUnusedCapacity);
				logger.trace("Dashboard -maxPhysicalUnusedCapacity in " + sizeUnit + " - "  + maxPhysicalUnusedCapacity/sizeUnitDivisor);
				groupVolumeUsedCapacity += volumeUtil.getVolumeUsedCapacity(domain, nthPhyscialVolume);
				logger.trace("Dashboard -groupVolumeUsedCapacity - " + groupVolumeUsedCapacity);
				logger.trace("Dashboard -groupVolumeUsedCapacity in " + sizeUnit + " - "  + groupVolumeUsedCapacity/sizeUnitDivisor);
			}

			volResp.setTotalCapacity(Float.valueOf(df.format(groupVolumeCapacity/sizeUnitDivisor)));
			volResp.setUsedCapacity(Float.valueOf(df.format(groupVolumeUsedCapacity/sizeUnitDivisor)));
			volResp.setUnusedCapacity(Float.valueOf(df.format(groupVolumeUnusedCapacity/sizeUnitDivisor)));
			volResp.setMaxPhysicalUnusedCapacity(Float.valueOf(df.format(maxPhysicalUnusedCapacity/sizeUnitDivisor)));
			volResp.setSizeUnit(sizeUnit); 
		}
		
		if(volume.getLocation() != null)
			volResp.setLocation(volume.getLocation().getId());
		
		VolumeDetails volumeDetails = volume.getDetails();
		if(volumeDetails != null) {
			Details details = new Details();
			
			//details.setBarcoded(volumeDetails.isBarcoded());
			if(volumeDetails.getBlocksize() != null) {
				details.setBlocksize(volumeDetails.getBlocksize()/1024);
				details.setBlocksizeUnit("KiB");
			}
			details.setStoragesubtype(volume.getStoragesubtype());
			//details.setMountPoint(mountPoint);
			//details.setProvider(provider);
			//details.setRemoveAfterJob(removeAfterJob);
			if(volume.getType() == Volumetype.group) {
				logger.info(volume.getId() + ":" +  volResp.getUnusedCapacity() + ":" + (volumeDetails.getMinimumFreeSpace()/sizeUnitDivisor));
				if(volResp.getUnusedCapacity() < (volumeDetails.getMinimumFreeSpace()/sizeUnitDivisor))
					details.setExpandCapacity(true);
				details.setNextBarcode(volume.getSequence().getPrefix() + (volume.getSequence().getCurrrentNumber() + 1) + "L7"); // TODO - How to findout LTO Generation???
			}
			
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
		
		String tmpXmlFilepathname = filesystemTemporarylocation + File.separator + volumeId + "_index.xml";
		volumeindexManager.createVolumeindexXml(volume, Domain.ONE, tmpXmlFilepathname);
		
		String response = tmpXmlFilepathname + " created"; 
		logger.trace(response);
		
		return response;
	}

	public void rewriteVolume(String volumeId, RewriteRequest rewriteRequest) throws Exception {
		//Optional<Volume> volumeEntity = volumeDao.findById(volumeId);
		Volume volume = volumeDao.findById(volumeId).get();
		if(volume.getHealthstatus() != VolumeHealthStatus.defective)
			throw new Exception(volumeId + " not flagged as defective. Please double check and flag it first");
			
		// create user request
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("volumeId", volumeId);
		data.put("mode", rewriteRequest.getMode());
		data.put("souceCopy", rewriteRequest.getSourceCopy());
		Integer additionalCopy = rewriteRequest.getDestinationCopy();
		if(additionalCopy != null)
			data.put("destinationCopy", additionalCopy);
		
		Pattern artifactclassRegexPattern = null;
		String artifactclassRegex = rewriteRequest.getArtifactclassRegex();
		if(artifactclassRegex != null) {
			artifactclassRegexPattern = Pattern.compile(artifactclassRegex);
			data.put("artifactclassRegex", artifactclassRegex);
		}
		Request userRequest = createUserRequest(Action.rewrite, data);
		Domain domain = Domain.ONE; // TODO Hardcoded domain

		ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
		List<ArtifactVolume> artifactVolumeList = domainSpecificArtifactVolumeRepository.findAllByIdVolumeIdAndStatus(volumeId, ArtifactVolumeStatus.current); // only not deleted artifacts need to be rewritten
		
		// loop artifacts on volume
		for (ArtifactVolume nthArtifactVolume : artifactVolumeList) {
			int artifactId = nthArtifactVolume.getId().getArtifactId();
			
			Artifact artifact = (Artifact) domainUtil.getDomainSpecificArtifactRepository(domain).findById(artifactId).get(); // get the artifact details from DB
 
			// filtering out artifacts that dont match specified artifactclass(es)
			if(artifactclassRegexPattern != null) {
				String artifactclassId = artifact.getArtifactclass().getId();
				Matcher m = artifactclassRegexPattern.matcher(artifactclassId);
				if(!m.matches()) { 
					logger.info("Skipping " + artifact.getName() + "(" + artifactId + ") as " + artifactclassId + " doesnt match " + artifactclassRegex);
					continue;
				}
			}

			// Also System will skip any artifacts which already exist on the additional copy (e.g. if the config was changed to increase the number of copies from 3 to 4 while a tape was partially written - so the artifacts on the latter part of that tape would need to be skipped)
			if(additionalCopy != null) {
				boolean isCopyAlreadyWritten = false; 
				List<ArtifactVolume> artifactVolumeList2 = domainSpecificArtifactVolumeRepository.findAllByIdArtifactIdAndStatus(artifactId, ArtifactVolumeStatus.current);
				for (ArtifactVolume nthArtifactVolume2 : artifactVolumeList2) {
					if(nthArtifactVolume2.getVolume().getGroupRef().getCopy().getId() == additionalCopy) {
						isCopyAlreadyWritten = true;
						break;
					}
				}
				if(isCopyAlreadyWritten) {
					logger.info("Skipping " + artifact.getName() + "(" + artifactId + ") as additional copy already written");
					continue;
				}
			}
			
			//create system requests			
			Request systemRequest = new Request();
			systemRequest.setType(RequestType.system);
			systemRequest.setRequestRef(userRequest);
			systemRequest.setActionId(userRequest.getActionId());
			systemRequest.setStatus(Status.queued);
			systemRequest.setRequestedBy(userRequest.getRequestedBy());
			systemRequest.setRequestedAt(LocalDateTime.now());
		
			RequestDetails systemrequestDetails = new RequestDetails();
			systemrequestDetails.setArtifactId(artifactId);
			systemrequestDetails.setVolumeId(volumeId);
			systemrequestDetails.setMode(RewriteMode.valueOf(rewriteRequest.getMode()));
			systemrequestDetails.setSourceCopy(rewriteRequest.getSourceCopy());
			if(rewriteRequest.getDestinationCopy() != null)
				systemrequestDetails.setDestinationCopy(rewriteRequest.getDestinationCopy());
			if(rewriteRequest.getArtifactclassRegex() != null)
				systemrequestDetails.setArtifactclassRegex(rewriteRequest.getArtifactclassRegex());
			systemRequest.setDetails(systemrequestDetails);
			systemRequest = requestDao.save(systemRequest);
			
			logger.info(DwaraConstants.SYSTEM_REQUEST + systemRequest.getId());
			

			//create jobs
			jobCreator.createJobs(systemRequest, artifact);
		}
	}

	public MarkVolumeStatusResponse markVolumeHealthstatus(String volumeId, String healthstatus, MarkVolumeStatusRequest markVolumeStatusRequest) throws Exception {
		MarkVolumeStatusResponse markVolumeStatusResponse = new MarkVolumeStatusResponse();
		
		Volume volume = volumeDao.findById(volumeId).get();
		if(volume == null)
			throw new Exception(volumeId + " not found");
		
		VolumeHealthStatus volumeStatus = null;
		try {
			volumeStatus = VolumeHealthStatus.valueOf(healthstatus);
			if(volumeStatus == null)
				throw new Exception(healthstatus + " not supported");
				
		}catch (Exception e) {
			throw new Exception(healthstatus + " not supported");
		}
		
		String reason = markVolumeStatusRequest.getReason();
		// create user request
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("volumeId", volumeId);
		data.put("healthstatus", healthstatus);
		data.put("reason", reason);
		Request userRequest = createUserRequest(Action.mark_volume, Status.completed, data);
		userRequest.setMessage(reason);
		requestDao.save(userRequest);
		
		volume.setHealthstatus(volumeStatus);

		volumeDao.save(volume);
		
		markVolumeStatusResponse.setRequestId(userRequest.getId());
		markVolumeStatusResponse.setRequestedBy(userRequest.getRequestedBy().getName());
		markVolumeStatusResponse.setRequestedAt(getDateForUI(userRequest.getRequestedAt()));
		
		markVolumeStatusResponse.setVolumeId(volumeId);
		markVolumeStatusResponse.setVolumeStatus(healthstatus);
		
		markVolumeStatusResponse.setAction(userRequest.getActionId().name());
		markVolumeStatusResponse.setStatus(userRequest.getStatus().name());
		markVolumeStatusResponse.setCompletedAt(getDateForUI(userRequest.getCompletedAt()));
		
		return markVolumeStatusResponse;
	}
	
	public MarkVolumeStatusResponse markVolumeLifecyclestage(String volumeId, String lifecyclestage, MarkVolumeStatusRequest markVolumeStatusRequest) throws Exception {
		MarkVolumeStatusResponse markVolumeStatusResponse = new MarkVolumeStatusResponse();
		
		Volume volume = volumeDao.findById(volumeId).get();
		if(volume == null)
			throw new Exception(volumeId + " not found");
		
		VolumeLifecyclestage volumeStatus = null;
		try {
			volumeStatus = VolumeLifecyclestage.valueOf(lifecyclestage);
			if(volumeStatus == null)
				throw new Exception(lifecyclestage + " not supported");
				
		}catch (Exception e) {
			throw new Exception(lifecyclestage + " not supported");
		}
		
		String reason = markVolumeStatusRequest.getReason();
		// create user request
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("volumeId", volumeId);
		data.put("lifecyclestage", lifecyclestage);
		data.put("reason", reason);
		Request userRequest = createUserRequest(Action.mark_volume, Status.completed, data);
		userRequest.setMessage(reason);
		requestDao.save(userRequest);
		
		volume.setLifecyclestage(volumeStatus);

		volumeDao.save(volume);
		
		markVolumeStatusResponse.setRequestId(userRequest.getId());
		markVolumeStatusResponse.setRequestedBy(userRequest.getRequestedBy().getName());
		markVolumeStatusResponse.setRequestedAt(getDateForUI(userRequest.getRequestedAt()));
		
		markVolumeStatusResponse.setVolumeId(volumeId);
		markVolumeStatusResponse.setVolumeStatus(lifecyclestage);
		
		markVolumeStatusResponse.setAction(userRequest.getActionId().name());
		markVolumeStatusResponse.setStatus(userRequest.getStatus().name());
		markVolumeStatusResponse.setCompletedAt(getDateForUI(userRequest.getCompletedAt()));
		
		return markVolumeStatusResponse;
	}
}

