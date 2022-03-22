package org.ishafoundation.dwaraapi.process.checksum;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.FileVolumeDao;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileVolume;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.Job;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.ishafoundation.dwaraapi.process.request.TFile;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("checksum-verify")
public class ChecksumVerifier implements IProcessingTask {

	private static final Logger logger = LoggerFactory.getLogger(ChecksumVerifier.class);
	
	public static final String CHECKSUM_VERIFIER_COMPONENT_NAME = "checksum-verify";
	
	@Autowired
	private FileVolumeDao fileVolumeDao;
	
	@Autowired
	private Configuration configuration;
	
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		
		LogicalFile logicalFile = processContext.getLogicalFile();
		org.ishafoundation.dwaraapi.process.request.File file = processContext.getFile();
		TFile tFile = processContext.getTFile() != null ? processContext.getTFile() : processContext.getFile();
		if(tFile == null) {
			String msg = "No tFile/File record for " + logicalFile.getAbsolutePath(); // Edited/Backup scenario will bomb if files that are not part of File table are to be verified outside ingest - say during rewrite or restore_verify - as tFile records are purged after finalisation
			logger.error(msg);
			throw new Exception(msg);
		}
			
		if(logicalFile.exists() && logicalFile.isFile()) {
			logger.info("Verifying checksum for - " + tFile.getId() + ":" + logicalFile.getAbsolutePath());
			byte[] originalChecksum = tFile.getChecksum();
			logger.trace("originalChecksum " + Hex.encodeHexString(originalChecksum));
//			String filePathname = file.getPathname();
//			String fileAbsolutePath = logicalFile.getAbsolutePath();
//			String filePathPrefix = StringUtils.substringBefore(fileAbsolutePath, filePathname);
//			fileAbsolutePath.replace(filePathPrefix, processContext.getInputDirPath());
			
			byte[] checksumToBeVerified = ChecksumUtil.getChecksum(logicalFile, Checksumtype.valueOf(configuration.getChecksumType()));
			logger.trace("checksumToBeVerified " + Hex.encodeHexString(checksumToBeVerified));
			
			if (Arrays.equals(originalChecksum, checksumToBeVerified)) {
				logger.trace("originalChecksum = checksumToBeVerified. All good");	
				String volumeId = null;
				List<Job> jobDependencies = processContext.getJob().getDependencies();
				for (Job nthJobDependency : jobDependencies) {
					if(nthJobDependency.getStoragetaskActionId() != null)
						volumeId = nthJobDependency.getVolume().getId();
				}
				 
				if(file != null) {
			    	FileVolume fileVolume = fileVolumeDao.findByIdFileIdAndIdVolumeId(file.getId(), volumeId);
			    	fileVolume.setVerifiedAt(LocalDateTime.now());
			    	fileVolumeDao.save(fileVolume);
		    	}
			}
			else {
				String msg = "Checksum mismatch for file " + tFile.getId();
				logger.error(msg);
				throw new Exception(msg);
			}
			
		}
		else {
			logger.info(tFile.getId() + " not a file but a folder. Skipping it");
		}
		processingtaskResponse.setIsComplete(true);
		return processingtaskResponse;
	}

}
