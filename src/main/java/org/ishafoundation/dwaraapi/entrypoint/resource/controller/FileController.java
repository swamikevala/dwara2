package org.ishafoundation.dwaraapi.entrypoint.resource.controller;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.model.transactional.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class FileController {
	
	@Autowired
	FileDao fileDao;
	
	@GetMapping("/file")
	public ResponseEntity<List<File>>  getAllFiles() {
		List<File> fileList = new ArrayList<File>();
		
		fileList = (List<File>) fileDao.findAll();
		if (fileList.size() > 0) {
			return ResponseEntity.ok(fileList);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}
		 
	}
}
