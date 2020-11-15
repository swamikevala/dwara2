package org.ishafoundation.dwaraapi.process.thread;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.FiletypeDao;
import org.ishafoundation.dwaraapi.db.dao.master.ProcessingtaskDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ArtifactclassProcessingtaskDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepositoryUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TFileJobDao;
import org.ishafoundation.dwaraapi.db.keys.ArtifactclassProcessingtaskKey;
import org.ishafoundation.dwaraapi.db.keys.TFileJobKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Filetype;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassProcessingtask;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ExtensionFiletype;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileJob;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.db.utils.SequenceUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.helpers.ThreadNameHelper;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.helpers.FiletypePathnameReqexVisitor;
import org.ishafoundation.dwaraapi.process.helpers.LogicalFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ProcessingJobManager implements Runnable{

	private static final Logger logger = LoggerFactory.getLogger(ProcessingJobManager.class);
	
	@Autowired
	private Map<String, IProcessingTask> processingtaskActionMap;
	
	@Autowired
    private ProcessingtaskDao processingtaskDao;
	
	@Autowired
	private FiletypeDao filetypeDao;
	
	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private ArtifactclassProcessingtaskDao artifactclassProcessingtaskDao;
	
	@Autowired
	private TFileJobDao tFileJobDao;
		
	@Autowired
	private DomainUtil domainUtil;

	@Autowired
	private	LogicalFileHelper fileHelper;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@Autowired
	private SequenceUtil sequenceUtil;
	
	@Autowired
	private Configuration configuration;

	@Autowired
	private FileRepositoryUtil fileRepositoryUtil;
	
	private Job job;

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public boolean isJobToBeCreated(String processingtaskId, String inputArtifactPath, Artifactclass inputArtifactclass) {
		Processingtask processingtask = processingtaskDao.findById(processingtaskId).get();
		Collection<LogicalFile> selectedFileList = getFilesToBeProcessed(processingtaskId, inputArtifactPath, inputArtifactclass);
		int filesToBeProcessedCount = selectedFileList.size();
		logger.trace("filesToBeProcessedCount " + filesToBeProcessedCount);
		
		if(filesToBeProcessedCount == 0) {
			String msg = "No files to process.";
			if(!processingtask.getFiletypeId().equals("_all_")) {
				msg = msg + " Check supported extensions...";
			}
			logger.warn(msg);
			return false;
		}
		return true;
	}
	
	private Collection<LogicalFile> getFilesToBeProcessed(String processingtaskId, String inputArtifactPath, Artifactclass inputArtifactclass){
		
		Processingtask processingtask = processingtaskDao.findById(processingtaskId).get();
		
		// TODO Cache filetypes...
		Filetype ft = null;
		if(!processingtask.getFiletypeId().equals("_all_")) { // if filetype is all means get all files...
			ft = filetypeDao.findById(processingtask.getFiletypeId()).get();
		}
			
		return  getLogicalFileList(ft, inputArtifactPath, inputArtifactclass.getId(), processingtaskId);
	}
	
	@Override
    public void run() {
		logger.info("Managing processing job - " + job.getId());
		
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
			
			Executor executor = IProcessingTask.taskName_executor_map.get(processingtaskId.toLowerCase());
			// TODO Any check needed on the configured executor? 
			
			Processingtask processingtask = processingtaskDao.findById(processingtaskId).get();
			
			Domain domain = null;
			Artifactclass outputArtifactclass = null;
			
			String outputArtifactclassSuffix = processingtask.getOutputArtifactclassSuffix();
			String artifactclassId = job.getRequest().getDetails().getArtifactclassId();
			logger.trace("outputArtifactclassSuffix " + outputArtifactclassSuffix);
			if(outputArtifactclassSuffix != null) { // For processing tasks like checksum-gen this will be null...
				String outputArtifactclassId =  artifactclassId + outputArtifactclassSuffix;
				logger.trace("outputArtifactclassId " + outputArtifactclassId);
				outputArtifactclass = configurationTablesUtil.getArtifactclass(outputArtifactclassId);
				domain = outputArtifactclass.getDomain();
			}
			else{
				Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(artifactclassId);
				domain = artifactclass.getDomain();
			}
			
			if(domain == null) {
				logger.error("Unable to get domain from the request");
				throw new Exception("Unable to get domain from the request");
			}
			ArtifactRepository artifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);
			
			Integer inputArtifactId = job.getInputArtifactId();
			Artifact inputArtifact = (Artifact) artifactRepository.findById(inputArtifactId).get();
			
			String inputArtifactName = inputArtifact.getName();
			Artifactclass inputArtifactclass = inputArtifact.getArtifactclass();
			String inputArtifactPath = inputArtifactclass.getPath() + File.separator + inputArtifactName;
	
			// For the task getting processed check
			// 1) if there are any dependent tasks 
			// 2) and is responsible for generating a artifactclass
			
			// then it means the output of the current task is the input for the dependent task
			
			
			String outputArtifactName = null;
			String outputArtifactPathname = null; // holds where to generate the files in the physical system...
			if(outputArtifactclass != null) {
				outputArtifactName = getOutputArtifactName(outputArtifactclass, inputArtifactName);
				outputArtifactPathname = getOutputArtifactPathname(outputArtifactclass, outputArtifactName);
			}
			
			logger.trace("inputArtifactPath " + inputArtifactPath); 	// /data/ingested/V22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12 
																		// /data/ingested/V22204_Test2_MBT20481_01.MP4 OR 
																		// /data/transcoded/public/VL22204_Test2_MBT20481_01
			logger.trace("outputArtifactName " + outputArtifactName); 	// null for processes that has no outputartifactclass OR 
																		// VL22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12
			logger.trace("outputArtifactPathname " + outputArtifactPathname); 	// null OR 
																				// /data/transcoded/public/VL22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12
			
			HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.domain.File> filePathToFileObj = getFilePathToFileObj(domain, inputArtifact);
	
			Collection<LogicalFile> selectedFileList = getFilesToBeProcessed(processingtaskId, inputArtifactPath, inputArtifactclass);
			int filesToBeProcessedCount = selectedFileList.size();
			logger.trace("filesToBeProcessedCount " + filesToBeProcessedCount);
			
			boolean anyFileSentForProcessing = false;
			if(filesToBeProcessedCount > 0) {
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
					if(StringUtils.isNotBlank(FilenameUtils.getExtension(inputArtifactPath))) { // means input artifact is a file and not a directory
						artifactNamePrefixedFilePathname = inputArtifactName; // would hold 14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A.MP4
						if(outputArtifactPathname != null)
							outputFilePath = outputArtifactPathname;
					}
					else {
						String filePathnameWithoutArtifactNamePrefixed = logicalFilePath.replace(inputArtifactPath + File.separator, ""); // would hold 1 CD\00018.MTS or just 00019.MTS
						artifactNamePrefixedFilePathname = logicalFilePath.replace(inputArtifactPath + File.separator, inputArtifactName + File.separator); // would hold 14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A\1 CD\00018.MTS
						
						if(outputArtifactPathname != null) {
							String suffixPath = FilenameUtils.getFullPathNoEndSeparator(filePathnameWithoutArtifactNamePrefixed);
							if(StringUtils.isBlank(suffixPath))
								outputFilePath = outputArtifactPathname;
							else
								outputFilePath = outputArtifactPathname + File.separator + FilenameUtils.getFullPathNoEndSeparator(filePathnameWithoutArtifactNamePrefixed);
						}
					}
					logger.trace("outputFilePath - " + outputFilePath);
					//logger.info("Now processing - " + path);
					org.ishafoundation.dwaraapi.db.model.transactional.domain.File file = null;
					if(filePathToFileObj.containsKey(artifactNamePrefixedFilePathname))
						file = filePathToFileObj.get(artifactNamePrefixedFilePathname);
					logger.debug("Delegating for processing -job-" + job.getId() + "-file-" + file.getId());
	
					// Requeue scenario - Only failed files are to be continued...
					Optional<TFileJob> tFileJobDB = tFileJobDao.findById(new TFileJobKey(file.getId(), job.getId()));
					if(tFileJobDB.isPresent() && (tFileJobDB.get().getStatus() == Status.in_progress || tFileJobDB.get().getStatus() == Status.completed)) {
						logger.info("job-" + job.getId() + "-file-" + file.getId() + " already Inprogress/completed. Skipping it...");
						continue;
					}
					
					// This check is because of the same file getting queued up for processing again...
					// JobManager --> get all "Queued" processingjobs --> ProcessingJobManager ==== thread per file ====> ProcessingJobProcessor --> Only when the file's turn comes the status change to inprogress
					// Next iteration --> get all "Queued" processingjobs would still show the same job above sent already to ProcessingJobManager as it has to wait for its turn for CPU cycle... 
					ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
					BlockingQueue<Runnable> runnableQueueList = tpe.getQueue();
					boolean alreadyQueued = false;
					for (Runnable runnable : runnableQueueList) {
						ProcessingJobProcessor pjp = (ProcessingJobProcessor) runnable;
						if(job.getId() == pjp.getJob().getId() && file.getId() == pjp.getFile().getId()) {
							logger.info("job-" + job.getId() + "-file-" + file.getId() + " already in ProcessingJobProcessor queue. Skipping it...");
							alreadyQueued = true;
							break;
						}
					}
					
					if(!alreadyQueued) { // only when the job is not already dispatched to the queue to be executed, send it now...
						if(!tFileJobDB.isPresent()) {
							TFileJob tFileJob = new TFileJob();
							tFileJob.setId(new TFileJobKey(file.getId(), job.getId()));
							tFileJob.setJob(job);
							tFileJob.setArtifactId(inputArtifactId);
							tFileJob.setStatus(Status.queued);
							logger.debug("DB TFileJob Creation for file " + file.getId());
							tFileJobDao.save(tFileJob);
							logger.debug("DB TFileJob Creation - Success");
						}
						
						ProcessingJobProcessor processingJobProcessor = applicationContext.getBean(ProcessingJobProcessor.class);
						processingJobProcessor.setJob(job);
						processingJobProcessor.setDomain(domain);
						processingJobProcessor.setInputArtifact(inputArtifact);
						processingJobProcessor.setFile(file);
						processingJobProcessor.setLogicalFile(logicalFile);
						
						processingJobProcessor.setOutputArtifactclass(outputArtifactclass);
						processingJobProcessor.setOutputArtifactName(outputArtifactName); // VL20190701_071239
						processingJobProcessor.setOutputArtifactPathname(outputArtifactPathname); // C:\data\transcoded\public\VL20190701_071239
						processingJobProcessor.setDestinationDirPath(outputFilePath);
						logger.info("Now kicking off - " + job.getId() + " " + logicalFilePath + " task " + processingtaskId);
						executor.execute(processingJobProcessor);
					}
				}
			}
			
			if(filesToBeProcessedCount == 0 || !anyFileSentForProcessing) {
				String msg = "No files to process.";
				if(!processingtask.getFiletypeId().equals("_all_")) {
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

	private String getOutputArtifactName(Artifactclass outputArtifactclass, String inputArtifactName){
		String outputArtifactClassSequenceId = outputArtifactclass.getSequenceId();
		Sequence outputArtifactClassSequence = configurationTablesUtil.getSequence(outputArtifactClassSequenceId);
		String inputArtifactSeqCode = sequenceUtil.getExtractedCode(outputArtifactClassSequence, inputArtifactName);
		String outputArtifactSeqCode = sequenceUtil.getSequenceCode(outputArtifactClassSequence, inputArtifactName);
		String outputArtifactName = inputArtifactName.replace(inputArtifactSeqCode, outputArtifactSeqCode);
		if(StringUtils.isNotBlank(FilenameUtils.getExtension(outputArtifactName))) // is a file - remove the extn
			outputArtifactName =  FilenameUtils.getBaseName(outputArtifactName);
		return outputArtifactName;
	}

	private String getOutputArtifactPathname(Artifactclass outputArtifactclass, String outputArtifactName) {
		return outputArtifactclass.getPath() + java.io.File.separator + outputArtifactName;
	}
	
	private HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.domain.File> getFilePathToFileObj(Domain domain, Artifact artifactDbObj) throws Exception{
		HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.domain.File> filePathTofileObj = new HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.domain.File>();
		
		List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList = fileRepositoryUtil.getArtifactFileList(artifactDbObj, domain);
		for (org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile : artifactFileList) {
			filePathTofileObj.put(nthFile.getPathname(), nthFile);
		}
		//logger.trace("file collection - " + filePathTofileObj.keySet().toString());
		return filePathTofileObj;
	}
	
	private Collection<LogicalFile> getLogicalFileList(Filetype filetype, String inputArtifactPath, String inputArtifactclassId, String processingtaskId){
		
		Collection<LogicalFile> logicalFileCollection =  new ArrayList<LogicalFile>(); 
		
		List<String> extensions = null;
		String[] extensionsArray = null;
		List<String> sidecarExtensions = null;
		String[] sidecarExtensionsArray = null;
		boolean includeSidecarFiles = false;
		if(filetype != null) { // if filetype is null, extensions are set to null - which will get all the files listed - eg., process like checksum-gen
			Set<String> pathsToBeUsed = new TreeSet<String>(); 
			Optional<ArtifactclassProcessingtask> artifactclassProcessingtask = artifactclassProcessingtaskDao.findById(new ArtifactclassProcessingtaskKey(inputArtifactclassId, processingtaskId));
			String pathnameRegex = artifactclassProcessingtask.isPresent() ? artifactclassProcessingtask.get().getPathnameRegex() : null;
			Set<String> extnsToBeUsed = null; 
			if(pathnameRegex != null) { // if artifactclass_processingtask has a pathregex we need to only get the processable files from that folder path and not from the entire archives directory... e.g., video-pub-edit will have .mov files under output folder
				FiletypePathnameReqexVisitor filetypePathnameReqexVisitor = new FiletypePathnameReqexVisitor(pathnameRegex);
				try {
					Files.walkFileTree(Paths.get(inputArtifactPath), filetypePathnameReqexVisitor);
				} catch (IOException e) {
					// swallow for now
				}
				if(filetypePathnameReqexVisitor != null) {
					pathsToBeUsed.addAll(filetypePathnameReqexVisitor.getPaths());
					if(filetypePathnameReqexVisitor.getExtns().size() > 0) { // if regex contains specific file extns we need to only use processable files with that extn only{
						extnsToBeUsed = filetypePathnameReqexVisitor.getExtns();
					}
				}
			} else { // all processable files from the entire artifact directory
				pathsToBeUsed.add(inputArtifactPath);	
			}
			logger.trace("pathsToBeUsed - " + pathsToBeUsed);
			logger.trace("extnsToBeUsed - " + extnsToBeUsed);
			
			extensions = new ArrayList<String>();
			sidecarExtensions = new ArrayList<String>();
			List<ExtensionFiletype> extn_Filetype_List = filetype.getExtensions(); //extensionFiletypeDao.findAllByFiletypeId(filetype.getId());
			for (ExtensionFiletype extensionFiletype : extn_Filetype_List) {
				String extensionName = extensionFiletype.getExtension().getId();
				if(extnsToBeUsed == null || (extnsToBeUsed != null && extnsToBeUsed.contains(extensionName))) { // if regex contains specific file extns, we need to only use them and not all the extensions_filetype for that particular processingtasks' filetype
					if(extensionFiletype.isSidecar()) {
						sidecarExtensions.add(extensionName);
						includeSidecarFiles = true;
					}
					else
						extensions.add(extensionName);
				}
			}
			extensionsArray = ArrayUtils.toStringArray(extensions.toArray());
			sidecarExtensionsArray = ArrayUtils.toStringArray(sidecarExtensions.toArray());
			
			logger.trace("extensionsArray - " + extensionsArray);
			logger.trace("sidecarExtensionsArray - " + sidecarExtensionsArray);
			for (String nthPathToBeUsed : pathsToBeUsed) {
				logicalFileCollection.addAll(fileHelper.getFiles(nthPathToBeUsed, extensionsArray, includeSidecarFiles, sidecarExtensionsArray));				
			}
		}
		else
			logicalFileCollection.addAll(fileHelper.getFiles(inputArtifactPath, extensionsArray, includeSidecarFiles, sidecarExtensionsArray));
		
		return logicalFileCollection;
	}
}
