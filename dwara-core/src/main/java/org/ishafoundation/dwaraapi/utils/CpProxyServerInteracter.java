package org.ishafoundation.dwaraapi.utils;

import java.util.Base64;

import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CpProxyServerInteracter {

	@Autowired
	private Configuration configuration;
	
	private static final Logger logger = LoggerFactory.getLogger(CpProxyServerInteracter.class);
	
	public String callCpProxyServer(String endpointUrlSuffix, String postBody) {
		
		String endpointUrl = "http://" + configuration.getCpServerIp() + ":8080/api" + endpointUrlSuffix;
		String userName = "pgurumurthy";
		String pwd = "Shiva0!";
		
		String responseFromCPServerAsString = null;
		
		String encodedCreds = Base64.getEncoder().encodeToString((userName + ":" + pwd).getBytes());
		String authHeader = "Basic " + encodedCreds;
		try {
			responseFromCPServerAsString = HttpClientUtil.postIt(endpointUrl, authHeader, postBody);
			logger.info("responseFromCPServerAsString  " + responseFromCPServerAsString);
		} catch (Exception e) {
			logger.error("Unable to call " + endpointUrl + "::" + e.getMessage(), e);
		}
		return responseFromCPServerAsString;
	}
}
