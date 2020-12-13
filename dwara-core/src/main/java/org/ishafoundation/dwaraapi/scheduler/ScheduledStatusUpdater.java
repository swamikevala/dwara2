package org.ishafoundation.dwaraapi.scheduler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TFileJobDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileJob;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.CoreFlow;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.ishafoundation.dwaraapi.process.thread.ProcessingJobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ScheduledStatusUpdater {
	
	private static final Logger logger = LoggerFactory.getLogger(ScheduledStatusUpdater.class);

	@Autowired
	private JobCreator jobCreator;
	
	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private RequestDao requestDao;

	@Autowired
	private TFileJobDao tFileJobDao;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Value("${scheduler.statusUpdater.enabled:true}")
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
		
		List<Status> statusList = new ArrayList<Status>();
		statusList.add(Status.in_progress);
		statusList.add(Status.queued);
		
		List<Request> systemRequestList = requestDao.findAllByTypeAndStatusIn(RequestType.system, statusList);
		//updateDependentJobsStatus(systemRequestList);
		updateSystemRequestStatus(systemRequestList);
		
		List<Request> userRequestList = requestDao.findAllByTypeAndStatusIn(RequestType.user, statusList);
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
//						job.setCompletedAt(LocalDateTime.now()); // Just can only give some rough completed times... 
						status = Status.completed;
					}
					if(hasFailures) {
						if(hasAnyCompleted)
							status = Status.completed_failures;
						else
							status = Status.failed;
					}
					job.setCompletedAt(LocalDateTime.now()); // Just can only give some rough completed times... 
					job.setStatus(status);
					jobDao.save(job);
					logger.info("Job " + job.getId() + " - " + status);
					
					if(status == Status.completed) {
						tFileJobDao.deleteAll(jobFileList);
						logger.info("tFileJob cleaned up files of Job " + job.getId());

						jobCreator.createDependentJobs(job);
						
						// if the processing job has a dependency on restore - then delete the restored file from the tmp directory
						ProcessingJobManager processingJobManager = applicationContext.getBean(ProcessingJobManager.class);
						String inputPath = processingJobManager.getInputPath(job);
						if(inputPath != null) {
							Request request = job.getRequest();
							RequestDetails requestDetails = request.getDetails();
							org.ishafoundation.dwaraapi.enumreferences.Action requestedAction = request.getActionId();
							if(requestedAction == Action.restore_process && CoreFlow.core_restore_checksumverify_flow.getFlowName().equals(request.getDetails().getFlow())){
								// what need to be restored
								int fileIdRestored = requestDetails.getFileId();
								
								String restoredFilePathName = null;
								
						    	Domain[] domains = Domain.values();
					    		for (Domain nthDomain : domains) {
					    			org.ishafoundation.dwaraapi.db.model.transactional.domain.File file = domainUtil.getDomainSpecificFile(nthDomain, fileIdRestored);
					    			if(file != null) {
					    				restoredFilePathName = file.getPathname();
					    				break;
					    			}
								}
								// inputPath = something like - /data/restored/someoutputfolder/.restoring
								String srcPath = inputPath + java.io.File.separator + restoredFilePathName;
								String destPath = srcPath.replace(java.io.File.separator + configuration.getRestoreInProgressFileIdentifier(), "");	
								logger.trace("src " + srcPath);
								logger.trace("dest " + destPath);

					    		try {
									java.io.File srcFile = new java.io.File(srcPath);
									java.io.File destFile = new java.io.File(destPath);
							
									if(srcFile.isFile())
										Files.createDirectories(Paths.get(FilenameUtils.getFullPathNoEndSeparator(destPath)));		
									else
										Files.createDirectories(Paths.get(destPath));
								
										Files.move(srcFile.toPath(), destFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
										logger.info("Moved restored files from " + srcPath + " to " + destPath);
									}
								catch (Exception e) {
									logger.error("Unable to move files from " + srcPath + " to " + destPath);
								}
							}
							else if(requestedAction == Action.ingest || requestedAction == Action.restore_process) {
								// inputPath = something like - /data/tmp/job-1234
								File restoreTmpFolder = new File(inputPath);
								restoreTmpFolder.delete();
							}
						}
					}
				}
			}
		}
	}
	
	private void updateSystemRequestStatus(List<Request> requestList) {
		for (Request nthRequest : requestList) {
			List<Job> nthRequestJobs = jobDao.findAllByRequestId(nthRequest.getId());
			
			/**
			 * 
			 * in_progress
				queued
				on_hold
				cancelled
				completed_failures
				failed
				completed
				*/
			boolean anyInProgress = false;
			boolean anyQueued = false;
			boolean anyOnHold = false;
			boolean anyCompletedWithFailures = false;
			boolean hasFailures = false;
			boolean isAllComplete = true;
			boolean isAllCancelled = true;
						
			for (Job nthJob : nthRequestJobs) {
				Status status = nthJob.getStatus();
				switch (status) {
					case in_progress:
						anyInProgress = true;
						isAllComplete = false;
						isAllCancelled = false;
						break;
					case queued:
						anyQueued = true;
						isAllComplete = false;
						isAllCancelled = false;
						break;
					case on_hold:
						anyOnHold = true;
						isAllComplete = false;
						isAllCancelled = false;
						break;
					case completed_failures:
						anyCompletedWithFailures = true;
						isAllComplete = false;
						isAllCancelled = false;						
						break;
					case failed:
						hasFailures = true;
						isAllComplete = false;
						isAllCancelled = false;						
						break;
					case marked_completed:
					case completed:
						isAllCancelled = false;
						break;						
					case cancelled:
						isAllComplete = false;
						break;
					default:
						break;
				}
			}
			
			/**
			 * 
			 * in_progress
				queued
				on_hold
				completed_failures
				failed
				completed
				cancelled
				*/
			Status status = Status.queued;
			if(anyInProgress) {
				status = Status.in_progress;
			}
			else if(anyQueued) {
				status = Status.queued; 
			}
			else if(anyOnHold) {
				status = Status.on_hold; 
			}
			else if(anyCompletedWithFailures) {
				status = Status.completed_failures; 
			}
			else if(hasFailures) {
				status = Status.failed;
			}
			else if(isAllCancelled) {
				status = Status.cancelled;
			}
			else if(isAllComplete) { // All jobs have successfully completed.
				status = Status.completed; 
			}
			
			logger.trace("System request status - " + nthRequest.getId() + " ::: " + status);
			
			nthRequest.setStatus(status); 
			requestDao.save(nthRequest);
			
			if(nthRequest.getActionId() == Action.ingest && status == Status.completed) {
				
				Domain domain = domainUtil.getDomain(nthRequest);
				ArtifactRepository<Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);

				Artifact artifact = artifactRepository.findTopByWriteRequestIdOrderByIdAsc(nthRequest.getId()); 
				Artifactclass artifactclass = artifact.getArtifactclass();
				String srcRootLocation = artifactclass.getPathPrefix();

				if(srcRootLocation != null) {
					try {
						java.io.File srcFile = FileUtils.getFile(srcRootLocation, artifact.getName());
						java.io.File destFile = FileUtils.getFile(configuration.getIngestCompleteDirRoot(), artifact.getName());

						if(srcFile.isFile())
							//Files.createDirectories(Paths.get(FilenameUtils.getFullPathNoEndSeparator(destFile.getAbsolutePath())));		
							Files.createDirectories(Paths.get(configuration.getIngestCompleteDirRoot()));
						else
							Files.createDirectories(destFile.toPath());
		
						Files.move(srcFile.toPath(), destFile.toPath(), StandardCopyOption.ATOMIC_MOVE); // what will be the timestamp of this moved file?
					}
					catch (Exception e) {
						logger.error("Unable to move file "  + e.getMessage());
					}
				}
			}
		}
	}

	
	private void updateUserRequestStatus(List<Request> userRequestList) {
		for (Request nthUserRequest : userRequestList) {
			int userRequestId = nthUserRequest.getId();
			List<Request> systemRequestList = requestDao.findAllByRequestRefId(userRequestId);

			/**
			 * 
			 * in_progress
				queued
				on_hold
				completed_failures
				failed
				completed
				cancelled
				*/
			boolean anyInProgress = false;
			boolean anyQueued = false;
			boolean anyOnHold = false;
			boolean anyCompletedWithFailures = false;
			boolean hasFailures = false;
			boolean isAllComplete = true;
			boolean isAllCancelled = true;
						
			for (Request nthSystemRequest : systemRequestList) {
			Status status = nthSystemRequest.getStatus();
				switch (status) {
					case in_progress:
						anyInProgress = true;
						isAllComplete = false;
						isAllCancelled = false;
						break;
					case queued:
						anyQueued = true;
						isAllComplete = false;
						isAllCancelled = false;
						break;
					case on_hold:
						anyOnHold = true;
						isAllComplete = false;
						isAllCancelled = false;
						break;
					case completed_failures:
						anyCompletedWithFailures = true;
						isAllComplete = false;
						isAllCancelled = false;						
						break;
					case failed:
						hasFailures = true;
						isAllComplete = false;
						isAllCancelled = false;						
						break;
					case marked_completed:
					case completed:
						isAllCancelled = false;
						break;						
					case cancelled:
						isAllComplete = false;
						break;
					default:
						break;
				}
			}
			
			/**
			 * 
			 * in_progress
				queued
				on_hold
				completed_failures
				failed
				completed
				cancelled
				*/
			Status status = Status.queued;
			if(anyInProgress) {
				status = Status.in_progress;
			}
			else if(anyQueued) {
				status = Status.queued; 
			}
			else if(anyOnHold) {
				status = Status.on_hold; 
			}
			else if(anyCompletedWithFailures) { // Some jobs have successfully completed, and some were skipped, or failed and then marked completed.
				status = Status.completed_failures; 
			}
			else if(hasFailures) {
				status = Status.failed;
			}
			else if(isAllCancelled) {
				status = Status.cancelled;
			}
			else if(isAllComplete) { // All jobs have successfully completed.
				status = Status.completed; 
			}
			
			logger.trace("User request status - " + nthUserRequest.getId() + " ::: " + status);
			
			// For completed restore jobs the .restoring folder is cleaned up...
			if((nthUserRequest.getActionId() == Action.restore || nthUserRequest.getActionId() == Action.restore_process) && status == Status.completed) {
				JsonNode jsonNode = nthUserRequest.getDetails().getBody();
				String outputFolder = jsonNode.get("outputFolder").asText();
				String destinationPath = jsonNode.get("destinationPath").asText();
				File restoreTmpFolder = FileUtils.getFile(destinationPath , outputFolder, configuration.getRestoreInProgressFileIdentifier());
				restoreTmpFolder.delete();
			}
			nthUserRequest.setStatus(status); 
			requestDao.save(nthUserRequest);
		}
	}
}