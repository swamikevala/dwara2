package org.ishafoundation.videopub.mam.ingest;

import org.apache.http.StatusLine;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.ishafoundation.dwaraapi.utils.HttpClientUtil;
import org.ishafoundation.videopub.mam.core.CatDVInteractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class CatalogDeleter extends CatDVInteractor {
	
	Logger logger = LoggerFactory.getLogger(CatalogDeleter.class);
	// E.g.,
	// http://172.18.1.126:8080/catdv/api/8/catalogs/2034 - DELETE
	public String deleteCatalog(String jsessionId, int catalogId) throws Exception {
    	String endpointUrl = getURLPrefix() + "/catdv/api/8/catalogs/" + catalogId;

        BasicClientCookie cookie = getCookie(jsessionId);
    	
    	String response = null;
		try {
			StatusLine statusLine = HttpClientUtil.deleteItWithCookie(cookie, endpointUrl);
			response = statusLine.getStatusCode() + "::" + statusLine.getReasonPhrase();
		} catch (Exception e) {
			String errorMsg = "Unable to delete catalog " + endpointUrl + " :: "  + e.getMessage();
			logger.error(errorMsg, e);
			throw new Exception(errorMsg);
		}
    	
		return response;    	
	}
}
