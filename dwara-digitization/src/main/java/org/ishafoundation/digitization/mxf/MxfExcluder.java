package org.ishafoundation.digitization.mxf;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileEntityUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.Artifact;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("mxf-exclusion")
public class MxfExcluder implements IProcessingTask {
    
    private static final Logger logger = LoggerFactory.getLogger(MxfExcluder.class);
	
//    @Autowired
//    private ApplicationContext appcont;
    
	@Autowired
	private Configuration config;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		Artifact inputArtifact = processContext.getJob().getInputArtifact();
		String inputArtifactName = inputArtifact.getName();
		
		String taskName = processContext.getJob().getProcessingtaskId();
		LogicalFile logicalFile = processContext.getLogicalFile();
		org.ishafoundation.dwaraapi.process.request.File file = processContext.getFile();
		String destinationDirPath = processContext.getOutputDestinationDirPath();

		// Mark the File deleted...
//		WebClient webClient = WebClient.create("http://localhost:9000");
//		logger.info(file.getId() + "");
////		WebClient webClient = WebClient.create("http://172.18.1.213:8080/api");
////		Mono<String> bodyMono = webClient.post().uri(URI.create("/file/" + file.getId() + "/delete")).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
////				bodyMono.subscribe
////				retrieve();
////				
//				
//				Mono<ClientResponse> clientResponse = webClient.post().uri(URI.create("/file/" + file.getId() + "/delete")).accept(MediaType.APPLICATION_JSON).exchange();
//				logger.info(clientResponse.toString());
//			    clientResponse.subscribe((response) -> {
//
//			        // here you can access headers and status code
//			        Headers headers = response.headers();
//			        HttpStatus stausCode = response.statusCode();
//			        logger.info("stausCode:" + stausCode);
//			        Mono<String> bodyToMono = response.bodyToMono(String.class);
//			        // the second subscribe to access the body
//			        bodyToMono.subscribe((body) -> {
//
//			            // here you can access the body
//			            logger.info("body:" + body);
//
//			            // and you can also access headers and status code if you need
//			            logger.info("headers:" + headers.asHttpHeaders());
//			            logger.info("stausCode:" + stausCode);
//
//			        }, (ex) -> {
//			            // handle error
//			        });
//			    }, (ex) -> {
//			        // handle network error
//			    });
		
		
		// TODO - Have to call this as API
    	FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(Domain.ONE);
    	org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileFromDB = domainSpecificFileRepository.findById(file.getId()).get();
    	fileFromDB.setDeleted(true);
    	domainSpecificFileRepository.save(fileFromDB);
		
    	// move the File to junk
    	String path = logicalFile.getAbsolutePath();
    	
    	String junkFilesStagedDirName = config.getJunkFilesStagedDirName();
    	String junkDirPrefixedFilePathname = inputArtifactName + File.separator + junkFilesStagedDirName; 

    	
    	String destPath = path.replace(inputArtifactName, junkDirPrefixedFilePathname);
    	Files.createDirectories(Paths.get(FilenameUtils.getFullPathNoEndSeparator(destPath)));
    	Files.move(Paths.get(path), Paths.get(destPath), StandardCopyOption.ATOMIC_MOVE);
    	
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setIsComplete(true);

		return processingtaskResponse;
	}
}
