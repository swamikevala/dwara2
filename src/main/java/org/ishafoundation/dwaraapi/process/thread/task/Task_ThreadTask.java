package org.ishafoundation.dwaraapi.process.thread.task;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.db.dao.master.ApplicationDao;
import org.ishafoundation.dwaraapi.db.dao.master.LibraryclassDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ApplicationFileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FailureDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.LibraryDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileJobDao;
import org.ishafoundation.dwaraapi.db.keys.TFileJobKey;
import org.ishafoundation.dwaraapi.db.model.master.Application;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.Task;
import org.ishafoundation.dwaraapi.db.model.transactional.Failure;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ApplicationFile;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileJob;
import org.ishafoundation.dwaraapi.helpers.ThreadNameHelper;
import org.ishafoundation.dwaraapi.job.JobUtils;
import org.ishafoundation.dwaraapi.model.LogicalFile;
import org.ishafoundation.dwaraapi.model.TaskResponse;
import org.ishafoundation.dwaraapi.process.factory.TaskFactory;
import org.ishafoundation.dwaraapi.process.thread.task.mam.MamUpdateTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

//		is tasktype filelevel?
//			what does it mean?
		
//		has dependent tasks?
//			means outputlibraryclass is needed
			
//		has a prerequisite task?
//			means ouputlibrary of the prerequisite task is the input of the task

@Component
@Scope("prototype")
public class Task_ThreadTask implements Runnable{


	private static final Logger logger = LoggerFactory.getLogger(Task_ThreadTask.class);
	
	@Autowired
    private LibraryclassDao libraryclassDao;
	
	@Autowired
    private LibraryDao libraryDao;

	@Autowired
	private SubrequestDao subrequestDao;
	
	@Autowired
	private JobDao jobDao;	
	
	@Autowired
	private FailureDao failureDao;
    
	@Autowired
	private FileDao fileDao;
	
	@Autowired
	private TFileJobDao tFileJobDao;

	@Autowired
	private ApplicationDao applicationDao;
	
	@Autowired
	private ApplicationFileDao applicationFileDao;
	
	@Autowired
	private JobUtils jobUtils;
	
	@Autowired
	private ApplicationContext applicationContext;	
	

	protected Job job;
	protected org.ishafoundation.dwaraapi.db.model.transactional.File file;
	protected LogicalFile logicalFile;
	
	protected String outputLibraryName;
	protected String outputLibraryPathname;
	protected String destinationDirPath;
	

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public org.ishafoundation.dwaraapi.db.model.transactional.File getFile() {
		return file;
	}

	public void setFile(org.ishafoundation.dwaraapi.db.model.transactional.File file) {
		this.file = file;
	}

	public LogicalFile getLogicalFile() {
		return logicalFile;
	}

	public void setLogicalFile(LogicalFile logicalFile) {
		this.logicalFile = logicalFile;
	}

	public String getOutputLibraryName() {
		return outputLibraryName;
	}

	public void setOutputLibraryName(String outputLibraryName) {
		this.outputLibraryName = outputLibraryName;
	}

	public String getOutputLibraryPathname() {
		return outputLibraryPathname;
	}

	public void setOutputLibraryPathname(String outputLibraryPathname) {
		this.outputLibraryPathname = outputLibraryPathname;
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
		logger.trace("running jobId - " + job.getId());
		String containerName = identifierSuffix;	
		
		ThreadNameHelper threadNameHelper = new ThreadNameHelper();
		// TODO - threadNameHelper.setThreadName(request.getMediaLibraryId(), identifierSuffix + "-" + VideoTasktypeingSteps.TRANSCODING.toString());
		Status tasktypeStatus = Status.in_progress;
		String failureReason = null;
		long startms = 0;
		long endms = 0;


		// For every file tasktype call we had to make a DB call just to ensure the job is not cancelled...
		// Its pretty expensive to make a DB call for every tasktype to be run, but its worth to cancel a job than run an expensive tasktype say like transcoding
		TFileJob tFileJob = null;
		String taskName = null;
		try {

			// if the current status of the job is queued - update it to inprogress and do so with the library status and request status...
			Subrequest systemGeneratedRequest = job.getSubrequest();
			Request request = systemGeneratedRequest.getRequest();
			job = checkAndUpdateStatusToInProgress(job, systemGeneratedRequest); // synchronous method so only one thread can access this at a time
			
			// If the job is QUEUED or IN_PROGRESS and cancellation is initiated ...
			if(job.getStatus() == Status.cancelled || job.getStatus() == Status.aborted) { 	
				tasktypeStatus = Status.cancelled; // Tasktype status is Cancelled because its yet to begin... // TODO revisit this...
			}
			else {
				startms = System.currentTimeMillis();
				
				tFileJob = new TFileJob();
				tFileJob.setId(new TFileJobKey(file.getId(), job.getId()));
				tFileJob.setFile(file);
				tFileJob.setJob(job);
				tFileJob.setLibrary(job.getInputLibrary());
				tFileJob.setStatus(Status.in_progress);
				tFileJob.setStartedAt(LocalDateTime.now());
				logger.debug("DB TFileJob Creation");
				tFileJobDao.save(tFileJob);
				logger.debug("DB TFileJob Creation - Success");

				Task task = job.getTask();
				int taskId = task.getId();
				taskName = task.getName();
				logger.trace(job.getId() + " " + taskName);
				
				ITaskExecutor taskExecutor = TaskFactory.getInstance(applicationContext, taskName);
				String inputLibraryName = job.getInputLibrary().getName();
				String libraryCategory = job.getInputLibrary().getLibraryclass().getCategory();
				TaskResponse tasktypeResponse = taskExecutor.execute(taskName, inputLibraryName, file.getId(), logicalFile, libraryCategory, destinationDirPath);
				if(tasktypeResponse == null) // TODO : Handle this...
					return;
				boolean isCancelInitiated = tasktypeResponse.isCancelled();
				
				long proxyEndTime = System.currentTimeMillis();
				boolean isComplete = tasktypeResponse.isComplete();
				if(isComplete) {
					endms = System.currentTimeMillis();
					
					if(outputLibraryName != null) {
						Library outputLibrary = libraryDao.findByName(outputLibraryName); 
						if(outputLibrary == null) {// not already created.
						    outputLibrary = new Library();
						    outputLibrary.setLibraryRef(job.getInputLibrary()); //sourceLibraryId
						    outputLibrary.setFileCount(111);
						    outputLibrary.setFileStructureMd5("not needed");
						    // TODO : try avoiding this call...
						    Libraryclass outputLibraryclass = libraryclassDao.findByGeneratorTaskId(taskId); //  the task output's resultant libraryclass
						    //Libraryclass outputLibraryclass = libraryclassDao.findByTaskId(taskId); //  the task output's resultant libraryclass
						    outputLibrary.setLibraryclass(outputLibraryclass);
						    outputLibrary.setName(outputLibraryName);
						    
						    outputLibrary = libraryDao.save(outputLibrary);
			
						    // setting the current jobs output libraryid
							String logMsgPrefix = "DB Job - " + "(" + job.getId() + ") - Updation - OutputLibraryId " + outputLibrary.getId();
							logger.debug(logMsgPrefix);	
						    job.setOutputLibrary(outputLibrary);
						    jobDao.save(job);
						    logger.debug(logMsgPrefix + " - Success");	
						    
						    // Now setting all the dependentjobs with the tasktype generated output libraryid
						    List<Job> jobList = jobUtils.getDependentJobs(job);
					
							for (Iterator<Job> iterator = jobList.iterator(); iterator.hasNext();) {
								Job nthDependentJob = (Job) iterator.next();
	
								nthDependentJob.setInputLibrary(outputLibrary); // output library of the current job is the input library of the dependent job
								String logMsgPrefix2 = "DB Job - " + "(" + nthDependentJob.getId() + ") - Updation - InputLibraryId " + outputLibrary.getId();
								logger.debug(logMsgPrefix2);	
							    jobDao.save(nthDependentJob);
							    logger.debug(logMsgPrefix2 + " - Success");
							}	    		
						}	    

					    int outputLibraryId = outputLibrary.getId();
						    
				    
						org.ishafoundation.dwaraapi.db.model.transactional.File nthFileRowToBeInserted = new org.ishafoundation.dwaraapi.db.model.transactional.File();
						nthFileRowToBeInserted.setFileRef(file);
						nthFileRowToBeInserted.setLibrary(outputLibrary);
						//nthFileRowToBeInserted.setFiletypeId(filetypeId); // TODO How?
						
						nthFileRowToBeInserted.setPathname(destinationDirPath.replace(outputLibraryPathname, outputLibraryName) + File.separator + FilenameUtils.getName(logicalFile.getAbsolutePath()));
						
						//(destinationFilePath + FilenameUtils.getName(logicalFile.getAbsolutePath()));
						
						// TODO need to be done and set after proxy file is generated
						nthFileRowToBeInserted.setCrc("crc_for_proxy"); 
						nthFileRowToBeInserted.setSize(9999);// TODO Hardcoded...
				    	logger.debug("DB File Creation");   
						fileDao.save(nthFileRowToBeInserted);
						logger.debug("DB File Creation - Success");
					}
					
					if(task.getApplication() != null) {
						Application application = task.getApplication();
						ApplicationFile applicationFile = new ApplicationFile(application, file);
						applicationFile.setIdentifier(tasktypeResponse.getAppId());
						applicationFileDao.save(applicationFile);
					}
					tasktypeStatus = Status.completed;
					//logger.info("Proxy for " + containerName + " created successfully in " + ((proxyEndTime - proxyStartTime)/1000) + " seconds - " +  generatedProxyFilePathname);
					logger.info("Processing Completed in " + ((endms - startms)/1000) + " seconds");
				}else {
					if(isCancelInitiated){
						tasktypeStatus = Status.aborted;
						logger.info("Tasktype for " + containerName + " aborted");
					} 
					else
						throw new Exception("Unable to tasktype" + FilenameUtils.getBaseName(logicalFile.getAbsolutePath()) + " : because : " + tasktypeResponse.getFailureReason());
				}
			}
		} catch (Exception e) {
			tasktypeStatus = Status.failed;
			failureReason = "Unable to complete " + taskName + " tasktype for " + identifierSuffix + " :: " + e.getMessage();
			logger.error(failureReason);
			
			// TODO : Work on the threshold failures...
			// create failed_media_file_run table
			logger.debug("DB Failure Creation");
			Failure failure = new Failure();
			failure.setFile(file);
			failure.setJob(job);
			failure = failureDao.save(failure);	
			logger.debug("DB Failure Creation - " + failure.getId());   
		}
		finally {
			if(tFileJob != null) {
				tFileJob.setStatus(tasktypeStatus);
				logger.debug("DB TFileJob Updation - status to " + tasktypeStatus.toString());
				tFileJobDao.save(tFileJob);
				logger.debug("DB TFileJob Updation - Success");
			}
			threadNameHelper.resetThreadName();
		}
	}
	
	protected synchronized Job checkAndUpdateStatusToInProgress(Job job, Subrequest systemGeneratedRequest){
		if(job.getStatus() == Status.queued) {
			Status status = Status.in_progress;
			
			String logMsgPrefix = "DB Job - " + "(" + job.getId() + ") - Updation - status to " + status;
			logger.debug(logMsgPrefix);	
			job.setStatus(status);
			job.setStartedAt(LocalDateTime.now());
			jobDao.save(job);
			logger.debug(logMsgPrefix + " - Success");					


			if(systemGeneratedRequest.getStatus() != status) {
				String logMsg = "DB Subrequest - " + systemGeneratedRequest.getId() + " - Update - status to " + status;
				logger.debug(logMsg);
		        systemGeneratedRequest.setStatus(status);
		        systemGeneratedRequest = subrequestDao.save(systemGeneratedRequest);
		        logger.debug(logMsg + " - Success");
			}			
		}
		return job;
	}

}
