package org.ishafoundation.dwaraapi.process.thread;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TTFileJobDao;
import org.ishafoundation.dwaraapi.db.keys.TTFileJobKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Destination;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Filetype;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Tag;
import org.ishafoundation.dwaraapi.db.model.master.jointables.Flowelement;
import org.ishafoundation.dwaraapi.db.model.master.jointables.json.Taskconfig;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TTFileJob;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.FlowelementUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.helpers.ThreadNameHelper;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.ishafoundation.dwaraapi.storage.storagetask.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ProcessingJobManager extends ProcessingJobHelper implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ProcessingJobManager.class);
	
	@Autowired
	private Map<String, IProcessingTask> processingtaskActionMap;
	
	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private TTFileJobDao tFileJobDao;
		
	@Autowired
	private ArtifactDao artifactDao;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@Autowired
	private FlowelementUtil flowelementUtil;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private Restore restoreStorageTask;

	@Autowired
	private JobEntityToJobForProcessConverter jobEntityToJobForProcessConverter;
	
	@Autowired
	private FileEntityToFileForProcessConverter fileEntityToFileForProcessConverter;
	
	@Value("${wowo.useNewJobManagementLogic:true}")
	private boolean useNewJobManagementLogic;

	private Job job;

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public boolean isJobToBeCreated(String processingtaskId, String inputArtifactPath, String pathnameRegex) {
		
		Collection<LogicalFile> selectedFileList = getFilesToBeProcessed(processingtaskId, inputArtifactPath, pathnameRegex);
		int filesToBeProcessedCount = selectedFileList.size();
		logger.trace("filesToBeProcessedCount " + filesToBeProcessedCount);
		
		if(filesToBeProcessedCount == 0) {
			String msg = "No files to process.";
			
			Processingtask processingtask = getProcessingtask(processingtaskId);
			if(processingtask != null && !processingtask.getFiletypeId().equals("_all_")) {
				msg = msg + " Check supported extensions...";
			}
			logger.warn(msg);
			return false;
		}
		return true;
	}
	
	private Collection<LogicalFile> getFilesToBeProcessed(String processingtaskId, String inputArtifactPath, String pathnameRegex){
		Processingtask processingtask = getProcessingtask(processingtaskId);
		
		Filetype ft = getInputFiletype(processingtask);
			
		return  getLogicalFileList(ft, inputArtifactPath, pathnameRegex);
				
	}

	public String getInputPath(Job job) {
		String inputPath = null;
		List<Integer> jobDependencyList = job.getDependencies(); // if current processing job has a dependency, and that too a restore job then inputpath = config tmp location + dependencyRestorejobid
		if(jobDependencyList != null) {
			for (Integer nthDependentJobId : jobDependencyList) {
				Job nthDependentJob = jobDao.findById(nthDependentJobId).get();
				if(nthDependentJob.getStoragetaskActionId() == Action.restore) {
					inputPath = restoreStorageTask.getRestoreLocation(nthDependentJob);
					break;
				}
			}
		}
		return inputPath;
	}
	
	@Override
    public void run() {
		logger.debug("Managing processing job - " + job.getId());
		
		// This check is because of the same file getting queued up for processing again...
		// JobManager --> get all "Queued" processingjobs --> ProcessingJobManager ==== thread per file ====> ProcessingJobProcessor --> Only when the file's turn comes the status change to inprogress
		// Next iteration --> get all "Queued" processingjobs would still show the same job above sent already to ProcessingJobManager as it has to wait for its turn for CPU cycle... 
		Status jobStatus = jobDao.findById(job.getId()).get().getStatus(); // getting the job again to reflect the current status...
		
		if(jobStatus == Status.on_hold || jobStatus == Status.cancelled) {
			logger.warn("Processing job - " + job.getId() + " " + jobStatus + " - so dont process it now");
			return;
		}
		
		if(jobStatus != Status.queued) {
			logger.trace("Processing job - " + job.getId() + " already picked up earlier. So skipping from processing again");
			return;
		}
		
		
		ThreadNameHelper threadNameHelper = new ThreadNameHelper();
		try {
			threadNameHelper.setThreadName(job.getRequest().getId(), job.getId());
		
			String processingtaskId = job.getProcessingtaskId();

			IProcessingTask processingtaskImpl = processingtaskActionMap.get(processingtaskId);
			if(processingtaskImpl == null)
				throw new Exception(processingtaskId + " class is still not impl. Please refer IProcessingTask doc...");
			
			
			String executorName = processingtaskId.toLowerCase();
			Executor executor = IProcessingTask.taskName_executor_map.get(executorName);
			if(executor == null) {
				executorName = IProcessingTask.GLOBAL_THREADPOOL_IDENTIFIER;
				executor = IProcessingTask.taskName_executor_map.get(executorName);
			}
			// TODO Any check needed on the configured executor? 
			// Checking if more than needed jobs are already queued to avoid too many files queued up on respective executors
			ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
			Set<Integer> jobsOnQueueSet = new TreeSet<Integer>();
			BlockingQueue<Runnable> runnableQueueList = tpe.getQueue();
			for (Runnable runnable : runnableQueueList) {
				ProcessingJobProcessor pjp = (ProcessingJobProcessor) runnable;
				jobsOnQueueSet.add(pjp.getJob().getId());
			}
			logger.trace("---" + executorName + ":" + tpe.getCorePoolSize() + ":" + jobsOnQueueSet.size() + ":" + jobsOnQueueSet);
			if(useNewJobManagementLogic && jobsOnQueueSet.size() >= (tpe.getCorePoolSize() + 2)) {
				logger.debug("Already enough jobs(" + jobsOnQueueSet.size() + ")'s files are in " + executorName + " processing queue");
				return;
			} else
				logger.info("Taking up processing job " + job.getId() + " for preprocessing and delegating it to PJP thread executor");
			
			Processingtask processingtask = getProcessingtask(processingtaskId);
//			if(processingtask == null)
//				throw new Exception(processingtask + " is not configured in DB. Please configure ProcessingTask table properly");
			
			Integer inputArtifactId = job.getInputArtifactId();
			Artifact inputArtifact = artifactDao.findById(inputArtifactId).get();
			
			String inputArtifactName = inputArtifact.getName();
			Artifactclass inputArtifactclass = inputArtifact.getArtifactclass();
			String inputArtifactclassId = inputArtifactclass.getId();
			String inputPath = getInputPath(job); 
			if(inputPath == null)
				inputPath = inputArtifactclass.getPath();
			String inputArtifactPathname =  inputPath + File.separator + inputArtifactName;
			
	
			String outputArtifactName = null;
			String outputArtifactPathname = null; // holds where to generate the files in the physical system...
    		Artifactclass outputArtifactclass = null;
			String outputArtifactclassSuffix = processingtask != null ? processingtask.getOutputArtifactclassSuffix() : null;
			logger.trace("outputArtifactclassSuffix " + outputArtifactclassSuffix);
			if(outputArtifactclassSuffix != null) { // For processing tasks like checksum-gen this will be null...
				if(outputArtifactclassSuffix.equals("")) {
					outputArtifactclass = inputArtifactclass;
					outputArtifactName = inputArtifactName;
					outputArtifactPathname = inputArtifactPathname; // holds where to generate the files in the physical system...
				}
				else {
					String outputArtifactclassId =  inputArtifactclassId + outputArtifactclassSuffix;
					logger.trace("outputArtifactclassId " + outputArtifactclassId);
					outputArtifactclass = configurationTablesUtil.getArtifactclass(outputArtifactclassId);
					
					if(outputArtifactclass == null) {
						throw new DwaraException(outputArtifactclassId + " not configured in artifactclass table. Please double check");
					}
					else {
						outputArtifactName = getOutputArtifactName(inputArtifactName, inputArtifactclass, outputArtifactclass);
						outputArtifactPathname = getOutputArtifactPathname(outputArtifactclass, outputArtifactName);
					}
				}
			}
			
			logger.trace("inputArtifactPath " + inputArtifactPathname); 	// /data/ingested/V22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12 
																		// /data/ingested/V22204_Test2_MBT20481_01.MP4 OR 
																		// /data/transcoded/public/VL22204_Test2_MBT20481_01
			logger.trace("outputArtifactName " + outputArtifactName); 	// null for processes that has no outputartifactclass OR 
																		// VL22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12
			logger.trace("outputArtifactPathname " + outputArtifactPathname); 	// null OR 
																				// /data/transcoded/public/VL22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12
			
			HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.File> filePathToFileObj = getFilePathToFileObj(inputArtifact);
			
			HashMap<String, TFile> filePathToTFileObj = getFilePathToTFileObj(inputArtifactId);
			
			Flowelement flowelement = flowelementUtil.findById(job.getFlowelementId());
			Taskconfig taskconfig = flowelement.getTaskconfig();
			String pathnameRegex = taskconfig != null ? taskconfig.getPathnameRegex() : null;
			
			Collection<LogicalFile> selectedFileList = getFilesToBeProcessed(processingtaskId, inputArtifactPathname, pathnameRegex);
			int filesToBeProcessedCount = selectedFileList.size();
			logger.trace("filesToBeProcessedCount " + filesToBeProcessedCount);
			
			boolean anyFileSentForProcessing = false;
			if(filesToBeProcessedCount > 0) {
				org.ishafoundation.dwaraapi.process.request.Job jobForProcess = jobEntityToJobForProcessConverter.getJobForProcess(job);
				org.ishafoundation.dwaraapi.process.request.Artifact inputArtifactForProcess = jobForProcess.getInputArtifact();
				inputArtifactForProcess.setName(inputArtifactName);
				org.ishafoundation.dwaraapi.process.request.Artifactclass inputArtifactclassForProcess = inputArtifactForProcess.getArtifactclass();
				inputArtifactclassForProcess.setId(inputArtifactclassId);
				inputArtifactclassForProcess.setSource(inputArtifact.getArtifactclass().getSource());
				inputArtifactclassForProcess.setPathPrefix(inputArtifact.getArtifactclass().getPathPrefix());
				inputArtifactclassForProcess.setCategory(inputArtifact.getArtifactclass().getCategory());
				
				org.ishafoundation.dwaraapi.process.request.Artifact outputArtifactForProcess = jobForProcess.getOutputArtifact();
				if(outputArtifactForProcess != null) {
					outputArtifactForProcess.setName(outputArtifactName);

					org.ishafoundation.dwaraapi.process.request.Artifactclass outputArtifactclassForProcess = outputArtifactForProcess.getArtifactclass();
					if(outputArtifactclass != null) {
						outputArtifactclassForProcess.setId(outputArtifactclass.getId());
						outputArtifactclassForProcess.setSource(outputArtifactclass.getSource());
						outputArtifactclassForProcess.setPathPrefix(outputArtifactclass.getPathPrefix());
						outputArtifactclassForProcess.setCategory(outputArtifactclass.getCategory());
					}
				}
				String outputPathSuffix = null;

				
					
				String configuredOutputPath = taskconfig != null ? taskconfig.getOutputPath() : null;
				String configuredDestinationId = taskconfig != null ? taskconfig.getDestinationId()  : null;
				String configuredDestinationPath = null;
				if(configuredDestinationId != null) {
					Destination destination = configurationTablesUtil.getDestination(configuredDestinationId);
					if(destination == null)
						throw new DwaraException("Destination " + configuredDestinationId + " is not configured in DB");
					configuredDestinationPath = destination.getPath();
				}
				if(configuredOutputPath != null) {
					String normalizedOutputPath = FilenameUtils.normalizeNoEndSeparator(configuredOutputPath);
					if(normalizedOutputPath == null)
						throw new DwaraException("ArtifactclassTask.config.output_path value " + configuredOutputPath + " is not supported");
					else 
						outputPathSuffix = normalizedOutputPath;
				}

				for (Iterator<LogicalFile> iterator = selectedFileList.iterator(); iterator.hasNext();) {
					LogicalFile logicalFile = (LogicalFile) iterator.next(); // would have an absolute file like C:\data\ingested\14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A\1 CD\00018.MTS and its sidecar files
					String logicalFilePath = logicalFile.getAbsolutePath();
					logger.trace("logicalFilePath - " + logicalFilePath);
					if(logicalFilePath.contains(configuration.getJunkFilesStagedDirName())) { // skipping junk files
						logger.trace("Junk file. Skipping it");
						continue;			
					}
					anyFileSentForProcessing = true;
					String artifactNamePrefixedFilePathname = null; // 14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A.MP4 || 14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A\1 CD\00018.MTS
					String outputFilePath = null; // /data/transcoded/public
					if(StringUtils.isNotBlank(FilenameUtils.getExtension(inputArtifactPathname))) { // means input artifact is a file and not a directory
						artifactNamePrefixedFilePathname = inputArtifactName; // would hold 14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A.MP4
						if(outputArtifactPathname != null)
							outputFilePath = outputArtifactPathname;
					}
					else {
						String filePathnameWithoutArtifactNamePrefixed = logicalFilePath.replace(inputArtifactPathname + File.separator, ""); // would hold 1 CD\00018.MTS or just 00019.MTS
						artifactNamePrefixedFilePathname = logicalFilePath.replace(inputArtifactPathname + File.separator, inputArtifactName + File.separator); // would hold 14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A\1 CD\00018.MTS
						
						if(outputArtifactPathname != null) {
							if(outputPathSuffix != null) {
								outputFilePath = FilenameUtils.normalizeNoEndSeparator(outputArtifactPathname + File.separator + outputPathSuffix);
							}else {
								String suffixPath = FilenameUtils.getFullPathNoEndSeparator(filePathnameWithoutArtifactNamePrefixed);
								if(StringUtils.isBlank(suffixPath))
									outputFilePath = outputArtifactPathname;
								else
									outputFilePath = outputArtifactPathname + File.separator + FilenameUtils.getFullPathNoEndSeparator(filePathnameWithoutArtifactNamePrefixed);
							}
						}
						else if(configuredDestinationPath != null) {
							outputFilePath = configuredDestinationPath + java.io.File.separator + FilenameUtils.getFullPathNoEndSeparator(artifactNamePrefixedFilePathname);
						}
					}
					logger.trace("outputFilePath - " + outputFilePath);
					
					//logger.info("Now processing - " + path);
					TFile tFile = null;
			
					if(job.getRequest().getActionId() == Action.ingest) {
						if(filePathToTFileObj.containsKey(artifactNamePrefixedFilePathname))
							tFile = filePathToTFileObj.get(artifactNamePrefixedFilePathname);
						
						if(tFile == null) {
							logger.warn(artifactNamePrefixedFilePathname + " not in t_file table. Skipping it");
							continue;
						}
					}
					
					org.ishafoundation.dwaraapi.db.model.transactional.File file = null;
					if(filePathToFileObj.containsKey(artifactNamePrefixedFilePathname))
						file = filePathToFileObj.get(artifactNamePrefixedFilePathname);

					if(tFile == null && file == null)
						throw new Exception("File/TFile record missing in DB for " + artifactNamePrefixedFilePathname); // Edited/Backup scenario will bomb if files that are not part of File table are to be verified outside ingest - say during rewrite or restore_verify - as tFile records are purged after finalisation
					
					int fileId = tFile != null ? tFile.getId() : file.getId();
					logger.debug("Delegating for processing -job-" + job.getId() + "-file-" + fileId);
	
					// Requeue scenario - Only failed files are to be continued...
					Optional<TTFileJob> tFileJobDB = tFileJobDao.findById(new TTFileJobKey(fileId, job.getId()));
					if(tFileJobDB.isPresent() && (tFileJobDB.get().getStatus() == Status.in_progress || tFileJobDB.get().getStatus() == Status.completed)) {
						logger.info("job-" + job.getId() + "-file-" + fileId + " already Inprogress/completed. Skipping it...");
						continue;
					}
					
					// This check is because of the same file getting queued up for processing again...
					// JobManager --> get all "Queued" processingjobs --> ProcessingJobManager ==== thread per file ====> ProcessingJobProcessor --> Only when the file's turn comes the status change to inprogress
					// Next iteration --> get all "Queued" processingjobs would still show the same job above sent already to ProcessingJobManager as it has to wait for its turn for CPU cycle... 
					boolean alreadyQueued = false;
					for (Runnable runnable : runnableQueueList) {
						ProcessingJobProcessor pjp = (ProcessingJobProcessor) runnable;
						if(job.getId() == pjp.getJob().getId() && fileId == (tFile != null ? pjp.getTFile().getId() : pjp.getFile().getId())) {
							logger.debug("job-" + job.getId() + "-file-" + fileId + " already in ProcessingJobProcessor queue. Skipping it...");
							alreadyQueued = true;
							break;
						}
					}
					
					if(!alreadyQueued) { // only when the job is not already dispatched to the queue to be executed, send it now...
						if(!tFileJobDB.isPresent()) {
							TTFileJob tFileJob = new TTFileJob();
							tFileJob.setId(new TTFileJobKey(fileId, job.getId()));
							tFileJob.setJob(job);
							tFileJob.setArtifactId(inputArtifactId);
							tFileJob.setStatus(Status.queued);
							logger.debug("DB TFileJob Creation for file " + fileId);
							tFileJobDao.save(tFileJob);
							logger.debug("DB TFileJob Creation - Success");
						}
					
	
						ProcessingJobProcessor processingJobProcessor = applicationContext.getBean(ProcessingJobProcessor.class);
						ProcessContext processContext = new ProcessContext();
						processContext.setInputDirPath(inputArtifactPathname);
						processContext.setJob(jobForProcess);
						processContext.setFile(fileEntityToFileForProcessConverter.getFileForProcess(file));
						processContext.setTFile(fileEntityToFileForProcessConverter.getTFileForProcess(tFile));
						processContext.setLogicalFile(logicalFile);
						processContext.setOutputDestinationDirPath(outputFilePath);
						Set<Tag> tagSet = inputArtifact.getTags();
						
						if(tagSet != null) {
							List<String> tags = new ArrayList<String>();
							for (Tag nthTag : tagSet) {
								tags.add(nthTag.getTag());
							}
							processContext.setTags(tags);
						}

						/* Wont be effective once the ProcessingJobProcessor object is sent to the executor queue
						BasicThreadFactory threadFactory =  (BasicThreadFactory) tpe.getThreadFactory();
						processContext.setPriority(threadFactory.getPriority());
						*/
						processingJobProcessor.setProcessContext(processContext);
						
						processingJobProcessor.setJob(job);
						processingJobProcessor.setInputArtifact(inputArtifact);
						processingJobProcessor.setFile(file);
						processingJobProcessor.setTFile(tFile);
						processingJobProcessor.setOutputArtifactclass(outputArtifactclass);
						logger.info("Now kicking off - " + job.getId() + " " + logicalFilePath + " task " + processingtaskId);
						executor.execute(processingJobProcessor);
					}
				}
			}
			
			if(filesToBeProcessedCount == 0 || !anyFileSentForProcessing) {
				String msg = "No files to process.";
				if(processingtask != null && !processingtask.getFiletypeId().equals("_all_")) {
					msg = msg + " Check supported extensions...";
				}
				throw new Exception(msg);
//				logger.info(msg);
//				String logMsgPrefix = "DB Job - " + "(" + job.getId() + ") - Updation - status to " + Status.completed;
//				logger.debug(logMsgPrefix);	
//				job.setStatus(Status.completed);
//				job.setMessage("[info] " + msg);
//				jobDao.save(job);
			}
		}catch (Exception e) {
			logger.error("Unable to proceed on job - " + job.getId(), e);
			String logMsgPrefix = "DB Job - " + "(" + job.getId() + ") - Updation - status to " + Status.failed;
			logger.debug(logMsgPrefix);	
			job.setCompletedAt(LocalDateTime.now());
			job.setStatus(Status.failed);
			job.setMessage("[error] " + e.getMessage());
			jobDao.save(job);
		}finally {
			threadNameHelper.resetThreadName();
		}
	}

	private String getOutputArtifactName(String inputArtifactName, Artifactclass inputArtifactclass, Artifactclass outputArtifactclass){
		String inputArtifactClassSequenceId = inputArtifactclass.getSequenceId();
		Sequence inputArtifactClassSequence = configurationTablesUtil.getSequence(inputArtifactClassSequenceId);
		String inputArtifactPrefix = inputArtifactClassSequence.getPrefix();
		
		String outputArtifactClassSequenceId = outputArtifactclass.getSequenceId();
		Sequence outputArtifactClassSequence = configurationTablesUtil.getSequence(outputArtifactClassSequenceId);
		String outputArtifactPrefix = outputArtifactClassSequence.getPrefix();
				
		String inputArtifactSeqCode = StringUtils.substringBefore(inputArtifactName,"_");
		
		String outputArtifactSeqCode = inputArtifactSeqCode.replace(inputArtifactPrefix, outputArtifactPrefix);
		
		String outputArtifactName = inputArtifactName.replace(inputArtifactSeqCode, outputArtifactSeqCode);
		if(StringUtils.isNotBlank(FilenameUtils.getExtension(outputArtifactName))) // is a file - remove the extn
			outputArtifactName =  FilenameUtils.getBaseName(outputArtifactName);
		return outputArtifactName;
	}

	private String getOutputArtifactPathname(Artifactclass outputArtifactclass, String outputArtifactName) {
		return outputArtifactclass.getPath() + java.io.File.separator + outputArtifactName;
	}

}
