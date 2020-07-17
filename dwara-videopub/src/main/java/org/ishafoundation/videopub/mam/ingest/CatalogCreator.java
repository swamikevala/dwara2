package org.ishafoundation.videopub.mam.ingest;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.ishafoundation.dwaraapi.utils.HttpClientUtil;
import org.ishafoundation.dwaraapi.utils.JsonPathUtil;
import org.ishafoundation.videopub.mam.core.CatDVInteractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class CatalogCreator extends CatDVInteractor {
	
	Logger logger = LoggerFactory.getLogger(CatalogCreator.class);
	// E.g.,
	// http://172.18.1.126:8080/catdv/api/8/catalogs?makeNameUnique=true - POST
	public String createCatalog(String jsessionId, String catalogName, int groupId, String comment) throws Exception {
    	String endpointUrl = getURLPrefix() + "/catdv/api/8/catalogs?makeNameUnique=true";

        BasicClientCookie cookie = getCookie(jsessionId);
        
    	String bodyPayload = frameCreateClipPayloadFromTemplate(catalogName, groupId, comment);
    	
    	String response = null;
		try {
			response = HttpClientUtil.postItWithCookie(cookie, endpointUrl, null, bodyPayload);
		} catch (Exception e) {
			String errorMsg = "Unable to insert clip " + endpointUrl + " :: "  + e.getMessage();
			logger.error(errorMsg, e);
			throw new Exception(errorMsg);
		}
    	
		return response;    	
	}
	
	private String frameCreateClipPayloadFromTemplate(String catalogName, int groupId, String comment) throws Exception {
		
		URL fileUrl = getClass().getResource("/catdv/CreateCatalogPayloadTemplate.json");
		java.io.File  templateFile = new java.io.File(fileUrl.getFile());
		String jsonDataSourceString = null;
		try {
			jsonDataSourceString = FileUtils.readFileToString(templateFile, "UTF-8");
		} catch (IOException e) {
			String errorMsg = "Unable to create catalog template " + templateFile.getAbsolutePath() + " :: "  + e.getMessage();
			logger.error(errorMsg, e);
			throw new Exception(errorMsg);
		}

		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "name", catalogName);
		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "groupID", groupId);
		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "comment", comment);
		
		return jsonDataSourceString;
	}
}
