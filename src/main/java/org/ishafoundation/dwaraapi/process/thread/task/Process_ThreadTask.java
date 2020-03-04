package org.ishafoundation.dwaraapi.process.thread.task;

import java.io.File;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.db.cacheutil.ProcessCacheUtil;
import org.ishafoundation.dwaraapi.db.dao.master.ingest.LibraryclassDao;
import org.ishafoundation.dwaraapi.db.dao.master.workflow.TaskDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FailureDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.LibraryDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TmpJobFileDao;
import org.ishafoundation.dwaraapi.db.model.master.ingest.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.workflow.Task;
import org.ishafoundation.dwaraapi.db.model.transactional.Failure;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.db.model.transactional.TmpJobFile;
import org.ishafoundation.dwaraapi.helpers.ThreadNameHelper;
import org.ishafoundation.dwaraapi.job.JobUtils;
import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.model.LogicalFile;
import org.ishafoundation.dwaraapi.process.factory.ProcessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

//		is process filelevel?
//			what does it mean?
		
//		has dependent tasks?
//			means outputlibraryclass is needed
			
//		has a prerequisite task?
//			means ouputlibrary of the prerequisite task is the input of the task

@Component
@Scope("prototype")
public class Process_ThreadTask implements Runnable{

	private static final Logger logger = LoggerFactory.getLogger(Process_ThreadTask.class);
	
	@Autowired
    private LibraryclassDao libraryclassDao;
	
	@Autowired
    private LibraryDao libraryDao;

	@Autowired
	private RequestDao requestDao;

	@Autowired
	private SubrequestDao subrequestDao;
	
	@Autowired
	private JobDao jobDao;	
	
	@Autowired
	private FailureDao failureDao;
    
	@Autowired
	private FileDao fileDao;
	
	@Autowired
	private TmpJobFileDao tmpJobFileDao;
	
	@Autowired
	private TaskDao taskDao;	
	
	@Autowired
	private JobUtils jobUtils;
	
	@Autowired
	private ApplicationContext applicationContext;	
	
	@Autowired
	private ProcessCacheUtil processCacheUtil;		
	
	protected int libraryId;
	protected String libraryName;
	protected int jobId;
	protected int fileId;
	protected LogicalFile logicalFile;
	protected String category;
	protected String outputLibraryPath;
	protected String destinationFilePath;
	

	public int getLibraryId() {
		return libraryId;
	}

	public void setLibraryId(int libraryId) {
		this.libraryId = libraryId;
	}

	public String getLibraryName() {
		return libraryName;
	}

	public void setLibraryName(String libraryName) {
		this.libraryName = libraryName;
	}

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public LogicalFile getLogicalFile() {
		return logicalFile;
	}

	public void setLogicalFile(LogicalFile logicalFile) {
		this.logicalFile = logicalFile;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getOutputLibraryPath() {
		return outputLibraryPath;
	}

	public void setOutputLibraryPath(String outputLibraryPath) {
		this.outputLibraryPath = outputLibraryPath;
	}

	public String getDestinationFilePath() {
		return destinationFilePath;
	}

	public void setDestinationFilePath(String destinationFilePath) {
		this.destinationFilePath = destinationFilePath;
	}

	@Override
	public void run(){
		String identifierSuffix = "";//mediaFileId+"";//  +  "-" + VideoProcessingSteps.PROXY_GENERATION.toString();
		logger.trace("running jobId - " + jobId);
		String containerName = identifierSuffix;	
		
		ThreadNameHelper threadNameHelper = new ThreadNameHelper();
		// TODO - threadNameHelper.setThreadName(request.getMediaLibraryId(), identifierSuffix + "-" + VideoProcessingSteps.TRANSCODING.toString());
		Status processStatus = Status.IN_PROGRESS;
		String failureReason = null;
		long startms = 0;
		long endms = 0;


		// For every file process call we had to make a DB call just to ensure the job is not cancelled...
		// Its pretty expensive to make a DB call for every process to be run, but its worth to cancel a job than run an expensive process say like transcoding
		TmpJobFile tmpJobFile = null;
		String taskName = null;
		try {

			// if the current status of the job is queued - update it to inprogress and do so with the library status and request status...
			Job job = jobDao.findById(jobId).get();
			Subrequest systemGeneratedRequest = subrequestDao.findById(job.getSubrequestId()).get();
			Request request = requestDao.findById(systemGeneratedRequest.getRequestId()).get();
			job = checkAndUpdateStatusToInProgress(job, systemGeneratedRequest); // synchronous method so only one thread can access this at a time
			
			// If the job is QUEUED or IN_PROGRESS and cancellation is initiated ...
			if(job.getStatusId() == Status.CANCELLED.getStatusId() || job.getStatusId() == Status.ABORTED.getStatusId()) { 	
				processStatus = Status.CANCELLED; // Process status is Cancelled because its yet to begin...
			}
			else {
				startms = System.currentTimeMillis();
				
				tmpJobFile = new TmpJobFile();
				tmpJobFile.setFileId(fileId);
				tmpJobFile.setJobId(jobId);
				tmpJobFile.setLibraryId(libraryId);
				tmpJobFile.setStatusId(Status.IN_PROGRESS.getStatusId());
				tmpJobFile.setStartedAt(System.currentTimeMillis());
				logger.debug("DB TmpJobFile Creation");
				tmpJobFileDao.save(tmpJobFile);
				logger.debug("DB TmpJobFile Creation - Success");

				int taskId = job.getTaskId();
				Task task = taskDao.findById(taskId).get();
				taskName = task.getName();
				logger.trace(jobId + " " + taskName);
				// call the process methods...
				
				org.ishafoundation.dwaraapi.db.model.master.workflow.Process processDBEntity = processCacheUtil.getProcess(task.getProcessId());
				String processName = processDBEntity.getName().toUpperCase();
				
				IProcessor processor = ProcessFactory.getInstance(applicationContext, processName);
				CommandLineExecutionResponse commandLineExecutionResponse = processor.process(taskName, libraryName, fileId, logicalFile, category, destinationFilePath);
				if(commandLineExecutionResponse == null) // TODO : Handle this...
					return;
				boolean isCancelInitiated = commandLineExecutionResponse.isCancelled();
				
				long proxyEndTime = System.currentTimeMillis();
				boolean isComplete = commandLineExecutionResponse.isComplete();
				if(isComplete) {
					endms = System.currentTimeMillis();
					Libraryclass outputLibraryclass = libraryclassDao.findByTaskId(taskId); //  the task output's resultant libraryclass
					String outputLibraryName = StringUtils.substringAfterLast(outputLibraryPath, File.separator);
					if(outputLibraryclass != null) {
						Library outputLibrary = libraryDao.findByName(outputLibraryName); 
						if(outputLibrary == null) {// not already created.
						    outputLibrary = new Library();
						    outputLibrary.setLibraryIdRef(libraryId); //sourceLibraryId
						    outputLibrary.setFileCount(111);
						    outputLibrary.setFileStructureMd5("not needed");
						    outputLibrary.setLibraryclassId(outputLibraryclass.getLibraryclassId());
						    outputLibrary.setName(outputLibraryName);
						    
						    outputLibrary = libraryDao.save(outputLibrary);
			
						    // setting the current jobs output libraryid
							String logMsgPrefix = "DB Job - " + "(" + jobId + ") - Updation - OutputLibraryId " + outputLibrary.getLibraryId();
							logger.debug(logMsgPrefix);	
						    job.setOutputLibraryId(outputLibrary.getLibraryId());
						    jobDao.save(job);
						    logger.debug(logMsgPrefix + " - Success");	
						    
						    // Now setting all the dependentjobs with the process generated output libraryid
						    List<Job> jobList = jobUtils.getDependentJobs(job, request.getRequesttypeId(), request.getLibraryclassId());
					
							for (Iterator<Job> iterator = jobList.iterator(); iterator.hasNext();) {
								Job nthDependentJob = (Job) iterator.next();
	
								nthDependentJob.setInputLibraryId(outputLibrary.getLibraryId());
								String logMsgPrefix2 = "DB Job - " + "(" + nthDependentJob.getJobId() + ") - Updation - InputLibraryId " + outputLibrary.getLibraryId();
								logger.debug(logMsgPrefix2);	
							    jobDao.save(nthDependentJob);
							    logger.debug(logMsgPrefix2 + " - Success");
							}	    		
						}	    

					    int outputLibraryId = outputLibrary.getLibraryId();
						    
				    
						org.ishafoundation.dwaraapi.db.model.transactional.File nthFileRowToBeInserted = new org.ishafoundation.dwaraapi.db.model.transactional.File();
						nthFileRowToBeInserted.setFileIdRef(fileId);
						nthFileRowToBeInserted.setLibraryId(outputLibraryId);
						//nthFileRowToBeInserted.setFiletypeId(filetypeId); // TODO How?
						nthFileRowToBeInserted.setPathname(destinationFilePath + FilenameUtils.getName(logicalFile.getAbsolutePath()));
						
						// TODO need to be done and set after proxy file is generated
						nthFileRowToBeInserted.setCrc("crc_for_proxy"); 
						nthFileRowToBeInserted.setSize(9999);// TODO Hardcoded...
				    	logger.debug("DB File Creation");   
						fileDao.save(nthFileRowToBeInserted);
						logger.debug("DB File Creation - Success");
					}
					processStatus = Status.COMPLETED;
					//logger.info("Proxy for " + containerName + " created successfully in " + ((proxyEndTime - proxyStartTime)/1000) + " seconds - " +  generatedProxyFilePathname);
					logger.info("Processing Completed in " + ((endms - startms)/1000) + " seconds");
				}else {
					if(isCancelInitiated){
						processStatus = Status.ABORTED;
						logger.info("Process for " + containerName + " aborted");
					} 
					else
						throw new Exception("Unable to process" + FilenameUtils.getBaseName(logicalFile.getAbsolutePath()) + " : because : " + commandLineExecutionResponse.getFailureReason());
				}
			}
		} catch (Exception e) {
			processStatus = Status.FAILED;
			failureReason = "Unable to complete " + taskName + " process for " + identifierSuffix + " :: " + e.getMessage();
			logger.error(failureReason);
			
			// TODO : Work on the threshold failures...
			// create failed_media_file_run table
			logger.debug("DB Failure Creation");
			Failure failure = new Failure();
			failure.setFileId(fileId);
			failure.setJobId(jobId);
			failure = failureDao.save(failure);	
			logger.debug("DB Failure Creation - " + failure.getFailureId());   
		}
		finally {
			if(tmpJobFile != null) {
				tmpJobFile.setStatusId(processStatus.getStatusId());
				logger.debug("DB TmpJobFile Updation - status to " + processStatus.toString());
				tmpJobFileDao.save(tmpJobFile);
				logger.debug("DB TmpJobFile Updation - Success");
			}
			threadNameHelper.resetThreadName();
		}
	}
	
	protected synchronized Job checkAndUpdateStatusToInProgress(Job job, Subrequest systemGeneratedRequest){
		if(job.getStatusId() == Status.QUEUED.getStatusId()) {
			String status = Status.IN_PROGRESS.toString();
			int statusId = Status.valueOf(status).getStatusId();
			long startedOn = Calendar.getInstance().getTimeInMillis();
			
			String logMsgPrefix = "DB Job - " + "(" + jobId + ") - Updation - status to " + status;
			logger.debug(logMsgPrefix);	
			job.setStatusId(statusId);
			job.setStartedAt(startedOn);
			jobDao.save(job);
			logger.debug(logMsgPrefix + " - Success");					


			if(systemGeneratedRequest.getStatusId() != statusId) {
				String logMsg = "DB Subrequest - " + systemGeneratedRequest.getSubrequestId() + " - Update - status to " + status;
				logger.debug(logMsg);
		        systemGeneratedRequest.setStatusId(statusId);
		        systemGeneratedRequest = subrequestDao.save(systemGeneratedRequest);
		        logger.debug(logMsg + " - Success");
			}			
		}
		return job;
	}

}
