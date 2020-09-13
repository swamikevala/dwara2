package org.ishafoundation.dwaraapi.process.thread;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.FiletypeDao;
import org.ishafoundation.dwaraapi.db.dao.master.ProcessingtaskDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepositoryUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TFileJobDao;
import org.ishafoundation.dwaraapi.db.keys.TFileJobKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Filetype;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
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

	@Override
    public void run() {
		logger.trace("Managing processing job - " + job.getId());
		
		// This check is because of the same file getting queued up for processing again...
		// JobManager --> get all "Queued" processingjobs --> ProcessingJobManager ==== thread per file ====> ProcessingJobProcessor --> Only when the file's turn comes the status change to inprogress
		// Next iteration --> get all "Queued" processingjobs would still show the same job above sent already to ProcessingJobManager as it has to wait for its turn for CPU cycle... 
		Status jobStatus = job.getStatus();
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
			
			String artifactName = inputArtifact.getName();
			Artifactclass artifactclass = inputArtifact.getArtifactclass();
			String inputArtifactPath = artifactclass.getPath() + File.separator + artifactName;
	
			// For the task getting processed check
			// 1) if there are any dependent tasks 
			// 2) and is responsible for generating a artifactclass
			
			// then it means the output of the current task is the input for the dependent task
			
			
			String outputArtifactName = null;
			String outputArtifactPathname = null; // holds where to generate the files in the physical system...
			if(outputArtifactclass != null) {
				outputArtifactName = getOutputArtifactName(outputArtifactclass, artifactName);
				outputArtifactPathname = getOutputArtifactPathname(outputArtifactclass, outputArtifactName);
			}
			logger.trace("inputArtifactPath " + inputArtifactPath);
			logger.trace("outputArtifactName " + outputArtifactName);
			logger.trace("outputArtifactPathname " + outputArtifactPathname);
			
			HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.domain.File> filePathToFileObj = getFilePathToFileObj(domain, inputArtifact);
	
			// TODO Cache filetypes...
			Filetype ft = null;
			if(!processingtask.getFiletypeId().equals("_all_")) {
				ft = filetypeDao.findById(processingtask.getFiletypeId()).get();
			}
				
			Collection<LogicalFile> selectedFileList = getLogicalFileList(ft, inputArtifactPath);
			int filesToBeProcessedCount = selectedFileList.size();
			logger.trace("filesToBeProcessedCount " + filesToBeProcessedCount);
			
			if(filesToBeProcessedCount == 0)
				throw new Exception("No files to process. Check supported extensions...");
			
			for (Iterator<LogicalFile> iterator = selectedFileList.iterator(); iterator.hasNext();) {
				LogicalFile logicalFile = (LogicalFile) iterator.next(); // would have an absolute file like C:\data\ingested\14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A\1 CD\00018.MTS and its sidecar files
				String logicalFilePath = logicalFile.getAbsolutePath();
				logger.trace("logicalFilePath - " + logicalFilePath);
				if(logicalFilePath.contains(configuration.getJunkFilesStagedDirName())) { // skipping junk files
					logger.trace("Junk file. Skipping it");
					continue;			
				}
				String filePath = FilenameUtils.getFullPath(logicalFilePath) + FilenameUtils.getName(logicalFilePath);
				
				String artifactNamePrefixedFilePathname = null; // 14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A.MP4 || 14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A\1 CD\00018.MTS
				String outputFilePath = null; // /data/transcoded/public
				if(logicalFilePath.equals(inputArtifactPath)) { // means input artifact is a file and not a directory
					artifactNamePrefixedFilePathname = artifactName; // logicalFilePath.replace(inputArtifactPath, artifactName); // would hold 14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A.MP4
					if(outputArtifactPathname != null)
						outputFilePath = FilenameUtils.getFullPathNoEndSeparator(outputArtifactPathname);
				}
				else {
					String filePathnameWithoutArtifactNamePrefixed = logicalFilePath.replace(inputArtifactPath + File.separator, ""); // would hold 1 CD\00018.MTS or just 00019.MTS
					artifactNamePrefixedFilePathname = logicalFilePath.replace(inputArtifactPath + File.separator, artifactName + File.separator); // would hold 14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A\1 CD\00018.MTS
					
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
				logger.trace("file - " + file.getId());

				// Requeue scenario - Only failed files are to be continued...
				Optional<TFileJob> tFileJobDB = tFileJobDao.findById(new TFileJobKey(file.getId(), job.getId()));
				if(tFileJobDB.isPresent() && tFileJobDB.get().getStatus() != Status.failed) {
					logger.debug(job.getId() + " already Inprogress/completed. Skipping it...");
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
						logger.debug(job.getId() + " already in ProcessingJobProcessor queue. Skipping it...");
						alreadyQueued = true;
						break;
					}
				}
				
				if(!alreadyQueued) { // only when the job is not already dispatched to the queue to be executed, send it now...
					TFileJob tFileJob = new TFileJob();
					tFileJob.setId(new TFileJobKey(file.getId(), job.getId()));
					tFileJob.setJob(job);
					tFileJob.setArtifactId(inputArtifactId);
					tFileJob.setStatus(Status.queued);
					logger.debug("DB TFileJob Creation for file " + file.getId());
					tFileJobDao.save(tFileJob);
					logger.debug("DB TFileJob Creation - Success");
					
					ProcessingJobProcessor processingJobProcessor = applicationContext.getBean(ProcessingJobProcessor.class);
					processingJobProcessor.setJob(job);
					processingJobProcessor.setDomain(domain);
					processingJobProcessor.setInputArtifact(inputArtifact);
					processingJobProcessor.setFileCount(filesToBeProcessedCount);
					processingJobProcessor.setTotalSize(0); // TODO How to calculate this?
					processingJobProcessor.setFile(file);
					processingJobProcessor.setLogicalFile(logicalFile);
					
					processingJobProcessor.setOutputArtifactclass(outputArtifactclass);
					processingJobProcessor.setOutputArtifactName(outputArtifactName); // VL20190701_071239.mp4
					processingJobProcessor.setOutputArtifactPathname(outputArtifactPathname); // C:\data\transcoded\public\VL20190701_071239.mp4
					processingJobProcessor.setDestinationDirPath(outputFilePath);
					logger.info("Now kicking off - " + job.getId() + " " + logicalFilePath + " task " + processingtaskId);
					executor.execute(processingJobProcessor);
				}
			}
			// TODO if no. of errors in the tasktype reach the configured max_errors threshold then we stop further processing.... count(*) on failures for the job_id...
	
		}catch (Exception e) {
			logger.error("Unable to proceed on job - " + job.getId(), e);
			String logMsgPrefix = "DB Job - " + "(" + job.getId() + ") - Updation - status to " + Status.failed;
			logger.debug(logMsgPrefix);	
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
		return inputArtifactName.replace(inputArtifactSeqCode, outputArtifactSeqCode);
	}

//	private String getOutputArtifactName(Artifactclass outputArtifactclass, String inputArtifactName,  Artifactclass inputArtifactclass){
//		String inputArtifactClassSequenceId = inputArtifactclass.getSequenceId();
//		Sequence inputArtifactClassSequence = configurationTablesUtil.getSequence(inputArtifactClassSequenceId);
//		String inputArtifactSeqCode = sequenceUtil.getExtractedCode(inputArtifactClassSequence, inputArtifactName);
//
//		
//		String outputArtifactClassSequenceId = outputArtifactclass.getSequenceId();
//		Sequence outputArtifactClassSequence = configurationTablesUtil.getSequence(outputArtifactClassSequenceId);
//		String outputArtifactSeqCode = sequenceUtil.getSequenceCode(outputArtifactClassSequence, inputArtifactName);
//		return inputArtifactName.replace(inputArtifactSeqCode, outputArtifactSeqCode);
//	}
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
	
	private Collection<LogicalFile> getLogicalFileList(Filetype filetype, String inputArtifactPath){
		
		List<String> extensions = null;
		String[] extensionsArray = null;
		List<String> sidecarExtensions = null;
		String[] sidecarExtensionsArray = null;
		boolean includeSidecarFiles = false;
		if(filetype != null) {
			extensions = new ArrayList<String>();
			sidecarExtensions = new ArrayList<String>();
			List<ExtensionFiletype> extn_Filetype_List = filetype.getExtensions(); //extensionFiletypeDao.findAllByFiletypeId(filetype.getId());
			for (ExtensionFiletype extensionFiletype : extn_Filetype_List) {
				String extensionName = extensionFiletype.getExtension().getId();
				
				if(extensionFiletype.isSidecar()) {
					sidecarExtensions.add(extensionName);
					includeSidecarFiles = true;
				}
				else
					extensions.add(extensionName);
			}
			extensionsArray = ArrayUtils.toStringArray(extensions.toArray());
			sidecarExtensionsArray = ArrayUtils.toStringArray(sidecarExtensions.toArray());
		}
		
		return fileHelper.getFiles(inputArtifactPath, extensionsArray, includeSidecarFiles, sidecarExtensionsArray);
	}
}
