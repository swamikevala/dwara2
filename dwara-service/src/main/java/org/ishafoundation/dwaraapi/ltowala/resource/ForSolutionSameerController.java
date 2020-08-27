package org.ishafoundation.dwaraapi.ltowala.resource;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.ltowala.api.resp.LtoWalaResponse;
import org.ishafoundation.dwaraapi.ltowala.service.ForSolutionSameerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public class ForSolutionSameerController {
	private static final Logger logger = LoggerFactory.getLogger(ForSolutionSameerController.class);
	
	@Autowired
	ForSolutionSameerService forSolutionSameerService;
	
	@ApiOperation(value = "Gets data dump for LTO Wala")
	@ApiResponses(value = { 
		    @ApiResponse(code = 200, message = "Ok"),
		    @ApiResponse(code = 400, message = "Error")
	})
	@GetMapping(value="/getLtoWalaData", produces = "application/json")
    public ResponseEntity<LtoWalaResponse> getLtoWalaData(@RequestParam(required=true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,  @RequestParam(required=true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate){
		
		LtoWalaResponse ltoWalaResponse = null;
		try {
			LocalDateTime startDateTime = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			LocalDateTime endDateTime = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			ltoWalaResponse = forSolutionSameerService.dataForLtoWala(startDateTime, endDateTime);
		}catch (Exception e) {
			String errorMsg = "Unable to get data for ltowala - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		
		return ResponseEntity.status(HttpStatus.OK).body(ltoWalaResponse);
	}	
}
