package org.ishafoundation.dwaraapi.entrypoint.resource.controller.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class TestAutomationContoller {
	
	Logger logger = LoggerFactory.getLogger(TestAutomationContoller.class);

	@ApiOperation(value = "Gets the details of all the files requested in fileIdsListAsCSV")
	@GetMapping("/getIngestListJson")
	public ResponseEntity<String> getIngestListJson() {
		
		URL fileUrl = getClass().getResource("/test/responses/libraryclassList.json");
		File  templateFile = new File(fileUrl.getFile());
		String jsonDataSourceString = null;
		try {
			jsonDataSourceString = FileUtils.readFileToString(templateFile, "UTF-8");
		} catch (IOException e) {
			String errorMsg = "Unable to read template file " + templateFile + " :: "  + e.getMessage();
			logger.error(errorMsg, e);
			return null;
		}

		return ResponseEntity.status(HttpStatus.OK).body(jsonDataSourceString);
	}
}
