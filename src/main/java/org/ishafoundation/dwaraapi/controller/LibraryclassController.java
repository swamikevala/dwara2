package org.ishafoundation.dwaraapi.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.common.UserDao;
import org.ishafoundation.dwaraapi.db.dao.master.ingest.LibraryclassDao;
import org.ishafoundation.dwaraapi.db.dao.master.ingest.LibraryclassUserDao;
import org.ishafoundation.dwaraapi.db.model.master.common.User;
import org.ishafoundation.dwaraapi.db.model.master.ingest.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.ingest.LibraryclassUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;


@RestController
public class LibraryclassController {

	@Autowired
	private LibraryclassDao libraryclassDao;
	
	@Autowired
	private UserDao userDao;

	@Autowired
	private LibraryclassUserDao libraryclassUserDao;
	
	@ApiOperation(value = "Gets all the libraryclasses allowed for the user")
	@GetMapping("/libraryclass")
	public ResponseEntity<List<org.ishafoundation.dwaraapi.api.resp.Libraryclass>> getAllAllowedLibraryclassesForUser(@RequestParam String user) {
		List<org.ishafoundation.dwaraapi.api.resp.Libraryclass> response_LibraryclassList = new ArrayList<org.ishafoundation.dwaraapi.api.resp.Libraryclass>();
		
		// gets all Libraryclass upfront and holds it in memory thus avoiding as many db calls inside the loop
		HashMap<Integer, Libraryclass> libraryclassId_Libraryclass_Map = new HashMap<Integer, Libraryclass>();
		Iterable<Libraryclass> libraryclassList = libraryclassDao.findAll();
		for (Libraryclass nthLibraryclass : libraryclassList) {
			libraryclassId_Libraryclass_Map.put(nthLibraryclass.getLibraryclassId(), nthLibraryclass);
		}

		User userObj = userDao.findByName(user);
		
		List<LibraryclassUser> libraryclassUserList = libraryclassUserDao.findAllByUserId(userObj.getUserId());
		for (LibraryclassUser nthLibraryclassUser : libraryclassUserList) {
			Libraryclass nthAllowedLibraryclass = libraryclassId_Libraryclass_Map.get(nthLibraryclassUser.getLibraryclassId());
			if(nthAllowedLibraryclass.isSource()) { // just double ensuring only source libraryclasses are added to the resultset...Ideally only source libraryclass should be configured for the user in libraryclassUser, but just an extra check
				org.ishafoundation.dwaraapi.api.resp.Libraryclass response_Libraryclass = new org.ishafoundation.dwaraapi.api.resp.Libraryclass();
				response_Libraryclass.setLibraryclassId(nthAllowedLibraryclass.getLibraryclassId());
				response_Libraryclass.setName(nthAllowedLibraryclass.getName());
				response_Libraryclass.setDisplayOrder(nthLibraryclassUser.getDisplayOrder());
				response_LibraryclassList.add(response_Libraryclass);
			}
		}
		
		if (response_LibraryclassList.size() > 0) {
			return ResponseEntity.ok(response_LibraryclassList);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}
	}
}
