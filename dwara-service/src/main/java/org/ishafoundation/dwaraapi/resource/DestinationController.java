package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.resp.destination.Destination;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class DestinationController {
	
	private static final Logger logger = LoggerFactory.getLogger(DestinationController.class);
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@GetMapping(value = "/destination", produces = "application/json")
    public ResponseEntity<List<Destination>> getAllConfiguredDestinations(){
		List<Destination> destinationList = new ArrayList<Destination>();
		List<org.ishafoundation.dwaraapi.db.model.master.configuration.Destination> allDestinationsFromDB = configurationTablesUtil.getAllDestinations();
		
		for (org.ishafoundation.dwaraapi.db.model.master.configuration.Destination destinationFromDB : allDestinationsFromDB) {
			Destination destination = new Destination();
			destination.setId(destinationFromDB.getId());
			destination.setPath(destinationFromDB.getPath());
			destinationList.add(destination);
		}

		return ResponseEntity.status(HttpStatus.OK).body(destinationList);
	}
	
}
