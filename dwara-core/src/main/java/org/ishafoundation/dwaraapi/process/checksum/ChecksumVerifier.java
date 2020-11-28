package org.ishafoundation.dwaraapi.process.checksum;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.File;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("checksum-verify")
public class ChecksumVerifier implements IProcessingTask {

	private static final Logger logger = LoggerFactory.getLogger(ChecksumVerifier.class);
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private Configuration configuration;
	
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		
		LogicalFile logicalFile = processContext.getLogicalFile();
		
		File file = processContext.getFile();
		if(logicalFile.isFile()) {
			byte[] originalChecksum = file.getChecksum();
//			String filePathname = file.getPathname();
//			String fileAbsolutePath = logicalFile.getAbsolutePath();
//			String filePathPrefix = StringUtils.substringBefore(fileAbsolutePath, filePathname);
//			fileAbsolutePath.replace(filePathPrefix, processContext.getInputDirPath());
			
			byte[] checksumToBeVerified = ChecksumUtil.getChecksum(logicalFile, Checksumtype.valueOf(configuration.getChecksumType()));
	
			if (Arrays.equals(originalChecksum, checksumToBeVerified)) {
				
				Domain domain = Domain.valueOf(processContext.getJob().getInputArtifact().getArtifactclass().getDomain());
				String volumeId = processContext.getJob().getDependencies().get(0).getVolume().getId();

		    	FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
		    	FileVolume fileVolume = domainSpecificFileVolumeRepository.findByIdFileIdAndIdVolumeId(file.getId(), volumeId);
		    	fileVolume.setVerifiedAt(LocalDateTime.now());
		    	
		    	domainSpecificFileVolumeRepository.save(fileVolume);
			}
			else {
				String msg = "Checksum mismatch for file " + file.getId();
				logger.error(msg);
				throw new Exception(msg);
			}
		}
		else {
			logger.info(file.getId() + " not a file but a folder. Skipping it");
		}
		processingtaskResponse.setIsComplete(true);
		return processingtaskResponse;
	}

}
