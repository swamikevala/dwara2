package org.ishafoundation.dwaraapi.entrypoint.resource.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.entrypoint.resource.mapper.SubrequestEntityResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@CrossOrigin
@RestController
public class ObjectMappingTestController {
	
    @Autowired
    SubrequestEntityResourceMapper subrequestEntityResourceMapper;

    @Autowired
    SubrequestDao subrequestDao;

	@GetMapping
	@RequestMapping({ "/testMapping" })
	public ResponseEntity<List<org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Subrequest>> testMapping() throws JsonParseException, JsonMappingException, IOException {
		List<org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Subrequest> resourceSubrequestList = new ArrayList<org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Subrequest>();
		List<Subrequest> subrequestList = subrequestDao.findAllByRequestId(1);
    	for (Iterator<Subrequest> iterator = subrequestList.iterator(); iterator.hasNext();) {
			Subrequest subrequest = (Subrequest) iterator.next();
			
			org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Subrequest resourceSubrequest = subrequestEntityResourceMapper.entityToResource(subrequest);
			resourceSubrequestList.add(resourceSubrequest);

		}
		return ResponseEntity.ok(resourceSubrequestList);
	}
}
