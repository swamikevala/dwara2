package org.ishafoundation.dwaraapi.entrypoint.resource.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ishafoundation.dwaraapi.db.cacheutil.RequesttypeCacheUtil;
import org.ishafoundation.dwaraapi.db.dao.master.LibraryclassDao;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.LibraryclassRequesttypeUserDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.LibraryclassTargetvolumeDao;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.User;
import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassRequesttypeUser;
import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassTargetvolume;
import org.ishafoundation.dwaraapi.db.model.master.reference.Requesttype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;


@CrossOrigin
@RestController
public class LibraryclassController {

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private LibraryclassDao libraryclassDao;
	
	@Autowired
	private LibraryclassRequesttypeUserDao libraryclassRequesttypeUserDao;

	@Autowired
	private LibraryclassTargetvolumeDao libraryclassTargetvolumeDao;
	
	@Autowired
	private RequesttypeCacheUtil requesttypeCacheUtil;

	/*
	userId=3&requestType=ingest&showTargetVolumes=true [200]
			// TODO - user request param is not mandatory,
			 * if user param is not passed then we list out all libraryclasses
			 *  and how about other filters? requesttype?
		*/	
	@ApiOperation(value = "Gets all the libraryclasses allowed for the user for the requesttype")
	@GetMapping("/libraryclass")
	public ResponseEntity<List<org.ishafoundation.dwaraapi.api.resp.Libraryclass>> getLibraryclass(@RequestParam(required=false) String user, @RequestParam(required=false) String requestType, @RequestParam(required=false) boolean showTargetVolumes) { 		

		List<org.ishafoundation.dwaraapi.api.resp.Libraryclass> response_LibraryclassList = new ArrayList<org.ishafoundation.dwaraapi.api.resp.Libraryclass>();

		HashMap<Integer, List<Integer>> libraryclassId_TargetvolumeList_Map = new HashMap<Integer, List<Integer>>();
		if(showTargetVolumes) {
			// gets all Libraryclass to targetvolumes upfront and holds it in memory thus avoiding as many db calls inside the loop
			Iterable<LibraryclassTargetvolume> libraryclassTargetvolumeList = libraryclassTargetvolumeDao.findAll();
			for (LibraryclassTargetvolume libraryclassTargetvolume : libraryclassTargetvolumeList) {
				int libraryclassId = libraryclassTargetvolume.getLibraryclass().getId();
				List<Integer> targetVolumeList = libraryclassId_TargetvolumeList_Map.get(libraryclassId);
				if(targetVolumeList != null) {
					targetVolumeList.add(libraryclassTargetvolume.getTargetvolume().getId());
				}else {
					targetVolumeList = new ArrayList<Integer>();
					targetVolumeList.add(libraryclassTargetvolume.getTargetvolume().getId());
					libraryclassId_TargetvolumeList_Map.put(libraryclassId, targetVolumeList);
				}
			}
		}
		
		Iterable<Libraryclass> libraryclassList = libraryclassDao.findAll();
		if(user == null || requestType == null) { // TODO should we get both together?? Check with swami - // if(user == null && requestType == null) { // if no filters get it all.
			for (Libraryclass nthLibraryclass : libraryclassList) {
				org.ishafoundation.dwaraapi.api.resp.Libraryclass response_Libraryclass = new org.ishafoundation.dwaraapi.api.resp.Libraryclass();
				int libraryclassId = nthLibraryclass.getId();
				response_Libraryclass.setLibraryclassId(libraryclassId);
				response_Libraryclass.setName(nthLibraryclass.getName());
				if(showTargetVolumes) {
					List<Integer> targetVolumeIdList = libraryclassId_TargetvolumeList_Map.get(libraryclassId);
					Integer[] targetVolumeIds = targetVolumeIdList.toArray(new Integer[targetVolumeIdList.size()]); //(int[]) ArrayUtils.toPrimitive();
					response_Libraryclass.setTargetVolumeIds(targetVolumeIds);
				}
				response_LibraryclassList.add(response_Libraryclass);
			}
		}
		else {
			// TODO can one of the filter be null or both expected together
			// do we validate it too... // NOTE There will be duplicates... if one of the parameter is not passed...
			Requesttype requesttypeObj = null;
			if(requestType != null)
				requesttypeObj = requesttypeCacheUtil.getRequesttype(requestType);
			
			User userObj = null;
			if(user != null)
				userObj = userDao.findByName(user); 
			
			List<LibraryclassRequesttypeUser> libraryclassRequesttypeUserList = libraryclassRequesttypeUserDao.findAllByRequesttypeIdAndUserId(requesttypeObj.getId(), userObj.getId());
			for (LibraryclassRequesttypeUser libraryclassRequesttypeUser : libraryclassRequesttypeUserList) {
				Libraryclass libraryclass = libraryclassRequesttypeUser.getLibraryclass();
				int libraryclassId = libraryclass.getId();
				if(libraryclass.isSource()) { // just double ensuring only source libraryclasses are added to the resultset...Ideally only source libraryclass should be configured for the user in libraryclassrequesttypeUser, but just an extra check
					org.ishafoundation.dwaraapi.api.resp.Libraryclass response_Libraryclass = new org.ishafoundation.dwaraapi.api.resp.Libraryclass();
					response_Libraryclass.setLibraryclassId(libraryclassId);
					response_Libraryclass.setName(libraryclass.getName());
					response_Libraryclass.setDisplayOrder(libraryclass.getDisplayOrder());
					if(showTargetVolumes) {
						List<Integer> targetVolumeIdList = libraryclassId_TargetvolumeList_Map.get(libraryclassId);
						Integer[] targetVolumeIds = targetVolumeIdList.toArray(new Integer[targetVolumeIdList.size()]); //(int[]) ArrayUtils.toPrimitive();
						response_Libraryclass.setTargetVolumeIds(targetVolumeIds);
					}
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
}
