package org.ishafoundation.videopub.mam.ingest;

import org.apache.http.impl.cookie.BasicClientCookie;
import org.ishafoundation.dwaraapi.utils.HttpClientUtil;
import org.ishafoundation.videopub.mam.core.CatDVInteractor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;



@Component
public class ClipUpdater extends CatDVInteractor {
	
	Logger logger = LoggerFactory.getLogger(ClipUpdater.class);

	// E.g.,
	// http://localhost:8080/catdv/api/8/clips/27?jsessionid=A214B210360509D08D05624969D55B3F - PUT
	public String updateClip(String jsessionId, String insertedClipJsonResp, Integer insertedThumbnailID) throws Exception {
		
	    JSONObject insertedClipJsonRespObject = new JSONObject(insertedClipJsonResp);
	    JSONObject insertedClipJsonRespDataObject = insertedClipJsonRespObject.getJSONObject("data");
	    
	    int insertedClipID = insertedClipJsonRespDataObject.getInt("ID");
	    
    	//String endpointUrl = getURLPrefix() + "/catdv/api/8/clips/" + insertedClipID + "?jsessionid=" + jsessionId;
	    String endpointUrl = getURLPrefix() + "/catdv/api/8/clips/" + insertedClipID;
	    
        BasicClientCookie cookie = getCookie(jsessionId);	
    	
	    JSONObject bodyPayload = insertedClipJsonRespDataObject.put("posterID", insertedThumbnailID);
	    int[] thumbnails = {insertedThumbnailID};
	    bodyPayload = bodyPayload.put("thumbnailIDs", thumbnails);
    	
    	String response = null;
		try {
			response = HttpClientUtil.putItWithCookie(cookie, endpointUrl, null, bodyPayload.toString());
		} catch (Exception e) {
			String errorMsg = "Unable to update clip " + endpointUrl + " :: "  + e.getMessage();
			logger.error(errorMsg, e);
			throw new Exception(errorMsg);
		}
    	
		return response;    	
	}
}
