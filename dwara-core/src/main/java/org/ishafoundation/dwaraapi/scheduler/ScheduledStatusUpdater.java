package org.ishafoundation.dwaraapi.scheduler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.artifact.ArtifactUtil;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.ArtifactclassDao;
import org.ishafoundation.dwaraapi.db.dao.master.DestinationDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TArtifactnameJobMapDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.ArtifactVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TTFileJobDao;
import org.ishafoundation.dwaraapi.db.keys.VolumeArtifactServerNameKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.TArtifactnameJobMap;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TTFileJob;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.ArtifactVolumeStatus;
import org.ishafoundation.dwaraapi.enumreferences.CoreFlowelement;
import org.ishafoundation.dwaraapi.enumreferences.JiraTransition;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.RewriteMode;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.VolumeHealthStatus;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.ishafoundation.dwaraapi.process.thread.ProcessingJobManager;
import org.ishafoundation.dwaraapi.service.TFileVolumeDeleter;
import org.ishafoundation.dwaraapi.service.UserRequestHelper;
import org.ishafoundation.dwaraapi.staged.StagedFileOperations;
import org.ishafoundation.dwaraapi.staged.scan.StagedFileEvaluator;
import org.ishafoundation.dwaraapi.storage.storagetask.Restore;
import org.ishafoundation.dwaraapi.utils.CpProxyServerInteracter;
import org.ishafoundation.dwaraapi.utils.JiraUtil;
import org.ishafoundation.dwaraapi.utils.SMSUtil;
import org.ishafoundation.dwaraapi.utils.StatusUtil;
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
	private FileDao fileDao;

	@Autowired
	private ArtifactDao artifactDao;
		
	@Autowired
	private TArtifactnameJobMapDao tArtifactnameJobMapDao;

	@Autowired
	private ArtifactVolumeDao artifactVolumeDao;

	@Autowired
	private Configuration configuration;

	@Value("${restoreTapesNotifier.mobileNos}")
	private String commaSeparatedMobileNos;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private StagedFileOperations stagedFileOperations;

	@Autowired
	private StagedFileEvaluator stagedFileEvaluator;

	@Autowired
	private TFileVolumeDeleter tFileVolumeDeleter;

	@Autowired
	private UserRequestHelper userRequestHelper;

	@Autowired
	private Restore restoreStorageTask;
	
	@Autowired
	private CpProxyServerInteracter cpProxyServerInteracter;

	@Autowired
	private ArtifactclassDao artifactClassDao;

	@Autowired
	private DestinationDao destinationDao;

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
				logger.error("Unable to update status " + e.getMessage(), e);
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
							volume.setHealthstatus(VolumeHealthStatus.suspect);
							volumeDao.save(volume);
							logger.info("Marked the volume " + volume.getId() + " as suspect");

							// create user request for tracking
							HashMap<String, Object> data = new HashMap<String, Object>();
							data.put("volumeId", volume.getId());
							data.put("status", VolumeHealthStatus.suspect);
							String reason = "Repeated failure on processing job " + job.getId();
							data.put("reason", reason);
							userRequestHelper.createUserRequest(Action.mark_volume, DwaraConstants.SYSTEM_USER_NAME, Status.completed, data, reason);

						}

					}
					else if(status == Status.completed) {
						tFileJobDao.deleteAll(jobFileList);
						logger.info("tFileJob cleaned up files of Job " + job.getId());

						try {
							jobCreator.createDependentJobs(job);
						}catch (Exception e) {
							logger.error("Unable to create dependent jobs " + e.getMessage(), e);
						}
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
								org.ishafoundation.dwaraapi.db.model.transactional.Artifact artifact = artifactDao.findById(outputArtifactId).get();
								String pathPrefix = artifact.getArtifactclass().getPath();
								String staged = "/staged";
								if(pathPrefix.contains(staged))
									stagedFileOperations.setPermissions(StringUtils.substringBefore(pathPrefix, staged), false, artifact.getName());
							}
						}
						// if the processing job has a dependency on restore - then delete the restored file from the tmp directory
						ProcessingJobManager processingJobManager = applicationContext.getBean(ProcessingJobManager.class);
						if(requestedAction == Action.rewrite) {
							String flowelementId = job.getFlowelementId();
							if(flowelementId != null) {
								CoreFlowelement coreFlowelement = CoreFlowelement.findById(flowelementId);
								if(coreFlowelement == CoreFlowelement.core_rewrite_flow_checksum_verify) { // last of the 2 verify jobs
									// delete the restored file from the tmp directory
									String inputPath = processingJobManager.getInputPath(job);
									File restoreTmpFolder = new File(inputPath);
									try {
										FileUtils.deleteDirectory(restoreTmpFolder);
									} catch (IOException e) {
										logger.error("Unable to delete directory " + restoreTmpFolder + " : " + e.getMessage(), e);
									}

									Integer artifactId = job.getInputArtifactId();

									// update the status of the defective/migrated artifact/volume
									Integer rewriteCopy = job.getRequest().getDetails().getRewriteCopy();
									RewriteMode rewritePurpose = job.getRequest().getDetails().getMode();
									if(rewriteCopy != null) { // if rewrite artifact
										List<ArtifactVolume> artifactVolumeList = artifactVolumeDao.findAllByIdArtifactIdAndStatus(artifactId, ArtifactVolumeStatus.current);
										for (ArtifactVolume nthArtifactVolume : artifactVolumeList) {
											if(nthArtifactVolume.getVolume().getGroupRef().getCopy().getId() == rewriteCopy && nthArtifactVolume.getVolume().getId() != job.getVolume().getId()) {
												nthArtifactVolume.setStatus(ArtifactVolumeStatus.deleted);
												artifactVolumeDao.save(nthArtifactVolume);

												softDeleteTFileVolumeEntries(artifactId, nthArtifactVolume.getId().getVolumeId());		
												break;
											}
										}
									}else if(rewritePurpose == RewriteMode.replace || rewritePurpose == RewriteMode.migrate) { // if rewritten volume
										String volumeId = job.getRequest().getDetails().getVolumeId();
										ArtifactVolume artifactVolume = artifactVolumeDao.findByIdArtifactIdAndIdVolumeId(artifactId, volumeId);
										ArtifactVolumeStatus artifactVolumeStatus = ArtifactVolumeStatus.deleted;
										if(rewritePurpose == RewriteMode.migrate)
											artifactVolumeStatus = ArtifactVolumeStatus.migrated;
										artifactVolume.setStatus(artifactVolumeStatus);

										artifactVolumeDao.save(artifactVolume);
										if(artifactVolumeStatus == ArtifactVolumeStatus.deleted) // TODO : Figure out what should happen to migrate... NOTE : migrate and artifact rewrite piece not tested.
											softDeleteTFileVolumeEntries(artifactId, volumeId);
									}

									// also delete the goodcopy/source restored content too
									Job goodCopyVerifyJob = jobDao.findByRequestIdAndFlowelementId(job.getRequest().getId(), CoreFlowelement.core_rewrite_flow_good_copy_checksum_verify.getId());
									inputPath = processingJobManager.getInputPath(goodCopyVerifyJob);
									restoreTmpFolder = new File(inputPath);
									try {
										FileUtils.deleteDirectory(restoreTmpFolder);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}
						else {
							String inputPath = processingJobManager.getInputPath(job);
							if(inputPath != null) {
								if(requestedAction == Action.restore_process){
									// what need to be restored
									int fileIdRestored = requestDetails.getFileId();

									String restoredFilePathName = null;

									org.ishafoundation.dwaraapi.db.model.transactional.File file = fileDao.findById(fileIdRestored).get();
									if(file != null)
										restoredFilePathName = file.getPathname();

									// TODO - SuPeR HaCk - Need to bring this into f/w scheme of things properly
									String lastJobProcessingTask = job.getProcessingtaskId();
									if(lastJobProcessingTask != null && lastJobProcessingTask.equals("video-digi-2020-mkv-mov-gen") && restoredFilePathName.endsWith(".mkv")) {
										restoredFilePathName = restoredFilePathName.replace(".mkv", ".mov");
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
	}

	private void softDeleteTFileVolumeEntries(int artifactId, String volumeId) {
		Artifact nthArtifact = artifactDao.findById(artifactId).get();
		List<org.ishafoundation.dwaraapi.db.model.transactional.File> artifactFileList = null;
		try {
			artifactFileList = fileDao.findAllByArtifactIdAndDeletedFalse(nthArtifact.getId());
		} catch (Exception e) {
			logger.error("Unable to getArtifactFileList for " + nthArtifact.getId() + " : " + e.getMessage(), e);
		}
		List<TFile> artifactTFileList  = tFileDao.findAllByArtifactId(nthArtifact.getId()); 
		tFileVolumeDeleter.softDeleteTFileVolumeEntries(artifactFileList, artifactTFileList, nthArtifact, volumeId);
	}

	private void updateArtifactSizeAndCount(Job job, int artifactId) {
		org.ishafoundation.dwaraapi.db.model.transactional.Artifact artifact = artifactDao.findById(artifactId).get();

		Path artifactPath = Paths.get(artifact.getArtifactclass().getPath(), artifact.getName());
		File artifactFileObj = artifactPath.toFile();
		long artifactSize = 0;
		int artifactFileCount = 0;

		if(artifactFileObj.isDirectory()) {
			org.ishafoundation.dwaraapi.staged.scan.ArtifactFileDetails afd = stagedFileEvaluator.getDetails(artifactFileObj);
			artifactFileCount = afd.getCount();
			artifactSize = afd.getTotalSize();
		}
		else { // for single file artifacts...
			artifactFileCount = 1;
			artifactSize = artifact.getTotalSize();
		}
		logger.debug("artifactSize old " + artifact.getTotalSize() + " artifactSize new " + artifactSize);
		artifact.setTotalSize(artifactSize);
		artifact.setFileCount(artifactFileCount);
		artifact = (Artifact) artifactDao.save(artifact);

		org.ishafoundation.dwaraapi.db.model.transactional.File artifactFileFromDB = fileDao.findByPathname(artifact.getName());
		artifactFileFromDB.setSize(artifactSize);
		fileDao.save(artifactFileFromDB);

		TFile artifactTFileFromDB = tFileDao.findByPathname(artifact.getName());
		if(artifactTFileFromDB != null) {
			artifactTFileFromDB.setSize(artifactSize);
			tFileDao.save(artifactTFileFromDB);
		}

		// TODO : Digi hack - clean this up -Long term - iterate through files that are directories and calc their size and update them...
		if("file-delete".equals(job.getProcessingtaskId()) && artifact.getArtifactclass().getId().startsWith("video-digi-2020-")) {
			String subfolder = "mxf";
			Path artifactSubfolderPath = Paths.get(artifactPath.toString(), subfolder);
			File artifactSubfolderObj = artifactSubfolderPath.toFile();
			if(!artifactSubfolderObj.isDirectory()) {
				subfolder = "mov";
				artifactSubfolderPath = Paths.get(artifactPath.toString(), subfolder);
				artifactSubfolderObj = artifactSubfolderPath.toFile();
			}
			if(artifactSubfolderObj.isDirectory()) {
				long artifactSubfolderSize = FileUtils.sizeOfDirectory(artifactSubfolderObj);
				Path artifactSubfolderFilePath = Paths.get(artifact.getName(), subfolder);
				org.ishafoundation.dwaraapi.db.model.transactional.File artifactSubfolderFileFromDB = fileDao.findByPathname(artifactSubfolderFilePath.toString());
				artifactSubfolderFileFromDB.setSize(artifactSubfolderSize);
				fileDao.save(artifactSubfolderFileFromDB);

				TFile tfileFromDB = tFileDao.findByPathname(artifactSubfolderFilePath.toString());
				tfileFromDB.setSize(artifactSubfolderSize);
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

			Status status = StatusUtil.getStatus(jobStatusList);
			logger.trace("System request status - " + nthRequest.getId() + " ::: " + status);
			nthRequest.setStatus(status);

			if(status != Status.queued && status != Status.in_progress && status != Status.on_hold)
				nthRequest.setCompletedAt(LocalDateTime.now());

			if(nthRequest.getActionId() == Action.restore_tape_and_move_it_to_cp_proxy_server && status == Status.completed) {
				
				// Step 1 - rename the folder from old to new 
				String cpServerName = "CP-Proxy";
				String ingestServerName = "Ingest";

				
				RequestDetails rd = nthRequest.getDetails();
				String volumeId = rd.getVolumeId();
				int artifactId = rd.getArtifactId();
				Artifact artifact = artifactDao.findById(artifactId).get();
				
				VolumeArtifactServerNameKey volumeArtifactServerNameKeyForIngest = new VolumeArtifactServerNameKey(volumeId, artifact.getName(), ingestServerName);
				
				TArtifactnameJobMap tArtifactnameJobMapForIngest = tArtifactnameJobMapDao.findById(volumeArtifactServerNameKeyForIngest).get();
				int jobIdFromIngestServer = tArtifactnameJobMapForIngest.getJobId();

				VolumeArtifactServerNameKey volumeArtifactServerNameKeyForCpProxy = new VolumeArtifactServerNameKey(volumeId, artifact.getName(), cpServerName);
				
				TArtifactnameJobMap tArtifactnameJobMapForCpProxy = tArtifactnameJobMapDao.findById(volumeArtifactServerNameKeyForCpProxy).get();
				int jobIdFromCPServer = tArtifactnameJobMapForCpProxy.getJobId();
				
				Job ingestRestoreJob = jobDao.findById(jobIdFromIngestServer).get();
				
				String srcPath = restoreStorageTask.getRestoreLocation(ingestRestoreJob);
				String destPath = srcPath.replace(jobIdFromIngestServer+"", jobIdFromCPServer+"");
				
				try {
					boolean isSuccess = new File(srcPath).renameTo(new File(destPath));
					if(isSuccess) {
						logger.info(srcPath + " renamed successfully to " + destPath);
						// Step 2 - put the restore jobs as completed and trigger createJobDependents (ingest needs to call new createJobDependents variant on CP server, that will put the restore job on completed and call createJobDependents)
						isSuccess = callMarkJobCompletedAndCreateDependentJobsApiOnCpProxyServer(jobIdFromCPServer);
						if(isSuccess) {
							logger.info(jobIdFromCPServer + " job on CP proxy server successfully marked completed and dependent job created");
						}
						else {
							String msg = "Unable to mark " + jobIdFromCPServer + " completed and create its dependent job";
							logger.error(msg);
							// Fail the system request here 
							nthRequest.setStatus(Status.failed);
							nthRequest.setMessage(msg);							
						}
					}
					else {
						String msg = "Unable to rename " + srcPath + " to " + destPath;
						logger.error(msg);
						// Fail the system request here 
						nthRequest.setStatus(Status.failed);
						nthRequest.setMessage(msg);
					}
				}
				catch (Exception e) {
					String msg = "Unable to rename " + srcPath + " to " + destPath + " or mark "+ jobIdFromCPServer + " completed and create its dependent job";
					logger.error(msg, e);
					// Fail the system request here 
					nthRequest.setStatus(Status.failed);
					nthRequest.setMessage(msg);
				}
				
			}
			
			if(nthRequest.getActionId() == Action.generate_mezzanine_proxies && status == Status.completed) {
				// 1. Copy the files to SAN / Overseas
				// Get the path where the mezzanine proxy folder has been restrucured 
				String restructureMezFoldername = configuration.getRestructuredMezzanineFolderName();
				String resturcturedMezFolderPathToWrite = configuration.getRestructuredMezzanineForWritingFolderPath();
				int originalArtifactID = nthRequest.getDetails().getArtifactId();
				int systemRequestID = nthRequest.getId();
				Artifact originalArtifact = artifactDao.findById(originalArtifactID).get();
				List<Artifact> proxyArtifacts = artifactDao.findAllByArtifactRef(originalArtifact) ;
				String artifactClassForMezzProxy = "";
				String mezzArtifactName = "";
				for (Artifact artifact: proxyArtifacts) {
					String artifactClass = artifact.getArtifactclass().getId();
					if (artifactClass.contains("mezz")) {
						artifactClassForMezzProxy = artifactClass;		
						mezzArtifactName = ArtifactUtil.renameWithDate(artifact.getName());
					}
				}
				
				// Check if the artifact Class was gotten. In case its not then it means the mez proxy was not added to artifact table.
				if (mezzArtifactName.equals("")) {
					String msg = " Mez proxy not found in artifact table. Original artifact ID -> "+originalArtifactID+ " . System request ID -> "+systemRequestID;
					logger.error(msg);
					// Fail the system request here 
					nthRequest.setStatus(Status.failed);
					nthRequest.setMessage(msg);
				}
				else {				
					// Get the source path for the mezanine proxy folder
					String sourcePathForMezzanineFolder =  artifactClassDao.findById(artifactClassForMezzProxy).get().getPath() + File.separator + restructureMezFoldername + File.separator + mezzArtifactName;
					String mezzFolderToDelete = artifactClassDao.findById(artifactClassForMezzProxy).get().getPath() + File.separator + mezzArtifactName;
					String destinationPath = destinationDao.findById("san-mezz").get().getPath(); 
					// Now copy the file 
					try {
						FileUtils.copyDirectoryToDirectory(new File(sourcePathForMezzanineFolder), new File(destinationPath));
						// 1.5 Copy the file to somewhere it can be written later to a LTFS tape .
						try {
							java.nio.file.Files.move(Paths.get(sourcePathForMezzanineFolder), Paths.get(resturcturedMezFolderPathToWrite + File.separator + mezzArtifactName));
						}
						catch (IOException e) {
							String msg = " Mezzanine proxy Copy to write dump folder failed -> "+sourcePathForMezzanineFolder+ " . Destination -> " + resturcturedMezFolderPathToWrite + File.separator + mezzArtifactName + " System request ID -> "+systemRequestID;
							logger.error(msg, e);			 
						}
					} catch (IOException e) {
						String msg = " Mezzanine proxy Copy Failed to SAN -> "+sourcePathForMezzanineFolder+ " . Destination -> " + destinationPath + " System request ID -> "+systemRequestID;
						logger.error(msg, e);
						// Fail the system request here  Mezzanine proxy Copy Failed to SAN
						nthRequest.setStatus(Status.failed);
						nthRequest.setMessage(msg);						 
					}
					
					
					// 2. Delete the empty directory of mezanine proxy and then the original restored file,
					//(i) Delete the empty directory of mezanine proxy
					try {
						FileUtils.deleteDirectory(new File(mezzFolderToDelete));
					} catch (IOException e) {
						String msg = " Failed to delete the empty mezz folder. Bcoz "+e.getMessage()+"  . Mezz folderpath -> "+mezzFolderToDelete;
						logger.error(msg,e);
					}
						//(ii) Delete the original file
					Job restoreJob = jobDao.findByRequestIdAndStoragetaskActionId(systemRequestID,Action.restore);
					String originalFileRestorePath = restoreStorageTask.getRestoreLocation(restoreJob);
							File restoreTmpFolder = new File(originalFileRestorePath);
					try {
						FileUtils.deleteDirectory(restoreTmpFolder);
					} catch (IOException e) {
						String msg = " Failed to delete the original Restored folder. Bcoz "+e.getMessage()+"  . Original folderpath -> "+originalFileRestorePath;
						logger.error(msg,e);
					}
					

					//Later	Files.move(srcFile.toPath(), destFile.toPath(), StandardCopyOption.ATOMIC_MOVE);

				}

			}
			requestDao.save(nthRequest);

			if(nthRequest.getActionId() == Action.ingest && (status == Status.marked_completed || status == Status.completed)) {
				List<Artifact> artifactList = artifactDao.findAllByWriteRequestId(nthRequest.getId());
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
						// dont delete priv2 proxies from ingest server - "Refer email from Maa Jeevapushpa with subject P2 Proxies"
						if(!artifactclass.getId().contains("priv2")){
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

			if(nthRequest.getActionId() == Action.restore_process && (status == Status.failed || status == Status.completed_failures)) {
				String messagePart = nthRequest.getId() + " restore failed" ;
				SMSUtil.sendSMS(commaSeparatedMobileNos, messagePart);
			}
		}
	}

	private boolean callMarkJobCompletedAndCreateDependentJobsApiOnCpProxyServer(Integer jobId) throws Exception {
		String endpointUrlSuffix = "/job/" + jobId + "/markJobCompletedAndCreateDependentJobs";
	
		String postBody = "";
		boolean isSuccess = false;
		try {
			cpProxyServerInteracter.callCpProxyServer(endpointUrlSuffix, postBody);
			isSuccess = true;
		} catch (Exception e) {
			logger.error("Unable to call markJobCompleteAndCreateDependentJobs on CP Proxy server for " + jobId + "::" + e.getMessage(), e);
		}
		return isSuccess;
	}

	private void updateUserRequestStatus(List<Request> userRequestList) {
		for (Request nthUserRequest : userRequestList) {
			int userRequestId = nthUserRequest.getId();
			List<Request> systemRequestList = requestDao.findAllByRequestRefId(userRequestId);

			Status status = null;
			if(systemRequestList == null || systemRequestList.size() == 0) {
				continue;
			}
			else {
				List<Status> systemRequestStatusList = new ArrayList<Status>();
				for (Request nthSystemRequest : systemRequestList) {
					Status nthSystemRequestStatus = nthSystemRequest.getStatus();
					systemRequestStatusList.add(nthSystemRequestStatus);
				}	

				status = StatusUtil.getStatus(systemRequestStatusList);

				logger.trace("User request status - " + nthUserRequest.getId() + " ::: " + status);
			}
			// For completed restore jobs the .restoring folder is cleaned up...
			if((nthUserRequest.getActionId() == Action.restore || nthUserRequest.getActionId() == Action.restore_process) && status == Status.completed) {
				JsonNode jsonNode = nthUserRequest.getDetails().getBody();
				String outputFolder = jsonNode.get("outputFolder").asText();
				String destinationPath = jsonNode.get("destinationPath").asText();
				File restoreTmpFolder = FileUtils.getFile(destinationPath, configuration.getRestoreInProgressFileIdentifier(), outputFolder);
				try {
					FileUtils.deleteDirectory(restoreTmpFolder);
					logger.trace(restoreTmpFolder.getPath() + " deleted succesfully");
				} catch (IOException e) {
					logger.error("Unable to delete " + restoreTmpFolder.getPath());
				}

				String vpTicketNo = jsonNode.get("vpJiraTicket").asText();

				JiraUtil.updateJiraWorkflow(vpTicketNo,JiraTransition.footage_request_closed, null);
			}
			nthUserRequest.setStatus(status); 

			if(status != Status.queued && status != Status.in_progress && status != Status.on_hold)
				nthUserRequest.setCompletedAt(LocalDateTime.now());

			requestDao.save(nthUserRequest);
		}
	}



}