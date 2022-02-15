package org.ishafoundation.dwaraapi.process.encryption;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.configuration.CryptoConfiguration;
import org.ishafoundation.dwaraapi.context.CryptoContext;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.ishafoundation.dwaraapi.utils.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("encrypted-gen")
public class Encryptor implements IProcessingTask {

	private static final Logger logger = LoggerFactory.getLogger(Encryptor.class);
	
	@Autowired
	private CryptoConfiguration cryptoConfiguration;

	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		LogicalFile logicalFile = processContext.getLogicalFile();
		String sourceFilePathname = logicalFile.getAbsolutePath();
		String destinationDirPath = processContext.getOutputDestinationDirPath();
		
		FileUtils.forceMkdir(new File(destinationDirPath));
		
		String fileName = FilenameUtils.getName(sourceFilePathname);
		String outputFilepathname = destinationDirPath + File.separator + fileName;
		
		if(logicalFile.isFile()) {
			CryptoContext cryptoContext = new CryptoContext();
			cryptoContext.setPassword(cryptoConfiguration.getPassword());
			cryptoContext.setIv(cryptoConfiguration.getIv());
			cryptoContext.setSalt(cryptoConfiguration.getSalt());
			cryptoContext.setBufferSize(cryptoConfiguration.getBufferSize());
			cryptoContext.setTransformation(cryptoConfiguration.getTransformation());
			cryptoContext.setSecretKeyFactoryAlgorithm(cryptoConfiguration.getSecretKeyFactoryAlgorithm());
			cryptoContext.setSecretKeyGeneratorAlgorithm(cryptoConfiguration.getSecretKeyGeneratorAlgorithm());
			
			cryptoContext.setInputFilepathname(sourceFilePathname);
			cryptoContext.setOutputFilepathname(outputFilepathname);

			CryptoUtil.encrypt(cryptoContext);
		}
		else {
			logger.info(sourceFilePathname + " not a file but a folder. Skipping it");
		}
		processingtaskResponse.setIsComplete(true);
		return processingtaskResponse;
	}

}
