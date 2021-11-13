package org.ishafoundation.dwaraapi.resource;

import io.swagger.annotations.ApiOperation;
import org.ishafoundation.dwaraapi.service.DwaraHoverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
public class DwaraHoverController {
	private static final Logger logger = LoggerFactory.getLogger(DwaraHoverController.class);

	@Autowired
	DwaraHoverService dwaraHoverService;

	@ApiOperation(value = "List of Files as per the given Search Criteria")
	@GetMapping("/search")
	public ResponseEntity<List> list(@RequestParam(value = "items", required = true) List<String> searchWords, @RequestParam(value = "require", required = true, defaultValue = "all") String type, @RequestParam(value = "category", required = false, defaultValue = "folder") String category, @RequestParam(value = "contentType", required = false, defaultValue = "all") String contentType, @RequestParam(value = "offset", required = false, defaultValue = "0") int offset, @RequestParam(value = "limit", required = false, defaultValue = "10") int limit) {
		if(limit > 1000) {
			limit = 1000;
		}

		List dwaraHoverFileLists = dwaraHoverService.getSearchData(searchWords, type, category, contentType, offset, limit);

		return ResponseEntity.ok().body(dwaraHoverFileLists);
	}


}