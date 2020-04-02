package org.ishafoundation.dwaraapi.entrypoint.resource.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.api.exception.DwaraException;
import org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams;
import org.ishafoundation.dwaraapi.api.resp.ingest.IngestFile;
import org.ishafoundation.dwaraapi.utils.ObjectMappingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@CrossOrigin
@RestController
public class ExceptionHandlingTestController {
	
	@Autowired
	private ObjectMappingUtil entityToResourceMappingUtils;
	
	@PostMapping("/dryyyThis")
    public org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Request dryyyThis(@RequestBody org.ishafoundation.dwaraapi.api.req.ingest.UserRequest userRequest) throws IOException{	
		
		List<IngestFile> ingestFileList = new ArrayList<IngestFile>();
    	List<LibraryParams> libraryParamsList = userRequest.getLibrary();
    	for (Iterator<LibraryParams> iterator = libraryParamsList.iterator(); iterator.hasNext();) {
    		LibraryParams nthLibraryParams = iterator.next();
    		IngestFile ingestFile = entityToResourceMappingUtils.frameIngestFileObject(nthLibraryParams);
    		ingestFile.setErrorType("Error");
    		ingestFile.setErrorMessage("somemesage");
    		ingestFileList.add(ingestFile);
    	}
    	
		ObjectMapper mapper = new ObjectMapper(); 
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		JsonNode jsonNode = mapper.valueToTree(ingestFileList);
		throw new DwaraException("Pre ingest validation failed", jsonNode);
	}
}
