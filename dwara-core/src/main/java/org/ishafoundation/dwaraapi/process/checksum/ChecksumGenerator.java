package org.ishafoundation.dwaraapi.process.checksum;

import java.util.Optional;

import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("checksum-gen")
public class ChecksumGenerator implements IProcessingTask {

	private static final Logger logger = LoggerFactory.getLogger(ChecksumGenerator.class);
	
	@Autowired
	private TFileDao tFileDao;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private Configuration configuration;

	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		
		Domain domain = Domain.valueOf(processContext.getJob().getInputArtifact().getArtifactclass().getDomain());
		LogicalFile logicalFile = processContext.getLogicalFile();

		int fileId = processContext.getFile().getId();
		if(logicalFile.isFile()) {
			boolean generateChecksum = false;
			
			TFile tFile = null;
			Optional<TFile> tFileOptional = tFileDao.findById(fileId);
			if(tFileOptional.isPresent()) { // For later checksum generation scenario TFile records could have been deleted.
				tFile = tFileOptional.get();
				if(tFile.getChecksum() == null) { // If there is a checksum already dont overwrite it...
					generateChecksum = true;
				}
			}
			
			org.ishafoundation.dwaraapi.db.model.transactional.domain.File file = null;
			FileRepository<File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
			Optional<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileOptional = domainSpecificFileRepository.findById(fileId);
			if(fileOptional.isPresent()) { // Not all files are persisted anymore. For e.g., edited videos has only configured files...
				file = fileOptional.get();
				if(file.getChecksum() == null) { // If there is a checksum already dont overwrite it...
					generateChecksum = true;
				}
				else {
					logger.info(fileId + " already has checksum. Not overwriting it");
				}
			}
			
			if(generateChecksum) {
				byte[] checksum = ChecksumUtil.getChecksum(logicalFile, Checksumtype.valueOf(configuration.getChecksumType()));
			
				if(tFile != null) {
					tFile.setChecksum(checksum);
					tFileDao.save(tFile);
				}
				if(file != null) {
					file.setChecksum(checksum);
			    	domainSpecificFileRepository.save(file);
				}
			}
		}
		else {
			logger.info(fileId + " not a file but a folder. Skipping it");
		}
		processingtaskResponse.setIsComplete(true);
		return processingtaskResponse;
	}

}
