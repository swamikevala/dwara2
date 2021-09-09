package org.ishafoundation.dwaraapi.process.checksum;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.FileVolumeRepository;
//import org.ishafoundation.dwaraapi.db.model.transactional.Job;
//import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
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
	
	/*
	 * @Autowired private DomainUtil domainUtil;
	 */
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private FileVolumeRepository fileVolumeRepository;
	
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		
		LogicalFile logicalFile = processContext.getLogicalFile();
		org.ishafoundation.dwaraapi.db.model.transactional.request.File file = processContext.getFile();
		TFile tFile = processContext.getTFile();
		if(logicalFile.isFile()) {
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
				//Domain domain = Domain.valueOf(processContext.getJob().getInputArtifact().getArtifactclass().getDomain());
				String volumeId = null;
				List<Job> jobDependencies = processContext.getJob().getDependencies();
				for (Job nthJobDependency : jobDependencies) {
					if(nthJobDependency.getStoragetaskActionId() != null)
						volumeId = nthJobDependency.getVolume().getId();
				}
				 
				if(file != null) {
			    	//FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
			    	FileVolume fileVolume = fileVolumeRepository.findByIdFileIdAndIdVolumeId(file.getId(), volumeId);
			    	fileVolume.setVerifiedAt(LocalDateTime.now());
			    	fileVolumeRepository.save(fileVolume);
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
