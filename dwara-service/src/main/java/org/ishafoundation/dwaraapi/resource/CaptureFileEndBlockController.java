package org.ishafoundation.dwaraapi.resource;

import io.swagger.annotations.ApiOperation;
import org.ishafoundation.dwaraapi.service.CaptureFileVolumeEndBlockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
public class CaptureFileEndBlockController {
	private static final Logger logger = LoggerFactory.getLogger(CaptureFileEndBlockController.class);

	@Autowired
	CaptureFileVolumeEndBlockService fileEndBlockService;

	@ApiOperation(value = "Capture the End Block for files in the File_Volume Table")
	@PostMapping("/file/capture/endblock")
	public ResponseEntity<String> captureEndBlock(@RequestParam(value = "id", required = false) List<String> volumeId) {
		try {
			fileEndBlockService.fileVolumeEndBlock(volumeId);
		}catch (Exception e) {
			String errorMsg = "File Volume End Block Capture Error: - " + e.getMessage();
			logger.error(errorMsg, e);
			e.printStackTrace();
		}

		return ResponseEntity.status(HttpStatus.OK).body(String.valueOf("Success - End Block of file_volume table is updated"));
	}

}
