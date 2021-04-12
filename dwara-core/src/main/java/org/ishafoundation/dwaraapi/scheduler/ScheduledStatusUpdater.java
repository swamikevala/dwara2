package org.ishafoundation.dwaraapi.scheduler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TTFileJobDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TTFileJob;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.CoreFlow;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.ishafoundation.dwaraapi.process.thread.ProcessingJobManager;
import org.ishafoundation.dwaraapi.staged.StagedFileOperations;
import org.ishafoundation.dwaraapi.staged.scan.StagedFileEvaluator;
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
	private VolumeDao volumeDao;
	
	@Autowired
	private RequestDao requestDao;

	@Autowired
	private TTFileJobDao tFileJobDao;
	
	@Autowired
	private TFileDao tFileDao;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
    private StagedFileOperations stagedFileOperations;
	
	@Autowired
    private StagedFileEvaluator stagedFileEvaluator;
	
	@Value("${scheduler.statusUpdater.enabled:true}")
	private boolean isEnabled;
	
	@Scheduled(fixedDelayString = "${scheduler.statusUpdater.fixedDelay}")
    @PostMapping("/updateStatus")
    public ResponseEntity<String> updateStatus(){
    	if(isEnabled) {
    		logger.info("***** Updating Status now *****");
    		try {
    			updateTransactionalTablesStatus();
    		}
    		catch (Exception e) {
    			logger.error("Unable to update status " + e.getMessage());
			}
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
		statusList.add(Status.on_hold);
		
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
				List<TTFileJob> jobFileList = tFileJobDao.findAllByJobId(job.getId()); 
				for (Iterator<TTFileJob> iterator2 = jobFileList.iterator(); iterator2.hasNext();) {
					TTFileJob jobFile = (TTFileJob) iterator2.next();
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
					job = jobDao.findById(job.getId()).get(); // getting the job again so if processing job processor has updated the outputartifactid, it picks it up too...
					job.setCompletedAt(LocalDateTime.now()); // Just can only give some rough completed times... 
					job.setStatus(status);
					job = jobDao.save(job);
					logger.info("Job " + job.getId() + " - " + status);
					
					if(status == Status.failed) { // When a processing task involving a volume failed mark the tape suspsect. For e.g checksum-veriy processing task fails then we do below...
						Volume volume = job.getVolume();
						if(volume != null) {
							volume.setSuspect(true);
							volumeDao.save(volume);
							logger.info("Marked the volume " + volume.getId() + " as suspect");
						}
							
					}
					else if(status == Status.completed) {
						tFileJobDao.deleteAll(jobFileList);
						logger.info("tFileJob cleaned up files of Job " + job.getId());

						jobCreator.createDependentJobs(job);

						Request request = job.getRequest();
						RequestDetails requestDetails = request.getDetails();
						org.ishafoundation.dwaraapi.enumreferences.Action requestedAction = request.getActionId();

						if(requestedAction == Action.ingest) {
							Integer inputArtifactId = job.getInputArtifactId(); // For processing tasks like file-deleter and file-mover we need to update the count and size
							
							Integer outputArtifactId = job.getOutputArtifactId();
							if(outputArtifactId != null && !outputArtifactId.equals(inputArtifactId)) {
								updateArtifactSizeAndCount(job, outputArtifactId);
							}
	
							updateArtifactSizeAndCount(job, inputArtifactId);
						
							// TODO : Digi hack - the permissions script assumes the input artifact folder is "staged" and hence only supports Digitization A/Cs
							if(outputArtifactId != null) {
								org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact artifact = domainUtil.getDomainSpecificArtifact(outputArtifactId);
								String pathPrefix = artifact.getArtifactclass().getPath();
								String staged = "/staged";
								if(pathPrefix.contains(staged))
									stagedFileOperations.setPermissions(StringUtils.substringBefore(pathPrefix, staged), false, artifact.getName());
							}
						}
						// if the processing job has a dependency on restore - then delete the restored file from the tmp directory
						ProcessingJobManager processingJobManager = applicationContext.getBean(ProcessingJobManager.class);
						String inputPath = processingJobManager.getInputPath(job);
						if(inputPath != null) {
							if(requestedAction == Action.restore_process && CoreFlow.core_restore_checksumverify_flow.getFlowName().equals(request.getDetails().getFlowId())){
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
								try {
									FileUtils.deleteDirectory(restoreTmpFolder);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void updateArtifactSizeAndCount(Job job, int artifactId) {
		org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact artifact = domainUtil.getDomainSpecificArtifact(artifactId);
		
		Path artifactPath = Paths.get(artifact.getArtifactclass().getPath(), artifact.getName());
		File artifactFileObj = artifactPath.toFile();
		long artifactSize = 0;
		int artifactFileCount = 0;
		
	    if(artifactFileObj.isDirectory()) {
	        org.ishafoundation.dwaraapi.staged.scan.ArtifactFileDetails afd = stagedFileEvaluator.getDetails(artifactFileObj);
	        artifactFileCount = afd.getCount();
	        artifactSize = afd.getTotalSize();
	    }
	    else {
	    	// TODO for single file artifacts...
	    }
	    logger.debug("artifactSize old " + artifact.getTotalSize() + " artifactSize new " + artifactSize);
	    artifact.setTotalSize(artifactSize);
		artifact.setFileCount(artifactFileCount);
		
		ArtifactRepository<Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(artifact.getArtifactclass().getDomain());
		artifact = (Artifact) artifactRepository.save(artifact);
		
		FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(artifact.getArtifactclass().getDomain());
		org.ishafoundation.dwaraapi.db.model.transactional.domain.File artifactFileFromDB = domainSpecificFileRepository.findByPathname(artifact.getName());
		artifactFileFromDB.setSize(artifactSize);
		domainSpecificFileRepository.save(artifactFileFromDB);
		
		TFile artifactTFileFromDB = tFileDao.findByPathname(artifact.getName());
		if(artifactTFileFromDB != null) {
			artifactTFileFromDB.setSize(artifactSize);
			tFileDao.save(artifactTFileFromDB);
		}
		
		// TODO : Digi hack - clean this up -Long term - iterate through files that are directories and calc their size and update them...
		if("file-delete".equals(job.getProcessingtaskId()) && artifact.getArtifactclass().getId().startsWith("video-digi-2020-")) {
			Path artifactMxfSubfolderPath = Paths.get(artifactPath.toString(), "mxf");
			File artifactMxfSubfolderObj = artifactMxfSubfolderPath.toFile();
		    if(artifactMxfSubfolderObj.isDirectory()) {
		    	long artifactMxfSubfolderSize = FileUtils.sizeOfDirectory(artifactMxfSubfolderObj);
		    	Path artifactMxfSubfolderFilePath = Paths.get(artifact.getName(), "mxf");
				org.ishafoundation.dwaraapi.db.model.transactional.domain.File artifactMxfSubfolderFileFromDB = domainSpecificFileRepository.findByPathname(artifactMxfSubfolderFilePath.toString());
				artifactMxfSubfolderFileFromDB.setSize(artifactMxfSubfolderSize);
				domainSpecificFileRepository.save(artifactMxfSubfolderFileFromDB);
				
		    	TFile tfileFromDB = tFileDao.findByPathname(artifactMxfSubfolderFilePath.toString());
		    	tfileFromDB.setSize(artifactMxfSubfolderSize);
		    	tFileDao.save(tfileFromDB);
		    }
		}
			
		
	}
	
	private void updateSystemRequestStatus(List<Request> requestList) {
		for (Request nthRequest : requestList) {
			List<Job> nthRequestJobs = jobDao.findAllByRequestId(nthRequest.getId());
			// Fix for no job created usecase
			if(nthRequestJobs.size() == 0)
				continue;
				
			List<Status> jobStatusList = new ArrayList<Status>();
			for (Job nthJob : nthRequestJobs) {
				Status nthJobStatus = nthJob.getStatus();
				jobStatusList.add(nthJobStatus);
			}	
			
			Status status = getStatus(jobStatusList);
			logger.trace("System request status - " + nthRequest.getId() + " ::: " + status);
			nthRequest.setStatus(status);
			
			if(status != Status.queued && status != Status.in_progress && status != Status.on_hold)
				nthRequest.setCompletedAt(LocalDateTime.now());
 
			requestDao.save(nthRequest);
			
			if(nthRequest.getActionId() == Action.ingest && (status == Status.marked_completed || status == Status.completed)) {
				
				Domain domain = domainUtil.getDomain(nthRequest);
				ArtifactRepository<Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);
				
		    	List<Artifact> artifactList = artifactRepository.findAllByWriteRequestId(nthRequest.getId());
		    	for (Artifact artifact : artifactList) {
					Artifactclass artifactclass = artifact.getArtifactclass();
					String srcRootLocation = artifactclass.getPath();

					if(artifactclass.isSource()){ // source artifacts need to be moved to configured ingest completed location something like "/data/ingested" 
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
					else { // derived artifacts can be deleted
						if(srcRootLocation != null) {
							try {
								java.io.File srcFile = FileUtils.getFile(srcRootLocation, artifact.getName());

								if(srcFile.isFile())
									srcFile.delete();
								else
									FileUtils.deleteDirectory(srcFile);
							}
							catch (Exception e) {
								logger.error("Unable to delete file "  + e.getMessage());
							}
						}
					}
				}
			}
		}
	}

	
	private void updateUserRequestStatus(List<Request> userRequestList) {
		for (Request nthUserRequest : userRequestList) {
			int userRequestId = nthUserRequest.getId();
			List<Request> systemRequestList = requestDao.findAllByRequestRefId(userRequestId);
			
			List<Status> systemRequestStatusList = new ArrayList<Status>();
			for (Request nthSystemRequest : systemRequestList) {
				Status nthSystemRequestStatus = nthSystemRequest.getStatus();
				systemRequestStatusList.add(nthSystemRequestStatus);
			}	
		
			Status status = getStatus(systemRequestStatusList);
			
			logger.trace("User request status - " + nthUserRequest.getId() + " ::: " + status);
			
			// For completed restore jobs the .restoring folder is cleaned up...
			if((nthUserRequest.getActionId() == Action.restore || nthUserRequest.getActionId() == Action.restore_process) && status == Status.completed) {
				JsonNode jsonNode = nthUserRequest.getDetails().getBody();
				String outputFolder = jsonNode.get("outputFolder").asText();
				String destinationPath = jsonNode.get("destinationPath").asText();
				File restoreTmpFolder = FileUtils.getFile(destinationPath , outputFolder, configuration.getRestoreInProgressFileIdentifier());
				try {
					FileUtils.deleteDirectory(restoreTmpFolder);
					logger.trace(restoreTmpFolder.getPath() + " deleted succesfully");
				} catch (IOException e) {
					logger.error("Unable to delete " + restoreTmpFolder.getPath());
				}
				
			}
			nthUserRequest.setStatus(status); 
			
			if(status != Status.queued && status != Status.in_progress && status != Status.on_hold)
				nthUserRequest.setCompletedAt(LocalDateTime.now());

			requestDao.save(nthUserRequest);
		}
	}
	
	private Status getStatus(List<Status> entityStatusList) {
		boolean anyInProgress = false;
		boolean anyQueued = false;
		boolean anyOnHold = false;
		boolean anyCancelled = false;
		boolean anyCompletedWithFailures = false;
		boolean hasFailures = false;
		boolean anyMarkedCompleted = false;
		boolean isAllComplete = true;
					
		for (Status status : entityStatusList) {
			switch (status) {
				case in_progress:
					anyInProgress = true;
					isAllComplete = false;
					break;
				case queued:
					anyQueued = true;
					isAllComplete = false;
					break;
				case on_hold:
					anyOnHold = true;
					isAllComplete = false;
					break;
				case cancelled:
					anyCancelled = true;
					isAllComplete = false;
					break;
				case completed_failures:
					anyCompletedWithFailures = true;
					isAllComplete = false;
					break;
				case failed:
					hasFailures = true;
					isAllComplete = false;
					break;
				case marked_completed:
					anyMarkedCompleted = true;
					isAllComplete = false;
					break;
				case completed:
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
			cancelled
			failed
			completed_failures
			marked_completed
			completed
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
		else if(anyCancelled) {
			status = Status.cancelled;
		}
		else if(hasFailures) {
			status = Status.failed;
		}
		else if(anyCompletedWithFailures) {
			status = Status.completed_failures; 
		}
		else if(anyMarkedCompleted) {
			status = Status.marked_completed;
		}
		else if(isAllComplete) { // All jobs have successfully completed.
			status = Status.completed; 
		}
		
		return status;
	}
}