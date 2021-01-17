package org.ishafoundation.dwaraapi.hotfixes;

import java.io.File;
import java.io.FilenameFilter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactEntityUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileEntityUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Filetype;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.db.utils.JobUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.thread.ProcessingJobHelper;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class Fix_2_1_6_old extends ProcessingJobHelper {
	private static final Logger logger = LoggerFactory.getLogger(Fix_2_1_6_old.class);
	
	@Autowired
	private JobDao jobDao; 
	
	@Autowired
	private TFileDao tFileDao;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private JobUtil jobUtil;	

	@Autowired
	private ArtifactEntityUtil artifactEntityUtil;
	
	@Autowired
	private FileEntityUtil fileEntityUtil;
	
	private boolean dryRun = true;
	
	@PostMapping(value="/fix_2_1_06_old", produces = "application/json")
    public ResponseEntity<String> executeFix_2_1_06_Old(@RequestParam(defaultValue="true") boolean dryRun){
		
		String response = null;
		try {
			this.dryRun = dryRun;
			
			List<String> processingTaskList = new ArrayList<String>();
			processingTaskList.add("video-mkv-pfr-metadata-extract");
			processingTaskList.add("video-proxy-low-gen");
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); 
			LocalDateTime startDateTime = LocalDateTime.parse("2021-01-17 01:00", formatter);
			LocalDateTime endDateTime = LocalDateTime.parse("2021-01-17 07:15", formatter);
			
			List<Job> jobList = jobDao.findAllByStartedAtBetweenAndStatusAndProcessingtaskIdInOrderById(startDateTime, endDateTime, Status.failed, processingTaskList);

			Domain domain = Domain.ONE;
			for (Job nthJob : jobList) {
				logger.info("Now processing job " + nthJob.getId());
				int inputArtifactId = nthJob.getInputArtifactId();
				int outputArtifactId = nthJob.getOutputArtifactId();
				
				ArtifactRepository<Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);
				Artifact inputArtifact = artifactRepository.findById(inputArtifactId).get();
				Artifact outputArtifact = artifactRepository.findById(outputArtifactId).get(); 
				
				String inputArtifactName = inputArtifact.getName();
				String outputArtifactName = outputArtifact.getName();
				
				Artifactclass inputArtifactclass = inputArtifact.getArtifactclass();
				
				String inputArtifactPathname =  inputArtifactclass.getPath() + File.separator + inputArtifactName;
				
				logger.info("Now updating Output Artifact as tfile and file records");
				String artifactFileAbsolutePathName = outputArtifact.getArtifactclass().getPath() + File.separator + outputArtifactName;
				
				HashMap<String, TFile> inputFilePathToTFileObj = getFilePathToTFileObj(inputArtifactId);
				HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.domain.File> inputFilePathToFileObj = getFilePathToFileObj(domain, inputArtifact);
				
				HashMap<String, TFile> outputFilePathToTFileObj = getFilePathToTFileObj(outputArtifact.getId());
				HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.domain.File> outputFilePathToFileObj = getFilePathToFileObj(domain, outputArtifact);
				
				org.ishafoundation.dwaraapi.db.model.transactional.TFile artifactTFile = outputFilePathToTFileObj.get(outputArtifactName);
				
				if(artifactTFile == null) // only if not already created... 
					artifactTFile = createTFile(inputFilePathToTFileObj.get(inputArtifactName), artifactFileAbsolutePathName, outputArtifact);	
					
				FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
//				org.ishafoundation.dwaraapi.db.model.transactional.domain.File artifactFile = domainSpecificFileRepository.findByPathname(outputArtifactName);
				
				org.ishafoundation.dwaraapi.db.model.transactional.domain.File artifactFile = outputFilePathToFileObj.get(outputArtifactName);
				
				if(artifactFile == null) { // only if not already created... 
					artifactFile = createFile(inputFilePathToFileObj.get(inputArtifactName), artifactFileAbsolutePathName, outputArtifact, domainSpecificFileRepository, domain, artifactTFile);	
				}
				
				
				String destinationDirPath = artifactFileAbsolutePathName;
				Collection<LogicalFile> selectedFileList = getFilesToBeProcessed(nthJob.getProcessingtaskId(), inputArtifactPathname, inputArtifactclass);
				
				logger.info("Now looping the logical file set");
				for (LogicalFile logicalFile : selectedFileList) {
					
					final String srcFileBaseName = FilenameUtils.getBaseName(logicalFile.getAbsolutePath());
					logger.info("logical File - " + srcFileBaseName);
			        FilenameFilter fileNameFilter = new FilenameFilter() {
			            public boolean accept(File dir, String name) {
			            	if(FilenameUtils.getBaseName(name).equals(srcFileBaseName))
			            		return true;
			               
			               return false;
			            }
			         };
			         
					String[] processedFileNames = new File(destinationDirPath).list(fileNameFilter);
					logger.info("processedFileNames " + processedFileNames.toString());
					
					// creating File records for the process generated files
					for (String nthFileName : processedFileNames) {
						String filepathName = destinationDirPath.replace(outputArtifact.getArtifactclass().getPath() + File.separator, "") + File.separator + nthFileName;
						logger.trace("filepathName - " + filepathName);
	//					logger.info("filepathName using destinationDirPath " + filepathName);
	//					filepathName = proxyFilePath.replace(outputArtifact.getArtifactclass().getPath() + File.separator, "") + File.separator + nthFileName;
	//					logger.info("filepathName using proxyFilePath " + filepathName);
	
						org.ishafoundation.dwaraapi.db.model.transactional.TFile nthTFile = outputFilePathToTFileObj.get(filepathName);
						if(nthTFile == null) { // only if not already created... 
							logger.trace("Now creating T file record for - " + filepathName);
							nthTFile = createTFile(inputFilePathToTFileObj.get(filepathName), outputArtifact.getArtifactclass().getPath() + File.separator + filepathName, outputArtifact);	
						}							
						org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile = outputFilePathToFileObj.get(filepathName);
						//org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile = domainSpecificFileRepository.findByPathname(filepathName);
						if(nthFile == null) { // only if not already created... 
							logger.trace("Now creating file record for - " + filepathName);
							createFile(inputFilePathToFileObj.get(filepathName), outputArtifact.getArtifactclass().getPath() + File.separator + filepathName, outputArtifact, domainSpecificFileRepository, domain, nthTFile);	
						}
					}
				}
			}

		}catch (Exception e) {
			String errorMsg = "Unable to get data for ltowala - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		
		return ResponseEntity.status(HttpStatus.OK).body("Check logs");
	}
	
	private TFile createTFile(TFile tFileRef, String fileAbsolutePathName, Artifact outputArtifact) throws Exception {
		org.ishafoundation.dwaraapi.db.model.transactional.TFile nthTFileRowToBeInserted = new org.ishafoundation.dwaraapi.db.model.transactional.TFile();
		nthTFileRowToBeInserted.setArtifactId(outputArtifact.getId());
		nthTFileRowToBeInserted.setFileRefId(tFileRef.getId());
		
	    String filePathname = fileAbsolutePathName.replace(outputArtifact.getArtifactclass().getPath() + File.separator, "");
	    nthTFileRowToBeInserted.setPathname(filePathname);
	    
	    byte[] filePathChecksum = ChecksumUtil.getChecksum(filePathname);
	    nthTFileRowToBeInserted.setPathnameChecksum(filePathChecksum);
		
	    File file = new File(fileAbsolutePathName);
		if(file.isDirectory())
			nthTFileRowToBeInserted.setDirectory(true);
		
		if(file.exists()) {
			try {
				logger.trace("Calc size of " + filePathname);
				nthTFileRowToBeInserted.setSize(FileUtils.sizeOf(file));
			}catch (Exception e) {
				logger.warn("Weird. File exists but fileutils unable to calculate size. Skipping setting size");
			}
		}

	    logger.info("Pathname " +nthTFileRowToBeInserted.getPathname());
	    logger.info("Size " +nthTFileRowToBeInserted.getSize());
	    logger.info("tfileRef " + tFileRef.getId());
	    logger.info("outputArtifact " + outputArtifact.getId());
	    logger.info("PathnameChecksum " + Hex.encodeHexString(nthTFileRowToBeInserted.getPathnameChecksum()));
	    
	    //logger.info("Checksum " + Hex.encodeHexString(nthTFileRowToBeInserted.getChecksum()));
	    logger.info("Deleted " +nthTFileRowToBeInserted.isDeleted());
	    logger.info("Directory " +nthTFileRowToBeInserted.isDirectory());

	    
		if(!dryRun)
			tFileDao.save(nthTFileRowToBeInserted);
		return nthTFileRowToBeInserted;
	}

	private org.ishafoundation.dwaraapi.db.model.transactional.domain.File createFile(org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileRef, String fileAbsolutePathName, Artifact outputArtifact, FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository, Domain domain, org.ishafoundation.dwaraapi.db.model.transactional.TFile tFileDBObj) throws Exception {
	    org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFileRowToBeInserted = domainUtil.getDomainSpecificFileInstance(domain);
		
	    fileEntityUtil.setDomainSpecificFileRef(nthFileRowToBeInserted, fileRef);
	    
	    fileEntityUtil.setDomainSpecificFileArtifact(nthFileRowToBeInserted, outputArtifact);
	    
	    String filePathname = tFileDBObj.getPathname();
	    nthFileRowToBeInserted.setPathname(filePathname);
	    byte[] filePathChecksum = ChecksumUtil.getChecksum(filePathname);
	    nthFileRowToBeInserted.setPathnameChecksum(filePathChecksum);

	    nthFileRowToBeInserted.setDirectory(tFileDBObj.isDirectory());
	    nthFileRowToBeInserted.setSize(tFileDBObj.getSize());

	    org.ishafoundation.dwaraapi.db.model.transactional.domain.File savedFile = null;
	    

	    logger.info("Pathname " +nthFileRowToBeInserted.getPathname());
	    logger.info("Size " +nthFileRowToBeInserted.getSize());
	    logger.info("fileRef " + fileRef.getId());
	    logger.info("outputArtifact " + outputArtifact.getId());
	    logger.info("PathnameChecksum " + Hex.encodeHexString(nthFileRowToBeInserted.getPathnameChecksum()));
	    //logger.info("Checksum " + Hex.encodeHexString(nthFileRowToBeInserted.getChecksum()));
	    logger.info("Deleted " +nthFileRowToBeInserted.isDeleted());
	    logger.info("Directory " +nthFileRowToBeInserted.isDirectory());
	    
	    if(!dryRun) {
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
	    }
	    
		return savedFile;
	}

	private Collection<LogicalFile> getFilesToBeProcessed(String processingtaskId, String inputArtifactPath, Artifactclass inputArtifactclass){
		Processingtask processingtask = getProcessingtask(processingtaskId);
		
		Filetype ft = getInputFiletype(processingtask);
			
		return  getLogicalFileList(ft, inputArtifactPath, inputArtifactclass.getId(), processingtaskId);
	}
}
