package org.ishafoundation.dwaraapi.scheduler;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TFileJobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileJob;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

@Component
public class ScheduledStatusUpdater {
	
	private static final Logger logger = LoggerFactory.getLogger(ScheduledStatusUpdater.class);

	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private RequestDao requestDao;

	@Autowired
	private TFileJobDao tFileJobDao;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Value("${scheduler.enabled:true}")
	private boolean isEnabled;
	
	@Scheduled(fixedDelay = 60000)
    @PostMapping("/updateStatus")
    public ResponseEntity<String> updateStatus(){
    	if(isEnabled) {
    		updateTransactionalTablesStatus();
	    	return ResponseEntity.status(HttpStatus.OK).body("Done");
    	}
    	else
    		return null; 
    }
	
	public void updateTransactionalTablesStatus() {
		updateProcessingJobsStatus();
		
		List<Request> systemRequestList = requestDao.findAllByTypeAndStatus(RequestType.system, Status.in_progress);
		updateDependentJobsStatus(systemRequestList);
		updateSystemRequestStatus(systemRequestList);
		
		List<Request> userRequestList = requestDao.findAllByTypeAndStatus(RequestType.user, Status.in_progress);
		updateUserRequestStatus(userRequestList);
	}
	
	// On a job level only processing jobs need consolidated status update based on the files processing status
	// The storage jobs are supposed to be marked completed then and there.
	private void updateProcessingJobsStatus() {
		List<Job> jobList = jobDao.findAllByStatusAndProcessingtaskIdIsNotNullOrderById(Status.in_progress);
		for (Iterator<Job> iterator = jobList.iterator(); iterator.hasNext();) {
			Job job = (Job) iterator.next();
			if(job.getProcessingtaskId() != null){ // consolidated status update needed only for process jobs...
				boolean queued = false;
				boolean inProgress = false;
				boolean hasFailures = false;
				boolean hasAnyCompleted = false;
				boolean isAllComplete = true;
				List<TFileJob> jobFileList = tFileJobDao.findAllByJobId(job.getId()); 
				for (Iterator<TFileJob> iterator2 = jobFileList.iterator(); iterator2.hasNext();) {
					TFileJob jobFile = (TFileJob) iterator2.next();
					Status status = jobFile.getStatus();
					if(status == Status.queued) {
						queued = true;
						isAllComplete = false;
						break;
					}
					else if(status == Status.in_progress) {
						inProgress = true;
						isAllComplete = false;
						break;
					}
					else if(status == Status.failed) {
						isAllComplete = false;
						hasFailures = true;
					}
					else if(status == Status.completed) {
						hasAnyCompleted = true;
					}
				}
				
				if(!queued && !inProgress) {
					//Status.cancelled; Status.skipped
					Status status = null;
					if(isAllComplete) {
						job.setCompletedAt(LocalDateTime.now()); // Just can only give some rough completed times... 
						status = Status.completed;
					}
					if(hasFailures) {
						if(hasAnyCompleted)
							status = Status.completed_failures;
						else
							status = Status.failed;
					}
					job.setStatus(status);
					jobDao.save(job);
					logger.info("Job " + job.getId() + " - " + status);
					
					if(status == Status.completed) {
						tFileJobDao.deleteAll(jobFileList);
						logger.info("tFileJob cleaned up files of Job " + job.getId());
					}
				}
			}
		}
	}
	
	private void updateDependentJobsStatus(List<Request> requestList) {
//		for (Request nthRequest : requestList) {
//			List<Job> nthRequestJobs = jobDao.findAllByRequestId(nthRequest.getId());
//			
//			for (Job nthJob : nthRequestJobs) {
//				jobUtil.getDependentJobs(nthJob);
//				
//				Status status = nthJob.getStatus();
//				if(nthJob)
//			}
//		}
	}
	
	private void updateSystemRequestStatus(List<Request> requestList) {
		for (Request nthRequest : requestList) {
			List<Job> nthRequestJobs = jobDao.findAllByRequestId(nthRequest.getId());
			
			boolean anyQueued = false;
			boolean anyInProgress = false;
			boolean anyComplete = false;
			boolean hasFailures = false;
			boolean anyMarkedCompleted = false;
			boolean isAllQueued = true;
			boolean isAllComplete = true;
			boolean isAllCancelled = true;
						
			for (Job nthJob : nthRequestJobs) {
				Status status = nthJob.getStatus();
				switch (status) {
					case queued:
						anyQueued = true;
						isAllComplete = false;
						isAllCancelled = false;
						break;
					case in_progress:
						anyInProgress = true;
						isAllQueued = false;
						isAllComplete = false;
						isAllCancelled = false;
						break;
					case completed:
						anyComplete = true;
						isAllQueued = false;
						isAllCancelled = false;
						break;						
					case cancelled:
						isAllQueued = false;
						isAllComplete = false;
						break;
					case failed:
						hasFailures = true;
						isAllQueued = false;
						isAllComplete = false;
						isAllCancelled = false;						
						break;
					case completed_failures:
					case marked_completed:
						anyMarkedCompleted = true;
						isAllQueued = false;
						isAllComplete = false;
						isAllCancelled = false;						
						break;
					default:
						break;
				}
			}

			logger.trace(nthRequest.getId() + " ::: " + "isAllQueued - " + isAllQueued + " isAllComplete - " + isAllComplete + " isAllCancelled - " + isAllCancelled + " anyQueued - " + 
			anyQueued + " anyInProgress - " + anyInProgress + " anyComplete - " + anyComplete + " hasFailures - " + hasFailures + " anyMarkedCompleted - " + anyMarkedCompleted);
			
			Status status = Status.queued;
			if(isAllQueued) {
				status = Status.queued; 
			}
			else if(isAllCancelled) {
				status = Status.cancelled;
			}
			else if(isAllComplete) { // All jobs have successfully completed.
				status = Status.completed; 
			}
			else if(anyQueued || anyInProgress) {
				status = Status.in_progress;
			}
			else if(hasFailures) {
				status = Status.failed;
			}
			else if(anyComplete && anyMarkedCompleted) { // Some jobs have successfully completed, and some were skipped, or failed and then marked completed.
				status = Status.partially_completed; 
			}
			
			logger.trace(nthRequest.getId() + " ::: " + status);
			
			nthRequest.setStatus(status); 
			requestDao.save(nthRequest);
			
			if(nthRequest.getActionId() == Action.ingest && status == Status.completed) {
				
				Domain domain = domainUtil.getDomain(nthRequest);
				ArtifactRepository<Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);

				Artifact artifact = artifactRepository.findByWriteRequestId(nthRequest.getId()); 

				String destRootLocation = artifact.getArtifactclass().getPathPrefix();

				if(destRootLocation != null) {
					try {
						java.io.File srcFile = FileUtils.getFile(artifact.getArtifactclass().getPathPrefix(), artifact.getName());
						java.io.File destFile = FileUtils.getFile(destRootLocation, status.name(), artifact.getName());

						if(srcFile.isFile())
							Files.createDirectories(Paths.get(FilenameUtils.getFullPathNoEndSeparator(destFile.getAbsolutePath())));		
						else
							Files.createDirectories(destFile.toPath());
		
						Files.move(srcFile.toPath(), destFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
					}
					catch (Exception e) {
						logger.error("Unable to move file "  + e.getMessage());
					}
				}
			}
		}
	}

	
	// TODO When updating the status ensure for restore jobs the .restoring folder is cleaned up...
	private void updateUserRequestStatus(List<Request> userRequestList) {
		for (Request nthUserRequest : userRequestList) {
			int userRequestId = nthUserRequest.getId();
			List<Request> systemRequestList = requestDao.findAllByRequestRefId(userRequestId);

			
			boolean anyQueued = false;
			boolean anyInProgress = false;
			boolean anyComplete = false;
			boolean hasFailures = false;
			boolean anyPartiallyCompleted = false;
			boolean isAllQueued = true;
			boolean isAllComplete = true;
			boolean isAllCancelled = true;
						
			for (Request nthSystemRequest : systemRequestList) {
			Status status = nthSystemRequest.getStatus();
				switch (status) {
					case queued:
						anyQueued = true;
						isAllComplete = false;
						isAllCancelled = false;
						break;
					case in_progress:
						anyInProgress = true;
						isAllQueued = false;
						isAllComplete = false;
						isAllCancelled = false;
						break;
					case completed:
						anyComplete = true;
						isAllQueued = false;
						isAllCancelled = false;
						break;						
					case cancelled:
						isAllQueued = false;
						isAllComplete = false;
						break;
					case failed:
						hasFailures = true;
						isAllQueued = false;
						isAllComplete = false;
						isAllCancelled = false;						
						break;
					case partially_completed:
						anyPartiallyCompleted = true;
						isAllQueued = false;
						isAllComplete = false;
						isAllCancelled = false;						
						break;
					default:
						break;
				}
			}
//
//			boolean anyQueued = false;
//			boolean anyInProgress = false;
//			boolean anyComplete = false;
//			boolean hasFailures = false;
//			boolean isAllComplete = true;
//			boolean isAllCancelled = true;
//			
//			for (Request nthSystemRequest : systemRequestList) {
//				Status status = nthSystemRequest.getStatus();
//				switch (status) {
//					case queued:
//						anyQueued = true;
//						isAllComplete = false;
//						isAllCancelled = false;
//						break;
//					case in_progress:
//						anyInProgress = true;
//						isAllComplete = false;
//						isAllCancelled = false;
//						break;
//					case completed:
//						anyComplete = true;
//						isAllCancelled = false;
//						break;
//					case cancelled:
//						isAllComplete = false;
//						break;
//					case failed:
//						hasFailures = true;
//						isAllComplete = false;
//						isAllCancelled = false;						
//						break;
//					default:
//						break;
//				}
//			}

			logger.trace(nthUserRequest.getId() + " ::: " + "isAllQueued - " + isAllQueued + " isAllComplete - " + isAllComplete + " isAllCancelled - " + isAllCancelled + " anyQueued - " + 
			anyQueued + " anyInProgress - " + anyInProgress + " anyComplete - " + anyComplete + " hasFailures - " + hasFailures + " anyPartiallyCompleted - " + anyPartiallyCompleted);
			
//			Status status = Status.queued;
//			if(anyInProgress) { // Some System Requests are running
//				status = Status.in_progress; 
//			}
//			else if(anyQueued && !anyInProgress) { // Some System Requests are queued, and none are in progress
//				status = Status.queued; 
//			}
//			else if(isAllCancelled) {
//				status = Status.cancelled;
//			}
//			else if(isAllComplete) { // All System Requests have successfully completed.
//				status = Status.completed; 
//			}
//			else if(hasFailures) {
//				status = Status.failed;
//			}
//			else if(anyComplete) { // Some System Requests have successfully completed, but some are cancelled
//				status = Status.partially_completed; 
//			}

			Status status = Status.queued;
			if(isAllQueued) {
				status = Status.queued; 
			}
			else if(isAllCancelled) {
				status = Status.cancelled;
			}
			else if(isAllComplete) { // All jobs have successfully completed.
				status = Status.completed; 
			}
			else if(anyQueued || anyInProgress) {
				status = Status.in_progress;
			}
			else if(hasFailures) {
				status = Status.failed;
			}
			else if(anyComplete && anyPartiallyCompleted) { // Some jobs have successfully completed, and some were skipped, or failed and then marked completed.
				status = Status.partially_completed; 
			}
			
			logger.trace(nthUserRequest.getId() + " ::: " + status);
			
			nthUserRequest.setStatus(status); 
			requestDao.save(nthUserRequest);
		}
	}
}