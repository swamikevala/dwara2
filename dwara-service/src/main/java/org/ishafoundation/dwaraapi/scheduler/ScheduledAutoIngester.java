package org.ishafoundation.dwaraapi.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.req.staged.ingest.IngestUserRequest;
import org.ishafoundation.dwaraapi.api.req.staged.ingest.StagedFile;
import org.ishafoundation.dwaraapi.api.resp.staged.ingest.IngestResponse;
import org.ishafoundation.dwaraapi.api.resp.staged.scan.ArtifactClassGroupedStagedFileDetails;
import org.ishafoundation.dwaraapi.api.resp.staged.scan.StagedFileDetails;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.service.StagedService;
import org.ishafoundation.dwaraapi.staged.scan.Error;
import org.ishafoundation.dwaraapi.staged.scan.Errortype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//@Component
public class ScheduledAutoIngester {
	
	private static final Logger logger = LoggerFactory.getLogger(ScheduledAutoIngester.class);

	@Autowired
	private StagedService stagedService;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@Scheduled(cron = "${scheduler.autoIngester.cronExpression}")
	public void autoIngest(){
		logger.info("***** Auto ingesting now *****");


		// space availability on server?
		// maintenance mode - 
		// parallel write mode
		
		// tape availability 
		
		// configured artifactclasses only - artifactclass.autoIngest - true
		// copy shouldnt happen in user scan directory but only to user workspace dir... - change copy tool
		
		// Each artifactclass as separate user request
		// limit no. of requests per schedule - maxArtifactCountPerSchedule
		// based on traffic and decide on ingest should happen or not - noOfJobsQueuedThreshold - 
		
		// fix ingest errors
		
		List<Artifactclass> artifactclassList = configurationTablesUtil.getAllArtifactclasses();
		for (Artifactclass nthArtifactclass : artifactclassList) {
			if(Boolean.FALSE.equals(nthArtifactclass.getAutoIngest()))
				continue;
			
			String artifactclassId = nthArtifactclass.getId();
			logger.trace("Now dealing with " + artifactclassId);
			try {
				List<StagedFileDetails> stagedFilesList = stagedService.getAllIngestableFiles(artifactclassId);
				
				List<StagedFile> stagedFiles = new ArrayList<StagedFile>(); 

				for (StagedFileDetails nthStagedFileDetails : stagedFilesList) {
					if(nthStagedFileDetails.getErrors().size() > 0)
						continue; // skipped the orange and red files...

					
					StagedFile sf = new StagedFile();
					sf.setArtifactclass(artifactclassId);
					sf.setName(nthStagedFileDetails.getName());
					sf.setPath(nthStagedFileDetails.getPath());
					stagedFiles.add(sf);
				}
				
				if(stagedFiles.size() > 0) {
					IngestUserRequest ingestUserRequest = new IngestUserRequest();
					ingestUserRequest.setStagedFiles(stagedFiles);
					
					stagedService.ingest(ingestUserRequest); // error scenario handling - ingest
				}
			}catch (Exception e) {
				logger.error("Unable to ingest " + artifactclassId);
			}
		}
//		try {
//			List<ArtifactClassGroupedStagedFileDetails> groupedStagedFilesList = stagedService.getAllIngestableFiles();
//			
//			for (ArtifactClassGroupedStagedFileDetails artifactClassGroupedStagedFileDetails : groupedStagedFilesList) {
//				List<StagedFileDetails> stagedFilesList = artifactClassGroupedStagedFileDetails.getArtifact();
//				for (StagedFileDetails stagedFileDetails : stagedFilesList) {
//					if(stagedFileDetails.getErrors().size() > 0)
//						continue; // skipped the orange and red files...
//					
//					
//
////					List<Error> errorList = stagedFileDetails.getErrors();
////					for (Error nthError : errorList) {
////						if(nthError.getType() == ErrorType.Error || nthError.getType() == ErrorType.Warning)
////					}
//					
//				}
//			}
//		} catch (Exception e) {
//			logger.error("Unable to unload idle tapes from drives", e);
//		}
	}
}