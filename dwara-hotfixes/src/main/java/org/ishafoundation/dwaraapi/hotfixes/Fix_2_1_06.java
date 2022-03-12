package org.ishafoundation.dwaraapi.hotfixes;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TTFileJobDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TTFileJob;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.exception.DwaraException;
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
public class Fix_2_1_06 extends ProcessingJobHelper {
	private static final Logger logger = LoggerFactory.getLogger(Fix_2_1_06.class);
	
	@Autowired
	private JobDao jobDao; 
	
	@Autowired
	private TFileDao tFileDao;
	
	@Autowired
	private ArtifactDao artifactDao;
	
	@Autowired
	private TTFileJobDao tFileJobDao;
	
	@Autowired
	private FileDao fileDao;
	
	private boolean dryRun = true;
	TFile tFileRef = null;
	org.ishafoundation.dwaraapi.db.model.transactional.File fileRef = null;
	
	@PostMapping(value="/fix_2_1_06", produces = "application/json")
    public ResponseEntity<String> executeFix_2_1_06(@RequestParam(defaultValue="true") boolean dryRun){
		
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

			for (Job nthJob : jobList) {
				logger.info("Now processing job " + nthJob.getId());
				int inputArtifactId = nthJob.getInputArtifactId();
				int outputArtifactId = nthJob.getOutputArtifactId();
				
				
				Artifact inputArtifact = artifactDao.findById(inputArtifactId).get();
				Artifact outputArtifact = artifactDao.findById(outputArtifactId).get(); 
				
				String inputArtifactName = inputArtifact.getName();
				String outputArtifactName = outputArtifact.getName();
				
				Artifactclass inputArtifactclass = inputArtifact.getArtifactclass();
				
				String inputArtifactPathname =  inputArtifactclass.getPath() + File.separator + inputArtifactName;
				
				logger.info("Now updating Output Artifact as tfile and file records");
				String outputArtifactPathname = outputArtifact.getArtifactclass().getPath() + File.separator + outputArtifactName;
				
				HashMap<String, TFile> inputFilePathToTFileObj = getFilePathToTFileObj(inputArtifactId);
				HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.File> inputFilePathToFileObj = getFilePathToFileObj(inputArtifact);
				
				HashMap<String, TFile> outputFilePathToTFileObj = getFilePathToTFileObj(outputArtifact.getId());
				HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.File> outputFilePathToFileObj = getFilePathToFileObj(outputArtifact);
				
				org.ishafoundation.dwaraapi.db.model.transactional.TFile artifactTFile = outputFilePathToTFileObj.get(outputArtifactName);
				
				String[] extns = {"mkv"};
				Collection<File> mkvFileList = FileUtils.listFiles(new File(inputArtifactPathname), extns, false);
				File mkvFile = null;
				for (File nthFile : mkvFileList) {
					mkvFile = nthFile;
				}
				tFileRef = inputFilePathToTFileObj.get(inputArtifactName + File.separator + mkvFile.getName());
				if(artifactTFile == null) // only if not already created... 
					artifactTFile = createTFile(outputArtifactPathname, outputArtifact);	
					
				
//				org.ishafoundation.dwaraapi.db.model.transactional.File artifactFile = domainSpecificFileRepository.findByPathname(outputArtifactName);
				
				org.ishafoundation.dwaraapi.db.model.transactional.File artifactFile = outputFilePathToFileObj.get(outputArtifactName);
				
				fileRef = inputFilePathToFileObj.get(inputArtifactName + File.separator + mkvFile.getName());
				if(artifactFile == null) { // only if not already created... 
					artifactFile = createFile(outputArtifactPathname, outputArtifact, artifactTFile);	
				}
				
				
		        Collection<File> fileList = FileUtils.listFiles(new File(outputArtifactPathname), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
				for (File nthProcessedFile : fileList) {
					String fileAbsolutePathName = nthProcessedFile.getAbsolutePath();
					String filepathName = fileAbsolutePathName.replace(outputArtifact.getArtifactclass().getPath() + File.separator, "");
					logger.trace("filepathName - " + filepathName);
//					logger.info("filepathName using destinationDirPath " + filepathName);
//					filepathName = proxyFilePath.replace(outputArtifact.getArtifactclass().getPath() + File.separator, "") + File.separator + nthFileName;
//					logger.info("filepathName using proxyFilePath " + filepathName);

					org.ishafoundation.dwaraapi.db.model.transactional.TFile nthTFile = outputFilePathToTFileObj.get(filepathName);
					if(nthTFile == null) { // only if not already created... 
						logger.trace("Now creating T file record for - " + filepathName);
						nthTFile = createTFile(fileAbsolutePathName, outputArtifact);	
					}							
					org.ishafoundation.dwaraapi.db.model.transactional.File nthFile = outputFilePathToFileObj.get(filepathName);
					//org.ishafoundation.dwaraapi.db.model.transactional.File nthFile = domainSpecificFileRepository.findByPathname(filepathName);
					if(nthFile == null) { // only if not already created... 
						logger.trace("Now creating file record for - " + filepathName);
						createFile(fileAbsolutePathName, outputArtifact, nthTFile);	
					}
				}
				
				if(!dryRun) {
//					logger.info("Now marking all the t_t_filejob entries as completed for tfileid " + tFileRef.getId());
//					List<TTFileJob> ttFileJobList = tFileJobDao.findAllByIdFileId(tFileRef.getId());
//					for (TTFileJob nthTTFileJob : ttFileJobList) {
//						nthTTFileJob.setStatus(Status.completed);
//					}
//					tFileJobDao.saveAll(ttFileJobList);
					
					logger.info("Now marking all the t_t_filejob entries for job " + nthJob.getId() + " as completed");
					List<TTFileJob> ttFileJobList = tFileJobDao.findAllByJobId(nthJob.getId());
					for (TTFileJob nthTTFileJob : ttFileJobList) {
						nthTTFileJob.setStatus(Status.completed);
					}
					tFileJobDao.saveAll(ttFileJobList);
					
					// now marking the job status as in_progress
					logger.info("Now marking job " + nthJob.getId() + " as in_progress");
					nthJob.setStatus(Status.in_progress);
					jobDao.save(nthJob);
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
	
	private TFile createTFile(String fileAbsolutePathName, Artifact outputArtifact) throws Exception {
		org.ishafoundation.dwaraapi.db.model.transactional.TFile nthTFileRowToBeInserted = new org.ishafoundation.dwaraapi.db.model.transactional.TFile();
		nthTFileRowToBeInserted.setArtifactId(outputArtifact.getId());
		nthTFileRowToBeInserted.setFileRefId(tFileRef.getId());
		
	    String filePathname = fileAbsolutePathName.replace(outputArtifact.getArtifactclass().getPath() + File.separator, "");
	    nthTFileRowToBeInserted.setPathname(filePathname);
	    
	    byte[] filePathChecksum = ChecksumUtil.getFilePathnameMD5Checksum(filePathname);
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

		logger.info("TFile Pathname " + nthTFileRowToBeInserted.getPathname() + " Size " + nthTFileRowToBeInserted.getSize() + " tfileRef " + tFileRef.getId() + " outputArtifact " + outputArtifact.getId() 
	    + " PathnameChecksum " + Hex.encodeHexString(nthTFileRowToBeInserted.getPathnameChecksum()) + " Deleted " +nthTFileRowToBeInserted.isDeleted() + " Directory " +nthTFileRowToBeInserted.isDirectory());
	    
		if(!dryRun)
			tFileDao.save(nthTFileRowToBeInserted);
		return nthTFileRowToBeInserted;
	}

	private org.ishafoundation.dwaraapi.db.model.transactional.File createFile(String fileAbsolutePathName, Artifact outputArtifact, org.ishafoundation.dwaraapi.db.model.transactional.TFile tFileDBObj) throws Exception {
	    org.ishafoundation.dwaraapi.db.model.transactional.File nthFileRowToBeInserted = new org.ishafoundation.dwaraapi.db.model.transactional.File();
	    nthFileRowToBeInserted.setFileRef(fileRef);
	    nthFileRowToBeInserted.setArtifact(outputArtifact);
	    
	    String filePathname = tFileDBObj.getPathname();
	    nthFileRowToBeInserted.setPathname(filePathname);
	    byte[] filePathChecksum = ChecksumUtil.getFilePathnameMD5Checksum(filePathname);
	    nthFileRowToBeInserted.setPathnameChecksum(filePathChecksum);

	    nthFileRowToBeInserted.setDirectory(tFileDBObj.isDirectory());
	    nthFileRowToBeInserted.setSize(tFileDBObj.getSize());

	    org.ishafoundation.dwaraapi.db.model.transactional.File savedFile = null;
	    

	    logger.info("File Pathname " + nthFileRowToBeInserted.getPathname() + " Size " + nthFileRowToBeInserted.getSize() + " fileRef " + fileRef.getId() + " outputArtifact " + outputArtifact.getId() 
	    + " PathnameChecksum " + Hex.encodeHexString(nthFileRowToBeInserted.getPathnameChecksum()) + " Deleted " +nthFileRowToBeInserted.isDeleted() + " Directory " +nthFileRowToBeInserted.isDirectory());
	    
	    if(!dryRun) {
			logger.debug("DB File Creation");
			try {
				savedFile = fileDao.save(nthFileRowToBeInserted);
			}
			catch (Exception e) {
				nthFileRowToBeInserted = fileDao.findByPathname(filePathname);
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
}
