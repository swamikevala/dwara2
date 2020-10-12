package org.ishafoundation.dwaraapi.scheduler;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledIngestedArtifactAutoDeleter {
	
	private static final Logger logger = LoggerFactory.getLogger(ScheduledIngestedArtifactAutoDeleter.class);

	@Autowired
	private Environment env;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private Configuration configuration;
	
	// once in a day schedule
	@Scheduled(fixedDelay = 24 * 60 * 60 * 1000)
    public void autoDeleteIngestedArtifacts() {
		logger.trace("invoking autoDeleteIngestedArtifacts");
		File[] ingestableFiles = new File(configuration.getIngestCompleteDirRoot()).listFiles(); // get the list of ingested artifacts
		for (File file : ingestableFiles) {// loop through the ingested artifacts
			String artifactName = file.getName();
			logger.trace("artifactName " + artifactName);
			Artifact artifact = null; // get the artifact details from DB
		   	Domain[] domains = Domain.values();
    		for (Domain nthDomain : domains) {
				ArtifactRepository<Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(nthDomain);
				artifact = artifactRepository.findByName(artifactName);
				if(artifact != null)
					break;
    		}
    		if(artifact == null) {
    			logger.warn(artifactName + " doesnt exist in dwara. Skipping deleting it");
    		}
    		else {
	    		String artifactclassName = artifact.getArtifactclass().getId();
	    		long durationSinceArtifactIngested = ChronoUnit.DAYS.between(artifact.getWriteRequest().getRequestedAt(), LocalDateTime.now());
	    		logger.trace("durationSinceArtifactIngested " + durationSinceArtifactIngested);
	    		int retentionPeriod = getRetentionPeriod(artifactclassName);
	    		logger.trace("retentionPeriod " + retentionPeriod);
	    		if(retentionPeriod != -1 && durationSinceArtifactIngested >= retentionPeriod) { // if artifact has been there for more than retention period
	    			if(file.isDirectory()) {
	    				try {
							FileUtils.deleteDirectory(file);
							logger.info(artifactName + " deleted");
						} catch (IOException e) {
							logger.error("Unable to delete " + artifactName);
						}
	    			}
	    			else {
	    				if(file.delete()) { //delete it
							logger.info(artifactName + " deleted");
						}
	    			}
	    		}
    		}
		}
    }

	private int getRetentionPeriod(String artifactclassName) {
		String retentionPeriodAsString = env.getProperty("retentionPeriod."+ artifactclassName);
		if(retentionPeriodAsString == null)
			retentionPeriodAsString = env.getProperty("retentionPeriod");
		
		return Integer.parseInt(retentionPeriodAsString);
	}
}