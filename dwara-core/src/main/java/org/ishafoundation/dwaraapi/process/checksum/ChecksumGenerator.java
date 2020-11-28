package org.ishafoundation.dwaraapi.process.checksum;

import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
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
			FileRepository<File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
			org.ishafoundation.dwaraapi.db.model.transactional.domain.File file = domainSpecificFileRepository.findById(fileId).get();
			if(file.getChecksum() == null) { // If there is a checksum already dont overwrite it...
				file.setChecksum(ChecksumUtil.getChecksum(logicalFile, Checksumtype.valueOf(configuration.getChecksumType())));
		    	domainSpecificFileRepository.save(file);
			}
			else {
				logger.info(fileId + " already has checksum. Not overwriting it");
			}
		}
		else {
			logger.info(fileId + " not a file but a folder. Skipping it");
		}
		processingtaskResponse.setIsComplete(true);
		return processingtaskResponse;
	}

}
