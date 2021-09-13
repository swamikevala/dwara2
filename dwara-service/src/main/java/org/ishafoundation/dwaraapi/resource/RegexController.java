package org.ishafoundation.dwaraapi.resource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ishafoundation.dwaraapi.api.req.regex.RegexTesterBody;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegexController {
	
	private static final Logger logger = LoggerFactory.getLogger(RegexController.class);
	
	// {"regex": "", "text" : "5"}
	@RequestMapping(value = "/regexMatch", method = RequestMethod.POST)
	public ResponseEntity<Boolean> matchRegex(@RequestBody RegexTesterBody regexTesterBody) {
		logger.info("/regexMatch");
		Boolean response = false;
		try {
			Pattern patternToTest = Pattern.compile(regexTesterBody.getRegex());
			
			Matcher m = patternToTest.matcher(regexTesterBody.getText());
			if(m.matches()) {
				response = true;
			}
		}catch (Exception e) {
			logger.error(e.getMessage(),e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(e.getMessage(), null);
		}		
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
