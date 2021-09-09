package org.ishafoundation.dwaraapi.process.checksum;

import java.util.Optional;

import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
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
	
	//@Autowired
	//private DomainUtil domainUtil;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private FileRepository fileRepository;

	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		
		//Domain domain = Domain.valueOf(processContext.getJob().getInputArtifact().getArtifactclass().getDomain());
		LogicalFile logicalFile = processContext.getLogicalFile();
		org.ishafoundation.dwaraapi.process.request.TFile tFile = processContext.getTFile();
		org.ishafoundation.dwaraapi.db.model.transactional.request.File file = processContext.getFile();
		if(logicalFile.isFile()) {
			boolean generateChecksum = false;

			if(tFile.getChecksum() == null) { // If there is a checksum already dont overwrite it...
				generateChecksum = true;
			}

			if(file != null) {
				if(file.getChecksum() == null) {
					generateChecksum = true;
				}
				else {
					logger.info(file.getId() + " already has checksum. Not overwriting it");
				}
			}
			
			if(generateChecksum) {
				byte[] checksum = ChecksumUtil.getChecksum(logicalFile, Checksumtype.valueOf(configuration.getChecksumType()));

				TFile tFileDbObj = null;
				Optional<TFile> tFileOptional = tFileDao.findById(tFile.getId());
				if(tFileOptional.isPresent()) { // For later checksum generation scenario TFile records could have been deleted.
					tFileDbObj = tFileOptional.get();
					tFileDbObj.setChecksum(checksum);
					tFileDao.save(tFileDbObj);
				}
			
				if(file != null) {
					//FileRepository<File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
					org.ishafoundation.dwaraapi.db.model.transactional.File fileDBObj = fileRepository.findById(file.getId());
					if(fileDBObj!=null) { // Not all files are persisted anymore. For e.g., edited videos has only configured files...
						
						fileDBObj.setChecksum(checksum);
				    	fileRepository.save(fileDBObj);
					}
				}
			}
		}
		else {
			logger.info(tFile.getId() + " not a file but a folder. Skipping it");
		}
		processingtaskResponse.setIsComplete(true);
		return processingtaskResponse;
	}

}
