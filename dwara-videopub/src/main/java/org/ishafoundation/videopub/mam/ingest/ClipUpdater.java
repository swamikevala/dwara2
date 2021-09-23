package org.ishafoundation.videopub.mam.ingest;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.ishafoundation.dwaraapi.utils.HttpClientUtil;
import org.ishafoundation.dwaraapi.utils.JsonPathUtil;
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
	public String updateClip(String jsessionId, String insertedClipJsonResp, Integer insertedThumbnailID, String metaDataJson, String lowResFilePath) throws Exception {
		
	    JSONObject insertedClipJsonRespObject = new JSONObject(insertedClipJsonResp);
	    JSONObject insertedClipJsonRespDataObject = insertedClipJsonRespObject.getJSONObject("data");
	    
	    int insertedClipID = insertedClipJsonRespDataObject.getInt("ID");
	    
    	//String endpointUrl = getURLPrefix() + "/catdv/api/8/clips/" + insertedClipID + "?jsessionid=" + jsessionId;
	    String endpointUrl = getURLPrefix() + "/catdv/api/8/clips/" + insertedClipID;
	    
        BasicClientCookie cookie = getCookie(jsessionId);	
    	
	    JSONObject bodyPayloadJsonObj = insertedClipJsonRespDataObject.put("posterID", insertedThumbnailID);
	    int[] thumbnails = {insertedThumbnailID};
	    bodyPayloadJsonObj = bodyPayloadJsonObj.put("thumbnailIDs", thumbnails);
    	
		String mediaPath = null;
		String name = null;
		String bin = null;
		try {
			mediaPath = JsonPathUtil.getValue(metaDataJson, "format.filename");
			name = FilenameUtils.getName(mediaPath);
			bin = StringUtils.substringAfterLast(FilenameUtils.getFullPathNoEndSeparator(mediaPath), File.separator);
		} catch (Exception e) {
			logger.warn("Unable to get media details. So defaulting it to null.");
		}
		
	    String bodyPayload = bodyPayloadJsonObj.toString();
	    bodyPayload = JsonPathUtil.setValue(bodyPayload, "name", name);
	    bodyPayload = JsonPathUtil.setValue(bodyPayload, "media.filePath", lowResFilePath);
	    bodyPayload = JsonPathUtil.setValue(bodyPayload, "importSource.file", mediaPath);
	    
    	String response = null;
		try {
			response = HttpClientUtil.putItWithCookie(cookie, endpointUrl, null, bodyPayload);
		} catch (Exception e) {
			String errorMsg = "Unable to update clip " + endpointUrl + " :: "  + e.getMessage();
			logger.error(errorMsg, e);
			throw new Exception(errorMsg);
		}
    	
		return response;    	
	}
}
