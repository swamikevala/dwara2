package org.ishafoundation.dwaraapi.process.mam.ingest;

import org.apache.http.impl.cookie.BasicClientCookie;
import org.ishafoundation.dwaraapi.process.mam.core.CatDVInteractor;
import org.ishafoundation.dwaraapi.utils.HttpClientUtil;
import org.ishafoundation.dwaraapi.utils.JsonPathUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class CatalogNameUpdater extends CatDVInteractor {
	
	Logger logger = LoggerFactory.getLogger(CatalogNameUpdater.class);
	
	// E.g.,
	// http://localhost:8080/catdv/api/8/catalogs/27?makeNameUnique=true - PUT
	public String updateCatalogName(String jsessionId, String response, String newCatalogName) throws Exception {

		JSONObject insertedClipJsonRespObject = new JSONObject(response);
	    JSONObject insertedClipJsonRespDataObject = insertedClipJsonRespObject.getJSONArray("data").getJSONObject(0);
	    String bodyPayload = insertedClipJsonRespDataObject.toString();
	    bodyPayload = JsonPathUtil.setValue(bodyPayload, "name", newCatalogName);
	    
	    int catalogId = JsonPathUtil.getInteger(bodyPayload, "ID");
	    String endpointUrl = getURLPrefix() + "/catdv/api/8/catalogs/" + catalogId + "?makeNameUnique=true";
        BasicClientCookie cookie = getCookie(jsessionId);	
	    
    	String response2 = null;
		try {
			response2 = HttpClientUtil.putItWithCookie(cookie, endpointUrl, null, bodyPayload);
		} catch (Exception e) {
			String errorMsg = "Unable to update clip " + endpointUrl + " :: "  + e.getMessage();
			logger.error(errorMsg, e);
			throw new Exception(errorMsg);
		}
    	
		return response2;    	
	}	
}
