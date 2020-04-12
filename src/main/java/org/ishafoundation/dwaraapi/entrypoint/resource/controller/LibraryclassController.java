package org.ishafoundation.dwaraapi.entrypoint.resource.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.LibraryclassActionUserDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.LibraryclassTargetvolumeDao;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.User;
import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassActionUser;
import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassTargetvolume;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;


@CrossOrigin
@RestController
public class LibraryclassController {

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private LibraryclassActionUserDao libraryclassActionUserDao;

	@Autowired
	private LibraryclassTargetvolumeDao libraryclassTargetvolumeDao;
	
	@ApiOperation(value = "Gets all the libraryclasses allowed for the user")
	@GetMapping("/libraryclass")
	public ResponseEntity<List<org.ishafoundation.dwaraapi.entrypoint.resource.ingest.list.LibraryclassForList>> getLibraryclass() { 		

		List<org.ishafoundation.dwaraapi.entrypoint.resource.ingest.list.LibraryclassForList> response_LibraryclassList = new ArrayList<org.ishafoundation.dwaraapi.entrypoint.resource.ingest.list.LibraryclassForList>();

		HashMap<Integer, List<String>> libraryclassId_TargetvolumeList_Map = new HashMap<Integer, List<String>>();
		// gets all Libraryclass to targetvolumes upfront and holds it in memory thus avoiding as many db calls inside the loop
		Iterable<LibraryclassTargetvolume> libraryclassTargetvolumeList = libraryclassTargetvolumeDao.findAll();
		for (LibraryclassTargetvolume libraryclassTargetvolume : libraryclassTargetvolumeList) {
			int libraryclassId = libraryclassTargetvolume.getLibraryclass().getId();
			List<String> targetVolumeList = libraryclassId_TargetvolumeList_Map.get(libraryclassId);
			if(targetVolumeList != null) {
				targetVolumeList.add(libraryclassTargetvolume.getTargetvolume().getName());
			}else {
				targetVolumeList = new ArrayList<String>();
				targetVolumeList.add(libraryclassTargetvolume.getTargetvolume().getName());
				libraryclassId_TargetvolumeList_Map.put(libraryclassId, targetVolumeList);
			}
		}
		
		String user = getUserFromContext();
		User userObj = userDao.findByName(user); 
		
		List<LibraryclassActionUser> libraryclassActionUserList = libraryclassActionUserDao.findAllByUserId(userObj.getId());
		HashMap<Integer, List<String>> libraryclassId_PermittedActions_Map = new HashMap<Integer, List<String>>();
		
		for (LibraryclassActionUser libraryclassActionUser : libraryclassActionUserList) {
			Libraryclass libraryclass = libraryclassActionUser.getLibraryclass();
			int libraryclassId = libraryclass.getId();
			if(libraryclass.isSource()) { // just double ensuring only source libraryclasses are added to the resultset...Ideally only source libraryclass should be configured for the user in libraryclassactionUser table, but just an extra check
				List<String> permittedActionsList = libraryclassId_PermittedActions_Map.get(libraryclassId);
				if(permittedActionsList != null) {
					permittedActionsList.add(libraryclassActionUser.getAction().getName());
				}else {
					permittedActionsList = new ArrayList<String>();
					permittedActionsList.add(libraryclassActionUser.getAction().getName());
					libraryclassId_PermittedActions_Map.put(libraryclassId, permittedActionsList);
				}
			}
		}
		
		HashMap<Integer, org.ishafoundation.dwaraapi.entrypoint.resource.ingest.list.LibraryclassForList> libraryclassId_ResponseLibraryclass_Map = new HashMap<Integer, org.ishafoundation.dwaraapi.entrypoint.resource.ingest.list.LibraryclassForList>();
		for (LibraryclassActionUser libraryclassActionUser : libraryclassActionUserList) {
			Libraryclass libraryclass = libraryclassActionUser.getLibraryclass();
			int libraryclassId = libraryclass.getId();
			if(libraryclass.isSource()) { // just double ensuring only source libraryclasses are added to the resultset...Ideally only source libraryclass should be configured for the user in libraryclassactionUser table, but just an extra check
				org.ishafoundation.dwaraapi.entrypoint.resource.ingest.list.LibraryclassForList response_Libraryclass = libraryclassId_ResponseLibraryclass_Map.get(libraryclassId);
						
				if(response_Libraryclass == null) {
					response_Libraryclass = new org.ishafoundation.dwaraapi.entrypoint.resource.ingest.list.LibraryclassForList();
					response_Libraryclass.setLibraryclassId(libraryclassId);
					response_Libraryclass.setName(libraryclass.getName());
					response_Libraryclass.setDisplayOrder(libraryclass.getDisplayOrder());
	
					List<String> targetVolumesList = libraryclassId_TargetvolumeList_Map.get(libraryclassId);
					String[] targetVolumes = targetVolumesList.toArray(new String[targetVolumesList.size()]);
					response_Libraryclass.setTargetVolumes(targetVolumes);

					List<String> permittedActionsList = libraryclassId_PermittedActions_Map.get(libraryclassId);
					String[] permittedActions = permittedActionsList.toArray(new String[permittedActionsList.size()]); 
					response_Libraryclass.setPermittedActions(permittedActions);
					
					libraryclassId_ResponseLibraryclass_Map.put(libraryclassId, response_Libraryclass);
					response_LibraryclassList.add(response_Libraryclass);
				}
			}
		}
		
		if (response_LibraryclassList.size() > 0) {
			return ResponseEntity.ok(response_LibraryclassList);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}
	}
	
	private String getUserFromContext() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}
}
