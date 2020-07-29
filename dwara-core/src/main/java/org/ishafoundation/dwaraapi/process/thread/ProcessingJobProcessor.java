package org.ishafoundation.dwaraapi.process.thread;

import java.io.File;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.db.dao.transactional.FailureDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileJobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.keys.TFileJobKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Failure;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileJob;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.utils.Md5Util;
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
	private FailureDao failureDao;
    
	@Autowired
	private TFileJobDao tFileJobDao;
	
	@Autowired
	private Map<String, IProcessingTask> processingtaskActionMap;
	
	@Autowired
	private DomainUtil domainUtil;

	protected Job job;
	protected Domain domain;
	protected Artifact inputArtifact;
	
	protected org.ishafoundation.dwaraapi.db.model.transactional.domain.File file;
	protected LogicalFile logicalFile;
	
	protected String outputArtifactName;
	protected String outputArtifactPathname;
	protected String destinationDirPath;
	

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
		logger.trace("running - " + job.getId() + ":" + logicalFile.getAbsolutePath());
		
		String containerName = identifierSuffix;	
		
//		ThreadNameHelper threadNameHelper = new ThreadNameHelper();
		// TODO - threadNameHelper.setThreadName(request.getMediaArtifactId(), identifierSuffix + "-" + VideoTasktypeingSteps.TRANSCODING.toString());
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
			job = checkAndUpdateStatusToInProgress(job, systemGeneratedRequest); // synchronous method so only one thread can access this at a time
			
			// If the job is QUEUED or IN_PROGRESS and cancellation is initiated ...
			if(job.getStatus() == Status.cancelled || job.getStatus() == Status.aborted) { 	
				staus = Status.cancelled; // Tasktype status is Cancelled because its yet to begin... // TODO revisit this...
			}
			else {
				startms = System.currentTimeMillis();
				Integer inputArtifactId = job.getInputArtifactId();

				processingtaskName = job.getProcessingtaskId();
				logger.trace(job.getId() + " " + processingtaskName);
				
				tFileJob = new TFileJob();
				tFileJob.setId(new TFileJobKey(file.getId(), job.getId()));
				tFileJob.setJob(job);
				tFileJob.setArtifactId(inputArtifactId);
				tFileJob.setStatus(Status.in_progress);
				tFileJob.setStartedAt(LocalDateTime.now());
				logger.debug("DB TFileJob Creation for file " + file.getId());
				tFileJobDao.save(tFileJob);
				logger.debug("DB TFileJob Creation - Success");

				
				String inputArtifactName = inputArtifact.getName();
				Artifactclass artifactclass = inputArtifact.getArtifactclass();
				String artifactCategory = "???";
				
				ProcessingtaskResponse processingtaskResponse = processingtaskActionMap.get(processingtaskName).execute(processingtaskName, inputArtifactName, file, domain, logicalFile, artifactCategory, destinationDirPath);
				if(processingtaskResponse == null) // TODO : Handle this...
					return;
				boolean isCancelInitiated = processingtaskResponse.isCancelled();
				
				long proxyEndTime = System.currentTimeMillis();
				boolean isComplete = processingtaskResponse.isComplete();
				if(isComplete) {
					logger.trace("Processing Task " + processingtaskName + " execution completed for job " + job.getId());
					endms = System.currentTimeMillis();
					
					if(outputArtifactName != null) {
						ArtifactRepository<Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);

						Artifact outputArtifact = artifactRepository.findByName(outputArtifactName); 
						if(outputArtifact == null) {// not already created.
						    outputArtifact = domainUtil.getDomainSpecificArtifactInstance(domain);
							Method artifactRefSetter = outputArtifact.getClass().getMethod("set" + outputArtifact.getClass().getSimpleName() + "Ref", outputArtifact.getClass());
							artifactRefSetter.invoke(outputArtifact, inputArtifact); //sourceArtifactId
							
						    outputArtifact.setFileCount(1111111);
						    outputArtifact.setFileStructureMd5("not needed");
						    
						    Artifactclass outputArtifactclass = job.getActionelement().getOutputArtifactclass();
						    outputArtifact.setArtifactclass(outputArtifactclass);
						    outputArtifact.setName(outputArtifactName);
						    
						    outputArtifact = (Artifact) artifactRepository.save(outputArtifact);
			
						    // setting the current jobs output artifactid
							String logMsgPrefix = "DB Job - " + "(" + job.getId() + ") - Updation - OutputArtifactId " + outputArtifact.getId();
							logger.debug(logMsgPrefix);	
						    job.setOutputArtifactId(outputArtifact.getId());
						    jobDao.save(job);
						    logger.debug(logMsgPrefix + " - Success");	
						    
						    // Now setting all the dependentjobs with the tasktype generated output artifactid
						    List<Job> jobList = jobDao.findAllByJobRefId(job.getId());
					
							for (Iterator<Job> iterator = jobList.iterator(); iterator.hasNext();) {
								Job nthDependentJob = (Job) iterator.next();
	
								nthDependentJob.setInputArtifactId(outputArtifact.getId()); // output artifact of the current job is the input artifact of the dependent job
								String logMsgPrefix2 = "DB Job - " + "(" + nthDependentJob.getId() + ") - Updation - InputArtifactId " + outputArtifact.getId();
								logger.debug(logMsgPrefix2);	
							    jobDao.save(nthDependentJob);
							    logger.debug(logMsgPrefix2 + " - Success");
							}	    		
						}	    

					    org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFileRowToBeInserted = domainUtil.getDomainSpecificFileInstance(domain);

					    Method fileRefSetter = nthFileRowToBeInserted.getClass().getMethod("set" + file.getClass().getSimpleName() + "Ref", file.getClass());
					    fileRefSetter.invoke(nthFileRowToBeInserted, file);
						
					    Method fileArtifactSetter = nthFileRowToBeInserted.getClass().getMethod("set" + outputArtifact.getClass().getSimpleName(), outputArtifact.getClass());
						fileArtifactSetter.invoke(nthFileRowToBeInserted, outputArtifact);
						
						String filePathname = processingtaskResponse.getDestinationPathname().replace(outputArtifactPathname, outputArtifactName);
						nthFileRowToBeInserted.setPathname(filePathname);
						
						// TODO need to be done and set after proxy file is generated
						nthFileRowToBeInserted.setChecksum(Md5Util.getChecksum(new File(processingtaskResponse.getDestinationPathname()), Checksumtype.sha256)); 
						nthFileRowToBeInserted.setSize(9999);// TODO Hardcoded...
				    	logger.debug("DB File Creation");
				    	FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
				    	domainSpecificFileRepository.save(nthFileRowToBeInserted);
						logger.debug("DB File Creation - Success");
					}

					staus = Status.completed;
					//logger.info("Proxy for " + containerName + " created successfully in " + ((proxyEndTime - proxyStartTime)/1000) + " seconds - " +  generatedProxyFilePathname);
					logger.info("Processing Completed in " + ((endms - startms)/1000) + " seconds");
				}else {
					if(isCancelInitiated){
						staus = Status.aborted;
						logger.info("Processingtask " + containerName + " aborted");
					} 
					else
						throw new Exception("Unable to process " + FilenameUtils.getBaseName(logicalFile.getAbsolutePath()) + " : because : " + processingtaskResponse.getFailureReason());
				}
			}
		} catch (Exception e) {
			staus = Status.failed;
			failureReason = "Unable to complete " + processingtaskName + " for " + identifierSuffix + " :: " + e.getMessage();
			logger.error(failureReason, e);
			
			// TODO : Work on the threshold failures...
			// create failed_media_file_run table
			logger.debug("DB Failure Creation");
			Failure failure = new Failure();
			failure.setFileId(file.getId());
			failure.setJob(job);
			failure = failureDao.save(failure);	
			logger.debug("DB Failure Creation - " + failure.getId());   
		}
		finally {
			if(tFileJob != null) {
				tFileJob.setStatus(staus);
				logger.debug("DB TFileJob Updation - status to " + staus.toString());
				tFileJobDao.save(tFileJob);
				logger.debug("DB TFileJob Updation - Success");
			}
//			threadNameHelper.resetThreadName();
		}
	}
	
	protected synchronized Job checkAndUpdateStatusToInProgress(Job job, Request systemGeneratedRequest){
		if(job.getStatus() == Status.queued) {
			Status status = Status.in_progress;
			
			String logMsgPrefix = "DB Job - " + "(" + job.getId() + ") - Updation - status to " + status;
			logger.debug(logMsgPrefix);	
			job.setStatus(status);
			job.setStartedAt(LocalDateTime.now());
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
