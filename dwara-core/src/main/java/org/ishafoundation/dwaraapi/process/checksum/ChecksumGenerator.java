package org.ishafoundation.dwaraapi.process.checksum;

import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("checksum-generation")
public class ChecksumGenerator implements IProcessingTask {

	@Autowired
	private DomainUtil domainUtil;

	@Override
	public ProcessingtaskResponse execute(String taskName, String libraryName, File file, Domain domain, LogicalFile logicalFile,
			String category, String destinationDirPath) throws Exception {
		
		if(logicalFile.isFile())
			file.setChecksum(ChecksumUtil.getChecksum(logicalFile, Checksumtype.sha256));// TODO : ??? - From where do we get the checksumtype???

    	FileRepository<File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
    	domainSpecificFileRepository.save(file);

		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setIsComplete(true);

		return processingtaskResponse;
	}

}
