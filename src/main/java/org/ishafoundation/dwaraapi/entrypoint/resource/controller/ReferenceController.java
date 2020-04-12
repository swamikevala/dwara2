package org.ishafoundation.dwaraapi.entrypoint.resource.controller;

import java.util.List;

import org.ishafoundation.dwaraapi.constants.Reference;
import org.ishafoundation.dwaraapi.db.dao.master.ActionDao;
import org.ishafoundation.dwaraapi.db.dao.master.StatusDao;
import org.ishafoundation.dwaraapi.db.dao.master.TargetvolumeDao;
import org.ishafoundation.dwaraapi.db.model.master.Targetvolume;
import org.ishafoundation.dwaraapi.db.model.master.reference.Action;
import org.ishafoundation.dwaraapi.db.model.master.reference.Status;
import org.ishafoundation.dwaraapi.entrypoint.resource.ingest.AllReferences;
import org.ishafoundation.dwaraapi.entrypoint.resource.ingest.SingleReference;
import org.ishafoundation.dwaraapi.entrypoint.resource.mapper.MiscObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public class ReferenceController {

	private static final Logger logger = LoggerFactory.getLogger(ReferenceController.class);

	@Autowired
	private ActionDao actionDao;	

	@Autowired
	private StatusDao statusDao;	
	
	@Autowired
	private TargetvolumeDao targetvolumeDao;		
	
	@Autowired
	private MiscObjectMapper miscObjectMapper; 

	
	@ApiOperation(value = "Scans the selected directory path chosen in the dropdown and lists all candidate folders for users to ingest it.", response = List.class)
	@ApiResponses(value = { 
		    @ApiResponse(code = 200, message = "Ok"),
		    @ApiResponse(code = 404, message = "Not Found")
	})
	@GetMapping("/reference")
	public ResponseEntity<AllReferences> getAllReferences() {
		
		AllReferences allReferences = new AllReferences();

//		// TODO - How to do this dynamically so that the list of references can be configured. Possible?
//		for (Reference reference : Reference.values()) {
//		
//		}
		List<Status> statusList = (List<Status>) statusDao.findAll();
		List<Action> actionList = (List<Action>) actionDao.findAll();
		
		allReferences.setStatus(statusList);
		allReferences.setAction(actionList);
		
		return ResponseEntity.ok(allReferences);
	}
	
	@ApiOperation(value = "Scans the selected directory path chosen in the dropdown and lists all candidate folders for users to ingest it.", response = List.class)
	@ApiResponses(value = { 
		    @ApiResponse(code = 200, message = "Ok"),
		    @ApiResponse(code = 404, message = "Not Found")
	})
	@GetMapping("/reference/{referenceName}")
	public ResponseEntity<List<SingleReference>> getSpecificReference(@PathVariable("referenceName") Reference reference) {
		List<SingleReference> singleReferenceList = null;
		switch (reference) {
		case Status:
			List<Status> statusList = (List<Status>) statusDao.findAll();
			singleReferenceList = miscObjectMapper.statusListToSingleReferenceList(statusList);
			break;
		case Action:
			List<Action> actionList = (List<Action>) actionDao.findAll();
			singleReferenceList = miscObjectMapper.actionListToSingleReferenceList(actionList);
			break;
		default:
			logger.error("Not supported");
			break;
		}
		
		if (singleReferenceList.size() > 0) {
			return ResponseEntity.ok(singleReferenceList);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); //NO_CONTENT
		}
	}
	
	@ApiOperation(value = "Lists all the target volumes", response = List.class)
	@GetMapping("/reference/targetvolume")
	public ResponseEntity<List<Targetvolume>> getAllTargetVolumes() {
		List<Targetvolume> targetvolumeList = (List<Targetvolume>) targetvolumeDao.findAll();
		if (targetvolumeList.size() > 0) {
			return ResponseEntity.ok(targetvolumeList);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}
	}
}

