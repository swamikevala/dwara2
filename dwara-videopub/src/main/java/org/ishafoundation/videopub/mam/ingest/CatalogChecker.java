package org.ishafoundation.videopub.mam.ingest;

import java.util.List;
import java.util.Map;

import org.apache.http.impl.cookie.BasicClientCookie;
import org.ishafoundation.dwaraapi.utils.HttpClientUtil;
import org.ishafoundation.dwaraapi.utils.JsonPathUtil;
import org.ishafoundation.videopub.mam.core.CatDVInteractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class CatalogChecker extends CatDVInteractor {
	
	Logger logger = LoggerFactory.getLogger(CatalogChecker.class);
	
	// E.g.,
	// http://172.18.1.126:8080/catdv/api/8/catalogs?query=((catalog.name)eq(somename)) - GET
	public int getCatalogId(String jsessionId, String catalogName) throws Exception {
		int catalogId = 0;
		String response = getCatalog(jsessionId, catalogName);
		List<Map<String, Object>> catalogs = JsonPathUtil.getArray(response , "data");
		
		if(catalogs.size() > 0) {
			Map<String, Object> catalog = catalogs.get(0);
			catalogId = (int) catalog.get("ID");			
		}
		return catalogId;
	}
	
	public String getCatalog(String jsessionId, String catalogName) throws Exception {
    	String endpointUrl = getURLPrefix() + "/catdv/api/8/catalogs?query=((catalog.name)eq(" + catalogName + "))";

        BasicClientCookie cookie = getCookie(jsessionId);
        
    	String response = null;
		try {
			response = HttpClientUtil.getItWithCookie(cookie, endpointUrl, null);
		} catch (Exception e) {
			String errorMsg = "Unable to get catalogName " + endpointUrl + " :: "  + e.getMessage();
			logger.error(errorMsg, e);
			throw new Exception(errorMsg);
		}  
		
		return response;
	}	
}
