package org.ishafoundation.dwaraapi.process.thread;

import java.io.File;
import java.io.FilenameFilter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.dao.master.ProcessingtaskDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ProcessingFailureDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactEntityUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileEntityUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TFileJobDao;
import org.ishafoundation.dwaraapi.db.keys.TFileJobKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.ProcessingFailure;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileJob;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.db.utils.JobUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.helpers.ThreadNameHelper;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
		
//		has dependent tasks?
//			means outputartifactclass is needed
			
//		has a prerequisite task?
//			means ouputartifact of the prerequisite task is the input of the task

@Component
@Scope("prototype")
public class ProcessingJobProcessor implements Runnable{


	private static final Logger logger = LoggerFactory.getLogger(ProcessingJobProcessor.class);
		
	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private JobDao jobDao;	
	
	@Autowired
	private ProcessingtaskDao processingtaskDao;
	
	@Autowired
	private ProcessingFailureDao failureDao;
    
	@Autowired
	private TFileJobDao tFileJobDao;
	
	@Autowired
	private Map<String, IProcessingTask> processingtaskActionMap;

	@Autowired
	private JobUtil jobUtil;	
	
	@Autowired
	private DomainUtil domainUtil;

	@Autowired
	private ArtifactEntityUtil artifactEntityUtil;
	
	@Autowired
	private FileEntityUtil fileEntityUtil;
	
	private Job job;
	private Domain domain;
	private Artifact inputArtifact;

	private org.ishafoundation.dwaraapi.db.model.transactional.domain.File file;
	private LogicalFile logicalFile;
	
	private Artifactclass outputArtifactclass;
	private String outputArtifactName;
	private String outputArtifactPathname;
	private String destinationDirPath;
	

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}
	
	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public Artifact getInputArtifact() {
		return inputArtifact;
	}

	public void setInputArtifact(Artifact inputArtifact) {
		this.inputArtifact = inputArtifact;
	}

	public org.ishafoundation.dwaraapi.db.model.transactional.domain.File getFile() {
		return file;
	}

	public void setFile(org.ishafoundation.dwaraapi.db.model.transactional.domain.File file) {
		this.file = file;
	}

	public LogicalFile getLogicalFile() {
		return logicalFile;
	}

	public void setLogicalFile(LogicalFile logicalFile) {
		this.logicalFile = logicalFile;
	}

	public Artifactclass getOutputArtifactclass() {
		return outputArtifactclass;
	}

	public void setOutputArtifactclass(Artifactclass outputArtifactclass) {
		this.outputArtifactclass = outputArtifactclass;
	}

	public String getOutputArtifactName() {
		return outputArtifactName;
	}

	public void setOutputArtifactName(String outputArtifactName) {
		this.outputArtifactName = outputArtifactName;
	}

	public String getOutputArtifactPathname() {
		return outputArtifactPathname;
	}

	public void setOutputArtifactPathname(String outputArtifactPathname) {
		this.outputArtifactPathname = outputArtifactPathname;
	}

	public String getDestinationDirPath() {
		return destinationDirPath;
	}

	public void setDestinationDirPath(String destinationDirPath) {
		this.destinationDirPath = destinationDirPath;
	}

	@Override
	public void run(){
		String identifierSuffix = "";//mediaFileId+"";//  +  "-" + VideoTasktypeingSteps.PROXY_GENERATION.toString();
		
		// This check is because of the same file getting queued up for processing again...
		// JobManager --> get all "Queued" processingjobs --> ProcessingJobManager ==== thread per file ====> ProcessingJobProcessor --> Only when the file's turn comes the status change to inprogress
		// Next iteration --> get all "Queued" processingjobs would still show the same job above sent already to ProcessingJobManager as it has to wait for its turn for CPU cycle... 
//		FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = null;
//		if(outputArtifactName != null) {
//			String proxyTargetLocation = destinationDirPath + File.separator + FilenameUtils.getBaseName(logicalFile.getAbsolutePath()) + ".mp4";
//			String filepathname = proxyTargetLocation.replace(outputArtifactPathname, outputArtifactName);
//			domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
//			org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileInQuestion = domainSpecificFileRepository.findByPathname(filepathname);
//			if(fileInQuestion != null) {
//				logger.trace(outputArtifactPathname + " already processed. Skipping it");
//				return;
//			}
//		}
		
		String containerName = identifierSuffix;	
		
		ThreadNameHelper threadNameHelper = new ThreadNameHelper();
		threadNameHelper.setThreadName(job.getRequest().getId(), job.getId(), file.getId());
		logger.debug("Will be processing - " + logicalFile.getAbsolutePath());
		Status staus = Status.in_progress;
		String failureReason = null;
		long startms = 0;
		long endms = 0;


		// For every file tasktype call we had to make a DB call just to ensure the job is not cancelled...
		// Its pretty expensive to make a DB call for every tasktype to be run, but its worth to cancel a job than run an expensive tasktype say like transcoding
		TFileJob tFileJob = null;
		String processingtaskName = null;
		try {
			logger.trace("fileId - " + file.getId());
			// if the current status of the job is queued - update it to inprogress and do so with the artifact status and request status...
			Request systemGeneratedRequest = job.getRequest();
			//Request request = systemGeneratedRequest.getRequest();
			job = jobDao.findById(job.getId()).get();
			if(job.getStatus() == Status.on_hold || job.getStatus() == Status.cancelled) {
				logger.warn("Job " + job.getStatus() + " - not processing it now");
				return;
			}
			job = checkAndUpdateStatusToInProgress(job, systemGeneratedRequest); // synchronous method so only one thread can access this at a time
			
			// If the job is QUEUED or IN_PROGRESS and cancellation is initiated ...
			startms = System.currentTimeMillis();
			Integer inputArtifactId = job.getInputArtifactId();

			processingtaskName = job.getProcessingtaskId();
			logger.trace(job.getId() + " " + processingtaskName);
			
			tFileJob = tFileJobDao.findById(new TFileJobKey(file.getId(), job.getId())).get();
			tFileJob.setStatus(Status.in_progress);
			tFileJob.setStartedAt(LocalDateTime.now());
			logger.debug("DB TFileJob Updation for file " + file.getId());
			tFileJobDao.save(tFileJob);
			logger.debug("DB TFileJob Updation - Success");

			
			String inputArtifactName = inputArtifact.getName();
			Artifactclass artifactclass = inputArtifact.getArtifactclass();
			String artifactCategory = artifactclass.getCategory(); // TODO How to distinguish pub/priv
			
			ProcessingtaskResponse processingtaskResponse = processingtaskActionMap.get(processingtaskName).execute(processingtaskName, artifactclass.getId(), inputArtifactName, outputArtifactName, file, domain, logicalFile, artifactCategory, destinationDirPath);
			if(processingtaskResponse == null) // TODO : Handle this...
				return;
			boolean isCancelInitiated = processingtaskResponse.isCancelled();
			
			long proxyEndTime = System.currentTimeMillis();
			boolean isComplete = processingtaskResponse.isComplete();
			if(isComplete) {
				logger.info("Processing complete - " + logicalFile.getAbsolutePath());
				endms = System.currentTimeMillis();
				
				//synchronized (processingtaskResponse) { // A Synchronized block to ensure only one thread at a time updates... Handling it differently with extra checks..
					if(outputArtifactName != null) {
						ArtifactRepository<Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);
						Artifact outputArtifact = artifactRepository.findByName(outputArtifactName); 
						if(outputArtifact == null) {// not already created.
						    outputArtifact = domainUtil.getDomainSpecificArtifactInstance(domain);
							
						    artifactEntityUtil.setDomainSpecificArtifactRef(outputArtifact, inputArtifact);

						    //outputArtifact.setFileStructureMd5("not needed");
						    outputArtifact.setArtifactclass(outputArtifactclass);
						    outputArtifact.setName(outputArtifactName);
						    //outputArtifact.setPrevSequenceCode(inputArtifact.getPrevSequenceCode());
						    //outputArtifact.setSequenceCode(inputArtifact.getSequenceCode());
						    outputArtifact.setSequenceCode(StringUtils.substringBefore(outputArtifactName, "_"));
						    outputArtifact.setqLatestRequest(inputArtifact.getqLatestRequest());
						    outputArtifact.setWriteRequest(inputArtifact.getWriteRequest());
						    
						    try {
						    	outputArtifact = (Artifact) artifactRepository.save(outputArtifact);
						    }
						    catch (Exception e) {
								// possibly updated by another thread already
								outputArtifact = artifactRepository.findByName(outputArtifactName); 
								if(outputArtifact != null)
									logger.trace("OutputArtifact details possibly updated by another thread already");
								else
									throw e;
						    	
							}
						    
						    // setting the current jobs output artifactid
							String logMsgPrefix = "DB Job - " + "(" + job.getId() + ") - Updation - OutputArtifactId " + outputArtifact.getId();
							logger.debug(logMsgPrefix);	
						    job.setOutputArtifactId(outputArtifact.getId());
						    jobDao.save(job);
						    logger.debug(logMsgPrefix + " - Success");	
						    
						    // Now setting all the dependentjobs with the tasktype generated output artifactid
						    setInputArtifactForDependentJobs(job, outputArtifact);
						}

						String proxyFilePathName = processingtaskResponse.getDestinationPathname(); 
						String proxyFileBaseName = FilenameUtils.getBaseName(proxyFilePathName);
						String proxyFilePath = FilenameUtils.getFullPathNoEndSeparator(proxyFilePathName);
						
						// Output Artifact as a file record
						FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
						org.ishafoundation.dwaraapi.db.model.transactional.domain.File artifactFile = domainSpecificFileRepository.findByPathname(outputArtifactName);
						if(artifactFile == null) { // only if not already created... 
							artifactFile = createFile(proxyFilePath + File.separator + outputArtifactName, outputArtifact, domainSpecificFileRepository);	
						}
						
						// creating File records for the process generated files

				        FilenameFilter fileNameFilter = new FilenameFilter() {
				            @Override
				            public boolean accept(File dir, String name) {
				            	if(FilenameUtils.getBaseName(name).equals(proxyFileBaseName))
				            		return true;
				               
				               return false;
				            }
				         };
						String[] files = new File(proxyFilePath).list(fileNameFilter);
						for (String nthFileName : files) {
							String filepathName = proxyFilePath + File.separator + nthFileName;
							logger.trace("Now creating file record for - " + filepathName);
							createFile(filepathName, outputArtifact, domainSpecificFileRepository);	
						}
						
						outputArtifact.setTotalSize(artifactFile.getSize());
					    outputArtifact.setFileCount(files.length);
					    outputArtifact = (Artifact) artifactRepository.save(outputArtifact);
					}
				//}
				staus = Status.completed;
				//logger.info("Proxy for " + containerName + " created successfully in " + ((proxyEndTime - proxyStartTime)/1000) + " seconds - " +  generatedProxyFilePathname);
				logger.debug("Processing Completed in " + ((endms - startms)/1000) + " seconds");
			}
		} catch (Exception e) {
			staus = Status.failed;
			failureReason = "Unable to complete " + processingtaskName + " for " + identifierSuffix + " :: " + e.getMessage();
			logger.error(failureReason, e);
			
			Processingtask processingtask = processingtaskDao.findById(processingtaskName).get();
			int maxErrorsAllowed = processingtask.getMaxErrors();
			long noOfFailuresLogged = failureDao.countByJobId(job.getId());

			// Only the threshold no. of failures on a job need to be persisted in DB...
			if(noOfFailuresLogged < maxErrorsAllowed) {
				// TODO how to ensure the failures logged in are Only unique???
				logger.debug("DB Failure Creation");
				ProcessingFailure failure = new ProcessingFailure(file.getId(), job, e.getMessage());
				failure = failureDao.save(failure);	
				logger.debug("DB Failure Creation - " + failure.getId());
			}
		}
		finally {
			if(tFileJob != null) {
				tFileJob.setStatus(staus);
				logger.debug("DB TFileJob Updation - status to " + staus.toString());
				tFileJobDao.save(tFileJob);
				logger.debug("DB TFileJob Updation - Success");
			}
			threadNameHelper.resetThreadName();
		}
	}

	private org.ishafoundation.dwaraapi.db.model.transactional.domain.File createFile(String fileAbsolutePathName, Artifact outputArtifact, FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository) throws Exception {
	    org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFileRowToBeInserted = domainUtil.getDomainSpecificFileInstance(domain);
		
	    fileEntityUtil.setDomainSpecificFileRef(nthFileRowToBeInserted, file);
	    
	    fileEntityUtil.setDomainSpecificFileArtifact(nthFileRowToBeInserted, outputArtifact);

	    
	    
//		String filePathname = null;
//		if(StringUtils.isBlank(FilenameUtils.getExtension(outputArtifactName))) { // replace the entire output artifact Pathname (/data/transcoded/public/VL22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12) with artifactName(VL22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12)
//			filePathname = fileAbsolutePathName.replace(outputArtifactPathname, outputArtifactName);
//		}
//		else
//			filePathname = fileAbsolutePathName.replace(outputArtifact.getArtifactclass().getPath() + File.separator, ""); // /data/transcoded/public/VL22204_Test2_MBT20481_01.MP4 to VL22204_Test2_MBT20481_01.MP4
	    String filePathname = fileAbsolutePathName.replace(outputArtifact.getArtifactclass().getPath() + File.separator, "");
		nthFileRowToBeInserted.setPathname(filePathname);
		
		// TODO need to be done and set after proxy file is generated
		//nthFileRowToBeInserted.setChecksum(ChecksumUtil.getChecksum(new File(processingtaskResponse.getDestinationPathname()), Checksumtype.sha256)); 
		logger.trace("Calc size of " + filePathname);
		File file = new File(fileAbsolutePathName);
		if(file.exists()) {
			try {
				nthFileRowToBeInserted.setSize(FileUtils.sizeOf(file));
			}catch (Exception e) {
				logger.warn("Weird. File exists but fileutils unable to calculate size. Skipping setting size");
			}
		}
		org.ishafoundation.dwaraapi.db.model.transactional.domain.File savedFile = null;
		logger.debug("DB File Creation");
		try {
			savedFile = domainSpecificFileRepository.save(nthFileRowToBeInserted);
		}
		catch (Exception e) {
			nthFileRowToBeInserted = domainSpecificFileRepository.findByPathname(filePathname);
			// This check is because of the same file getting queued up for processing again...
			// JobManager --> get all "Queued" processingjobs --> ProcessingJobManager ==== thread per file ====> ProcessingJobProcessor --> Only when the file's turn comes the status change to inprogress
			// Next iteration --> get all "Queued" processingjobs would still show the same job above sent already to ProcessingJobManager as it has to wait for its turn for CPU cycle... 
			if(nthFileRowToBeInserted != null)
				logger.trace("File details possibly updated by another thread already");
			else
				throw e;
		}
		logger.debug("DB File Creation - Success");
		return savedFile;
	}

	/**
	 * Sets inputArtifact for all dependent Jobs of the current job(running process job that generates the output)
	 * Iterates through the job list on request and  
	 * @param job
	 * @param inputArtifactForDependentJobs
	 */
	private void setInputArtifactForDependentJobs(Job job, Artifact inputArtifactForDependentJobs) {
		List<Job> dependentJobList = jobUtil.getDependentJobs(job);
		for (Job nthDependentJob : dependentJobList) {
			nthDependentJob.setInputArtifactId(inputArtifactForDependentJobs.getId()); // output artifact of the current job is the input artifact of the dependent job
			String logMsgPrefix2 = "DB Job - " + "(" + nthDependentJob.getId() + ") - Updation - InputArtifactId " + inputArtifactForDependentJobs.getId();
			logger.debug(logMsgPrefix2);	
		    jobDao.save(nthDependentJob);
		    logger.debug(logMsgPrefix2 + " - Success");
		}
	}
	
	private synchronized Job checkAndUpdateStatusToInProgress(Job job, Request systemGeneratedRequest){
		if(job.getStatus() == Status.queued) {
			Status status = Status.in_progress;
			
			String logMsgPrefix = "DB Job - " + "(" + job.getId() + ") - Updation - status to " + status;
			logger.debug(logMsgPrefix);	
			job.setStatus(status);
			job.setStartedAt(LocalDateTime.now());
			job.setMessage(null);
			jobDao.save(job);
			logger.debug(logMsgPrefix + " - Success");					


			if(systemGeneratedRequest.getStatus() != status) {
				String logMsg = "DB Request - " + systemGeneratedRequest.getId() + " - Update - status to " + status;
				logger.debug(logMsg);
		        systemGeneratedRequest.setStatus(status);
		        systemGeneratedRequest = requestDao.save(systemGeneratedRequest);
		        logger.debug(logMsg + " - Success");
			}			
		}
		return job;
	}

}
