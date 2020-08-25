package org.ishafoundation.dwaraapi.resource.admin;

import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
