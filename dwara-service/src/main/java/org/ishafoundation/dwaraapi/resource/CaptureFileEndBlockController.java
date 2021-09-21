package org.ishafoundation.dwaraapi.resource;

import io.swagger.annotations.ApiOperation;
import org.ishafoundation.dwaraapi.api.req.restore.PFRestoreUserRequest;
import org.ishafoundation.dwaraapi.api.req.restore.RestoreUserRequest;
import org.ishafoundation.dwaraapi.api.resp.restore.File;
import org.ishafoundation.dwaraapi.api.resp.restore.RestoreResponse;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.CaptureFileVolumeEndBlockService;
import org.ishafoundation.dwaraapi.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CrossOrigin
@RestController
public class CaptureFileEndBlockController {
	private static final Logger logger = LoggerFactory.getLogger(CaptureFileEndBlockController.class);

	@Autowired
	CaptureFileVolumeEndBlockService fileEndBlockService;

	@ApiOperation(value = "Capture the End Block for files in the File_Volume Table")
	@PostMapping("/file/capture/endblock")
	public ResponseEntity<String> captureEndBlock(@RequestParam(value = "id") String volumeId) {
		try {
			System.out.println("End Block Controller");
			fileEndBlockService.fileVolumeEndBlock(volumeId);
		}catch (Exception e) {
			String errorMsg = "File Volume End Block Capture Error: - " + e.getMessage();
			logger.error(errorMsg, e);
			e.printStackTrace();
		}

		return ResponseEntity.status(HttpStatus.OK).body(String.valueOf("Success"));
	}

}
