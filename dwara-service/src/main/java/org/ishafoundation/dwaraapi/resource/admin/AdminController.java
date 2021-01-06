package org.ishafoundation.dwaraapi.resource.admin;

import org.ishafoundation.dwaraapi.ApplicationStatus;
import org.ishafoundation.dwaraapi.DwaraApiApplication;
import org.ishafoundation.dwaraapi.api.resp.artifact.ArtifactResponse;
import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.ishafoundation.dwaraapi.job.JobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {
	
	@SuppressWarnings("rawtypes")
	@Autowired
	private DBMasterTablesCacheManager dBMasterTablesCacheManager;
	
	@RequestMapping(value = "/clearAndReload", method = RequestMethod.POST) 
	public ResponseEntity<String> clearAndReload() {
		dBMasterTablesCacheManager.clearAll();
		dBMasterTablesCacheManager.loadAll();
		return ResponseEntity.status(HttpStatus.OK).body("Done");
	}
	
	// Application - MODE/Status
	@RequestMapping(value = "/application/mode/{mode}", method = RequestMethod.POST)
	public ResponseEntity<String> setMode(@PathVariable("mode") String mode) {
		JobManager.MODE = ApplicationStatus.valueOf(mode);
		return ResponseEntity.status(HttpStatus.OK).body(JobManager.MODE.name());
	}
	
	@RequestMapping(value = "/application/status", method = RequestMethod.GET)
	public ResponseEntity<String> getMode() {
		return ResponseEntity.status(HttpStatus.OK).body(JobManager.MODE.name());
	}
}
