package org.ishafoundation.dwaraapi.ltowala.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepositoryUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.ltowala.api.resp.Artifact;
import org.ishafoundation.dwaraapi.ltowala.api.resp.File;
import org.ishafoundation.dwaraapi.ltowala.api.resp.LtoWalaResponse;
import org.ishafoundation.dwaraapi.ltowala.api.resp.Volume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ForSolutionSameerService {

	@Autowired
	private JobDao jobDao;

	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private FileRepositoryUtil fileRepositoryUtil;
	
	// To narrow down the job to a specific copy
	private int copyNumber = 2; // Could be any copy...
	
	private static final Logger logger = LoggerFactory.getLogger(ForSolutionSameerService.class);
	
	public LtoWalaResponse dataForLtoWala(LocalDateTime startDateTime, LocalDateTime endDateTime, boolean listFiles, boolean needVolumeDetails) throws Exception {
		
		
		LtoWalaResponse ltoWalaResponse = new LtoWalaResponse();
		ltoWalaResponse.setStartDate(getDateForUI(startDateTime));
		ltoWalaResponse.setEndDate(getDateForUI(endDateTime));
				
		List<Status> statusList = new ArrayList<Status>();
		statusList.add(Status.completed);
		statusList.add(Status.completed_failures);
		
		List<Artifact> artifactList = new ArrayList<Artifact>();
		List<Request> requestList = requestDao.findAllByCompletedAtBetweenAndActionIdAndStatusInAndType(startDateTime, endDateTime, Action.ingest, statusList, RequestType.system);
		

		// find not completed but 3 copies completed list and send it to Vyom - Vyom's responsibility to dedupe
		List<Status> threeCopiesStatusList = new ArrayList<Status>();
		threeCopiesStatusList.add(Status.queued);
		threeCopiesStatusList.add(Status.in_progress);
		threeCopiesStatusList.add(Status.on_hold);
		threeCopiesStatusList.add(Status.failed);
		threeCopiesStatusList.add(Status.marked_completed);
		threeCopiesStatusList.add(Status.marked_failed);
		
		List<Request> allNotCompletedRequestList = requestDao.findAllByActionIdAndStatusInAndType(Action.ingest, threeCopiesStatusList, RequestType.system);
		for (Request request : allNotCompletedRequestList) {
			int requestId = request.getId();
			try {
				ArtifactRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(Domain.ONE);
				org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact artifactFromDB = artifactRepository.findTopByWriteRequestIdOrderByIdAsc(requestId);
				int artifactId = artifactFromDB.getId();
				
				List<Job> writeJobList = jobDao.findAllByRequestIdAndInputArtifactIdAndStoragetaskActionIdAndStatus(requestId, artifactId, Action.write, Status.completed);
				if(writeJobList.size() == 3)
					requestList.add(request);
			}
			catch (Exception e) {
				logger.warn("Skipping " + requestId + " : " + e.getMessage());
			}
		}
		
		for (Request request : requestList) {
			try {
				String artifactclassId = request.getDetails().getArtifactclassId();
				Domain domain = domainUtil.getDomain(artifactclassId);
				ArtifactRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);
				org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact artifactFromDB = artifactRepository.findTopByWriteRequestIdOrderByIdAsc(request.getId());
				int artifactId = artifactFromDB.getId();
				String artifactName = artifactFromDB.getName();
				
				Artifact artifact = new Artifact();
				artifact.setName(artifactName);
				artifact.setArtifactclass(artifactFromDB.getArtifactclass().getId());
				artifact.setTotalSize(artifactFromDB.getTotalSize());
				artifact.setCompletedAt(getDateForUI(request.getCompletedAt()));
				artifact.setFileCount(artifactFromDB.getFileCount());
				
				List<File> fileList = new ArrayList<File>();
				List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList = fileRepositoryUtil.getArtifactFileList(artifactFromDB, domain);
				for (org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile : artifactFileList) {
					String filePathname = nthFile.getPathname();
					if(listFiles || (!listFiles && filePathname.equals(artifactName))) {
						File file = new File();
						
						file.setId(nthFile.getId());
						file.setPathname(filePathname);
						file.setSize(nthFile.getSize());
						
						fileList.add(file);
						
						if(!listFiles)
							break;
					}
				}
				artifact.setFile(fileList);
				
				if(needVolumeDetails) {
					Volume volume = new Volume();
					Job job = jobDao.findByRequestIdAndInputArtifactIdAndStoragetaskActionIdAndGroupVolumeCopyId(request.getId(), artifactId, Action.write, copyNumber);
					String volumeId = job.getVolume().getId();
					volume.setBarcode(volumeId);
					ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
					ArtifactVolume artifactVolume = domainSpecificArtifactVolumeRepository.findByIdArtifactIdAndIdVolumeId(artifactId, volumeId);
					if(artifactVolume != null)
						volume.setStartBlock(artifactVolume.getDetails().getStartVolumeBlock());
					
					artifact.setVolume(volume);
				}
				artifactList.add(artifact);
			}
			catch (Exception e) {
				logger.warn("Skipped getting data for request " + request.getId());
				logger.error("Skipped getting data for request " + request.getId() + " " + e.getMessage(), e);
			}
		}
		ltoWalaResponse.setArtifact(artifactList);
		
		
		return ltoWalaResponse;
	}
	
	protected String getDateForUI(LocalDateTime _tedAt) { // requestedAt, createdAt, startedAt
		String dateForUI = null;
		if(_tedAt != null) {
			ZonedDateTime zdt = _tedAt.atZone(ZoneId.of("UTC"));
			dateForUI = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").format(zdt);
		}
		return dateForUI;
	}
}
