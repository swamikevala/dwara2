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
public class ClipFPSUpdater extends CatDVInteractor {
	
	Logger logger = LoggerFactory.getLogger(ClipFPSUpdater.class);

	// E.g.,
	// http://localhost:8080/catdv/api/8/clips/27?jsessionid=A214B210360509D08D05624969D55B3F - PUT
	public String updateClip(String jsessionId, String nthClipID, Float fps) throws Exception {
		
	    String endpointUrl = getURLPrefix() + "/catdv/api/8/clips/" + nthClipID;
	    
        BasicClientCookie cookie = getCookie(jsessionId);	

    	String response = null;
		try {
			response = HttpClientUtil.getItWithCookie(cookie, endpointUrl, null);
		} catch (Exception e) {
			String errorMsg = "Unable to get clip " + endpointUrl + " :: "  + e.getMessage();
			logger.error(errorMsg, e);
			throw new Exception(errorMsg);
		}        

		JSONObject insertedClipJsonRespObject = new JSONObject(response);
	    JSONObject insertedClipJsonRespDataObject = insertedClipJsonRespObject.getJSONObject("data");
	    
	    String bodyPayload = insertedClipJsonRespDataObject.toString();
	    
		bodyPayload = JsonPathUtil.setValue(bodyPayload, "fps", fps);
		bodyPayload = JsonPathUtil.setValue(bodyPayload, "in.fmt", fps);
		bodyPayload = JsonPathUtil.setValue(bodyPayload, "out.fmt", fps);
		bodyPayload = JsonPathUtil.setValue(bodyPayload, "media.tcFmt", fps);
		bodyPayload = JsonPathUtil.setValue(bodyPayload, "media.start.fmt", fps);
		bodyPayload = JsonPathUtil.setValue(bodyPayload, "media.end.fmt", fps);
		bodyPayload = JsonPathUtil.setValue(bodyPayload, "mediaStart.fmt", fps);
		bodyPayload = JsonPathUtil.setValue(bodyPayload, "mediaEnd.fmt", fps);
		
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
