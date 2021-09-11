package org.ishafoundation.dwaraapi.process.thread;

import java.io.File;
import java.io.FilenameFilter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.ApplicationStatus;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactEntityUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ProcessingFailureDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TTFileJobDao;
import org.ishafoundation.dwaraapi.db.keys.TTFileJobKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.ArtifactclassConfig;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Filetype;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ExtensionFiletype;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.ProcessingFailure;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TTFileJob;
import org.ishafoundation.dwaraapi.db.utils.JobUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.helpers.ThreadNameHelper;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.ishafoundation.dwaraapi.staged.scan.StagedFileEvaluator;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
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
public class ProcessingJobProcessor extends ProcessingJobHelper implements Runnable{


	private static final Logger logger = LoggerFactory.getLogger(ProcessingJobProcessor.class);
		
	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private JobDao jobDao;	
	
	@Autowired
	private ProcessingFailureDao failureDao;
	
	@Autowired
	private TFileDao tFileDao;
	
	@Autowired
	private TTFileJobDao tFileJobDao;
	
	@Autowired
    private StagedFileEvaluator stagedFileEvaluator;
	
	@Autowired
	private Map<String, IProcessingTask> processingtaskActionMap;

	@Autowired
	private JobUtil jobUtil;	
	
	@Autowired
	private ArtifactRepository artifactRepository;
	
	@Autowired
	private FileRepository fileRepository;


	@Autowired
	private ArtifactEntityUtil artifactEntityUtil;
	
	
	@Autowired
	private Configuration configuration;
	
	private ProcessContext processContext;

	// All below are available in processcontext but to avoid DB calls per file passing it from Manager...
	private Job job;
	private Artifact inputArtifact;
	private org.ishafoundation.dwaraapi.db.model.transactional.File file;
	private TFile tFile;
	
	private Artifactclass outputArtifactclass;

	

	public ProcessContext getProcessContext() {
		return processContext;
	}

	public void setProcessContext(ProcessContext processContext) {
		this.processContext = processContext;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public Artifact getInputArtifact() {
		return inputArtifact;
	}

	public void setInputArtifact(Artifact inputArtifact) {
		this.inputArtifact = inputArtifact;
	}

	public org.ishafoundation.dwaraapi.db.model.transactional.File getFile() {
		return file;
	}

	public void setFile(org.ishafoundation.dwaraapi.db.model.transactional.File file) {
		this.file = file;
	}

	public TFile getTFile() {
		return tFile;
	}

	public void setTFile(TFile tFile) {
		this.tFile = tFile;
	}

	public Artifactclass getOutputArtifactclass() {
		return outputArtifactclass;
	}

	public void setOutputArtifactclass(Artifactclass outputArtifactclass) {
		this.outputArtifactclass = outputArtifactclass;
	}

	@Override
	public void run(){
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
		//Domain domain = Domain.valueOf(processContext.getJob().getInputArtifact().getArtifactclass().getDomain());
		LogicalFile logicalFile = processContext.getLogicalFile();
		String outputArtifactName = processContext.getJob().getOutputArtifact().getName();
		
		ThreadNameHelper threadNameHelper = new ThreadNameHelper();
		threadNameHelper.setThreadName(job.getRequest().getId(), job.getId(), tFile.getId());
		logger.debug("Will be processing - " + logicalFile.getAbsolutePath());
		Status status = Status.in_progress;
		String failureReason = null;
		long startms = 0;
		long endms = 0;


		// For every file tasktype call we had to make a DB call just to ensure the job is not cancelled...
		// Its pretty expensive to make a DB call for every tasktype to be run, but its worth to cancel a job than run an expensive tasktype say like transcoding
		TTFileJob tFileJob = null;
		String processingtaskName = null;
		Processingtask processingtask = null;
		try {
			logger.trace("fileId - " + tFile.getId());
			// if the current status of the job is queued - update it to inprogress and do so with the artifact status and request status...
			Request systemGeneratedRequest = job.getRequest();
			//Request request = systemGeneratedRequest.getRequest();
			job = jobDao.findById(job.getId()).get();
			if(job.getStatus() == Status.on_hold || job.getStatus() == Status.cancelled) {
				logger.warn("Job " + job.getStatus() + " - not processing it now");
				return;
			}
			if(job.getStatus() == Status.queued && ApplicationStatus.valueOf(configuration.getAppMode()) == ApplicationStatus.maintenance) {
				logger.warn("App in maintenance - Job " + job.getId() + " is in " + job.getStatus() + " - so not processing it now");
				return;
			}
			job = checkAndUpdateStatusToInProgress(job, systemGeneratedRequest); // synchronous method so only one thread can access this at a time
			
			// If the job is QUEUED or IN_PROGRESS and cancellation is initiated ...
			startms = System.currentTimeMillis();

			processingtaskName = job.getProcessingtaskId();
			logger.trace(job.getId() + " " + processingtaskName);
			
			processingtask = getProcessingtask(processingtaskName);
			
			tFileJob = tFileJobDao.findById(new TTFileJobKey(tFile.getId(), job.getId())).get();
			tFileJob.setStatus(Status.in_progress);
			tFileJob.setStartedAt(LocalDateTime.now());
			logger.debug("DB TFileJob Updation for file " + tFile.getId());
			tFileJobDao.save(tFileJob);
			logger.debug("DB TFileJob Updation - Success");
			
			ProcessingtaskResponse processingtaskResponse = processingtaskActionMap.get(processingtaskName).execute(processContext);
			
			if(processingtaskResponse == null) // TODO : Handle this...
				return;
			boolean isCancelInitiated = processingtaskResponse.isCancelled();
			
			long proxyEndTime = System.currentTimeMillis();
			boolean isComplete = processingtaskResponse.isComplete();
			if(isComplete) {
				logger.info("Processing complete - " + logicalFile.getAbsolutePath());
				endms = System.currentTimeMillis();

				// UPDATE ARTIFACT and FILE tables
				//synchronized (processingtaskResponse) { // A Synchronized block to ensure only one thread at a time updates... Handling it differently with extra checks..
					if(outputArtifactName != null && systemGeneratedRequest.getActionId() == Action.ingest) {
						
						
						String destinationDirPath = processContext.getOutputDestinationDirPath();
						// Dont be tempted to call the logicalfilehelper as it will return only if files are available
//						 Collection<LogicalFile> processedFileList = getLogicalFileList(outputFiletype, destinationDirPath, null, null);
//						int processedFilesCount = processedFileList.size();
//						if(processedFilesCount != 1)
//							throw new DwaraException("Something wrong. Expected just one output logical file " + outputFiletypeId + ", but found " + processedFilesCount);

						// get all the processed files and check if all the filetype extensions are returned...
						//String processedFilePathName = processingtaskResponse.getDestinationPathname(); 
						
						
						String srcFileBaseName = FilenameUtils.getBaseName(logicalFile.getAbsolutePath());
						ArrayList<String> processedFileNames = new ArrayList<String>();
						if(processingtask != null && processingtask.getOutputFiletypeId() != null) {
							Filetype filetype = getFiletype(processingtask.getOutputFiletypeId());
							List<ExtensionFiletype> extn_Filetype_List = filetype.getExtensions(); //extensionFiletypeDao.findAllByFiletypeId(filetype.getId());
							for (ExtensionFiletype extensionFiletype : extn_Filetype_List) {
								String suffix = extensionFiletype.getSuffix();
								String fileName = srcFileBaseName;
								if(suffix != null)
									fileName = srcFileBaseName + suffix;
								fileName = fileName + "." + extensionFiletype.getExtension().getId().toLowerCase();
								
								File nthProcessedFile = new File(destinationDirPath + File.separator + fileName);
								if(nthProcessedFile.exists()) {
									processedFileNames.add(fileName);
								}
							}	
						}else { // processingtask == null (core PTs like checksum-gen/verify) && processingtask.getOutputFiletypeId() == null (user PTs like mam-update)
					        FilenameFilter fileNameFilter = new FilenameFilter() {
					            @Override
					            public boolean accept(File dir, String name) {
					            	if(FilenameUtils.getBaseName(name).equals(srcFileBaseName))
					            		return true;
					               
					               return false;
					            }
					         };
							String[] processedFileNamesArray = new File(destinationDirPath).list(fileNameFilter);
							processedFileNames.addAll(Arrays.asList(processedFileNamesArray));
						}

						//Validating if all files needed are generated
						String outputFiletypeId = processingtask != null ? processingtask.getOutputFiletypeId() : null;
						// TODO we will be using same artifactclass from 2 different processing tasks and so not all extns for the filetypes are available. so commenting this to out until we design it well 
						boolean isCommentedOut = true;
						if(!isCommentedOut) {
						// if(outputFiletypeId != null) { // commenting out this validation to accomodate the last minute idx file extraction change
							Filetype filetype = getFiletype(outputFiletypeId);
							List<ExtensionFiletype> extn_Filetype_List = filetype.getExtensions(); //extensionFiletypeDao.findAllByFiletypeId(filetype.getId());
							for (ExtensionFiletype extensionFiletype : extn_Filetype_List) {
								String extensionName = extensionFiletype.getExtension().getId();
								boolean isExtensionFileAvailable = false;
								for (String nthProcessedFileName : processedFileNames) {
									String processedFileExtn = FilenameUtils.getExtension(nthProcessedFileName);
									if(extensionName.equals(processedFileExtn)) {
										isExtensionFileAvailable = true;
										break;
									}
								}
								if(!isExtensionFileAvailable) {
									throw new DwaraException("Missing expected " + srcFileBaseName + "." + extensionName + " to be generated as output by the processing task");
								}
							}
						}					

						
						//ArtifactRepository<Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);
						Artifact outputArtifact = artifactRepository.findByName(outputArtifactName); 
						if(outputArtifact == null) {// not already created.
						   // outputArtifact = domainUtil.getDomainSpecificArtifactInstance(domain);
							outputArtifact = new Artifact();
							
						   // artifactEntityUtil.setDomainSpecificArtifactRef(outputArtifact, inputArtifact);
							outputArtifact.setArtifactRef(inputArtifact);
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
						}
						
						if(job.getOutputArtifactId() == null) { // if not already updated
							Integer outputArtifactId = null;
							
							if(outputArtifactName.equals(inputArtifact.getName())){
								outputArtifactId = inputArtifact.getId();
							}
							else {
								outputArtifactId = outputArtifact.getId();
							}
						    // setting the current jobs output artifactid
							String logMsgPrefix = "DB Job - " + "(" + job.getId() + ") - Updation - OutputArtifactId " + outputArtifactId;
							logger.debug(logMsgPrefix);	
						    job.setOutputArtifactId(outputArtifactId);
						    jobDao.save(job);
						    logger.debug(logMsgPrefix + " - Success");	
							
						}
						
						// Output Artifact as a file record
						String artifactFileAbsolutePathName = outputArtifact.getArtifactclass().getPath() + File.separator + outputArtifactName;
						HashMap<String, TFile> filePathToTFileObj = getFilePathToTFileObj(outputArtifact.getId());
						HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.File> filePathToFileObj = getFilePathToFileObj( outputArtifact);
						
						org.ishafoundation.dwaraapi.db.model.transactional.TFile artifactTFile = filePathToTFileObj.get(outputArtifactName);
						
						if(artifactTFile == null) // only if not already created... 
							artifactTFile = createTFile(artifactFileAbsolutePathName, outputArtifact);	
							
						//FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
//						org.ishafoundation.dwaraapi.db.model.transactional.domain.File artifactFile = domainSpecificFileRepository.findByPathname(outputArtifactName);
						
						org.ishafoundation.dwaraapi.db.model.transactional.File artifactFile = filePathToFileObj.get(outputArtifactName);
						
						if(artifactFile == null) { // only if not already created... 
							artifactFile = createFile(artifactFileAbsolutePathName, outputArtifact, fileRepository,  artifactTFile);	
//						}else {
//							// if artifactFile already exists - need to recalculate the size for i/p = o/p artifact class scenarios which will change the dynamics of the folder completely
//							if(outputArtifactName.equals(inputArtifact.getName())){
//								File file = new File(artifactFileAbsolutePathName);
//	
//								if(file.exists()) {
//									try {
//										artifactFile.setSize(FileUtils.sizeOf(file));
//									}catch (Exception e) {
//										logger.warn("Weird. File exists but fileutils unable to calculate size. Skipping setting size");
//									}
//								}
//								artifactFile = domainSpecificFileRepository.save(artifactFile);
//							}
						}
						
//						String proxyFilePathName = processingtaskResponse.getDestinationPathname(); 
//						String proxyFilePath = FilenameUtils.getFullPathNoEndSeparator(proxyFilePathName);
//						logger.info("destinationDirPath " + destinationDirPath);
//						logger.info("proxyFilePath" +  proxyFilePath);
						// creating File records for the process generated files
						for (String nthFileName : processedFileNames) {
							String filepathName = destinationDirPath.replace(outputArtifact.getArtifactclass().getPath() + File.separator, "") + File.separator + nthFileName;
//							logger.info("filepathName using destinationDirPath " + filepathName);
//							filepathName = proxyFilePath.replace(outputArtifact.getArtifactclass().getPath() + File.separator, "") + File.separator + nthFileName;
//							logger.info("filepathName using proxyFilePath " + filepathName);

							org.ishafoundation.dwaraapi.db.model.transactional.TFile nthTFile = filePathToTFileObj.get(filepathName);
							if(nthTFile == null) { // only if not already created... 
								logger.trace("Now creating T file record for - " + filepathName);
								nthTFile = createTFile(outputArtifact.getArtifactclass().getPath() + File.separator + filepathName, outputArtifact);	
							}							
							org.ishafoundation.dwaraapi.db.model.transactional.File nthFile = filePathToFileObj.get(filepathName);
							//org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile = domainSpecificFileRepository.findByPathname(filepathName);
							if(nthFile == null) { // only if not already created... 
								String fullFilepathname =  outputArtifact.getArtifactclass().getPath() + File.separator + filepathName;
								boolean addFileRecords = true;
								
								ArtifactclassConfig artifactclassConfig = outputArtifact.getArtifactclass().getConfig();
								if(artifactclassConfig != null) {
									String pathnameRegex = artifactclassConfig.getPathnameRegex();
									if(!fullFilepathname.matches(pathnameRegex)) {
										logger.trace("Doesnt match " + pathnameRegex + " regex for " + fullFilepathname);
										addFileRecords = false;
									}
								}
								if(addFileRecords) {
									logger.trace("Now creating file record for - " + filepathName);
									createFile(fullFilepathname, outputArtifact, fileRepository,  nthTFile);
								}
							}
						}
						
//						File file = new File(artifactFileAbsolutePathName);
//						if(file.exists()) {
//							try {
//								outputArtifact.setTotalSize(artifactFile.getSize());
//								int artifactFileCnt = FileUtils.listFiles(new File(artifactFileAbsolutePathName), FileFilterUtils.nameFileFilter(srcFileBaseName, null), TrueFileFilter.INSTANCE).size();
//								//outputArtifact.setFileCount(processedFileNames.length);
//								logger.trace("Hereee" + processingtaskName + ":" + artifactFileAbsolutePathName + ":" + fileNameFilter + ":" + artifactFileCnt + ":" +  processedFileNames.length);
//								outputArtifact.setFileCount(artifactFileCnt);
//							    outputArtifact = (Artifact) artifactRepository.save(outputArtifact);
//								
//							}catch (Exception e) {
//								logger.warn("Weird. File exists but fileutils unable to calculate size. Skipping setting size");
//							}
//						}
					}
				//}
				status = Status.completed;
				//logger.info("Proxy for " + containerName + " created successfully in " + ((proxyEndTime - proxyStartTime)/1000) + " seconds - " +  generatedProxyFilePathname);
				logger.debug("Processing Completed in " + ((endms - startms)/1000) + " seconds");
			}else {
				status = Status.failed;
				throw new Exception(processingtaskResponse.getFailureReason());
			}
		} catch (Exception e) {
			status = Status.failed;
			failureReason = "Unable to complete " + processingtaskName + " for " + tFile.getId() + " :: " + e.getMessage();
			logger.error(failureReason, e);
			
			int maxErrorsAllowed = processingtask != null ? processingtask.getMaxErrors() : 1;
			long noOfFailuresLogged = failureDao.countByJobId(job.getId());

			// Only the threshold no. of failures on a job need to be persisted in DB...
			if(noOfFailuresLogged < maxErrorsAllowed) {
				// TODO how to ensure the failures logged in are Only unique???
				logger.debug("DB Failure Creation");
				ProcessingFailure failure = new ProcessingFailure(tFile.getId(), job, e.getMessage());
				failure = failureDao.save(failure);	
				logger.debug("DB Failure Creation - " + failure.getId());
			}
		}
		finally {
			if(tFileJob != null) {
				tFileJob.setStatus(status);
				logger.debug("DB TFileJob Updation - status to " + status.toString());
				tFileJobDao.save(tFileJob);
				logger.debug("DB TFileJob Updation - Success");
			}
			threadNameHelper.resetThreadName();
		}
	}

	private TFile createTFile(String fileAbsolutePathName, Artifact outputArtifact) throws Exception {
		org.ishafoundation.dwaraapi.db.model.transactional.TFile nthTFileRowToBeInserted = new org.ishafoundation.dwaraapi.db.model.transactional.TFile();
		nthTFileRowToBeInserted.setArtifactId(outputArtifact.getId());
		nthTFileRowToBeInserted.setFileRefId(tFile.getId());
		
	    String filePathname = fileAbsolutePathName.replace(outputArtifact.getArtifactclass().getPath() + File.separator, "");
	    nthTFileRowToBeInserted.setPathname(filePathname);
	    
	    byte[] filePathChecksum = ChecksumUtil.getChecksum(filePathname);
	    nthTFileRowToBeInserted.setPathnameChecksum(filePathChecksum);
		
		// TODO need to be done and set after proxy file is generated
		//nthFileRowToBeInserted.setChecksum(ChecksumUtil.getChecksum(new File(processingtaskResponse.getDestinationPathname()), Checksumtype.sha256)); 
		File file = new File(fileAbsolutePathName);
		if(file.isDirectory())
			nthTFileRowToBeInserted.setDirectory(true);
		
		if(file.exists()) {
			org.ishafoundation.dwaraapi.staged.scan.ArtifactFileDetails afd = stagedFileEvaluator.getDetails(file);
			nthTFileRowToBeInserted.setSize(afd.getTotalSize());
		}
		
		org.ishafoundation.dwaraapi.db.model.transactional.TFile savedTFile = null;
		logger.debug("DB TFile Creation");
		try {
			savedTFile = tFileDao.save(nthTFileRowToBeInserted);
		}
		catch (Exception e) {
			nthTFileRowToBeInserted = tFileDao.findByPathname(filePathname);
			// This check is because of the same file getting queued up for processing again...
			// JobManager --> get all "Queued" processingjobs --> ProcessingJobManager ==== thread per file ====> ProcessingJobProcessor --> Only when the file's turn comes the status change to inprogress
			// Next iteration --> get all "Queued" processingjobs would still show the same job above sent already to ProcessingJobManager as it has to wait for its turn for CPU cycle... 
			if(nthTFileRowToBeInserted != null) {
				logger.trace("TFile details possibly updated by another thread already");
				return nthTFileRowToBeInserted;
			}
			else
				throw e;
		}
		logger.debug("DB TFile Creation - Success");
		return savedTFile;
	}

	private org.ishafoundation.dwaraapi.db.model.transactional.File createFile(String fileAbsolutePathName, Artifact outputArtifact, FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.File> domainSpecificFileRepository, org.ishafoundation.dwaraapi.db.model.transactional.TFile tFileDBObj) throws Exception {
	    //org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFileRowToBeInserted = domainUtil.getDomainSpecificFileInstance(domain);
		org.ishafoundation.dwaraapi.db.model.transactional.File nthFileRowToBeInserted = new org.ishafoundation.dwaraapi.db.model.transactional.File();
	    //fileEntityUtil.setDomainSpecificFileRef(nthFileRowToBeInserted, file);
	    nthFileRowToBeInserted.setFileRef(file); 
	    //fileEntityUtil.setDomainSpecificFileArtifact(nthFileRowToBeInserted, outputArtifact);
	    nthFileRowToBeInserted.setArtifact(outputArtifact);
	    String filePathname = tFileDBObj.getPathname();
	    nthFileRowToBeInserted.setPathname(filePathname);
	    byte[] filePathChecksum = ChecksumUtil.getChecksum(filePathname);
	    nthFileRowToBeInserted.setPathnameChecksum(filePathChecksum);

	    nthFileRowToBeInserted.setDirectory(tFileDBObj.isDirectory());
	    nthFileRowToBeInserted.setSize(tFileDBObj.getSize());

	    org.ishafoundation.dwaraapi.db.model.transactional.File savedFile = null;
		logger.debug("DB File Creation");
		try {
			savedFile = domainSpecificFileRepository.save(nthFileRowToBeInserted);
		}
		catch (Exception e) {
			nthFileRowToBeInserted = domainSpecificFileRepository.findByPathname(filePathname);
			// This check is because of the same file getting queued up for processing again...
			// JobManager --> get all "Queued" processingjobs --> ProcessingJobManager ==== thread per file ====> ProcessingJobProcessor --> Only when the file's turn comes the status change to inprogress
			// Next iteration --> get all "Queued" processingjobs would still show the same job above sent already to ProcessingJobManager as it has to wait for its turn for CPU cycle... 
			if(nthFileRowToBeInserted != null) {
				logger.trace("File details possibly updated by another thread already");
				return nthFileRowToBeInserted;
			}
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
