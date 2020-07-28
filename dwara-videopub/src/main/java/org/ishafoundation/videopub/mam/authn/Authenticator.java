package org.ishafoundation.videopub.mam.authn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.http.StatusLine;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.ishafoundation.dwaraapi.utils.HttpClientUtil;
import org.ishafoundation.dwaraapi.utils.JsonPathUtil;
import org.ishafoundation.videopub.mam.core.CatDVInteractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Authenticator extends CatDVInteractor {

	Logger logger = LoggerFactory.getLogger(Authenticator.class);
	
	// http://35.244.61.125:8080/catdv/api/8/session - POST
	public String authenticate() {

		String endpointUrl = getURLPrefix() + "/catdv/api/8/session";

		String postBodyPayload = framePostBodyFromTemplate(getCatDVUserID(), getCatDVUserPwd());

		String response = null;
		try {
			response = HttpClientUtil.postIt(endpointUrl, null, postBodyPayload);
		} catch (Exception e) {
			String errorMsg = "Unable to create session " + endpointUrl + " :: "  + e.getMessage();
			logger.error(errorMsg, e);
			return null;
		}

		return response;
	}

	private String framePostBodyFromTemplate(String userID, String pwd) {
		String templateFile = "/catdv/AuthPayloadTemplate.json";
		String jsonDataSourceString = null;
		try {
			jsonDataSourceString = loadTemplate(templateFile);
		} catch (Exception e) {
			String errorMsg = "Unable to read template file " + templateFile + " :: "  + e.getMessage();
			logger.error(errorMsg, e);
			return null;
		}

		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "username", userID);
		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "password", pwd);
		
		return jsonDataSourceString;
	}

	public String deleteSession(String jsessionId) {

		String endpointUrl = getURLPrefix() + "/catdv/api/8/session";
		
		BasicClientCookie cookie = getCookie(jsessionId);

		String response = null;
		try {
			StatusLine statusLine = HttpClientUtil.deleteItWithCookie(cookie, endpointUrl);
			response = statusLine.getStatusCode() + "::" + statusLine.getReasonPhrase();
		} catch (Exception e) {
			String errorMsg = "Unable to delete session " + endpointUrl + " :: "  + e.getMessage();
			logger.error(errorMsg, e);
		}

		return response;
	}	
	
}
