package org.ishafoundation.videopub.mam.ingest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.cookie.BasicClientCookie;
import org.ishafoundation.dwaraapi.utils.HttpClientUtil;
import org.ishafoundation.dwaraapi.utils.JsonPathUtil;
import org.ishafoundation.videopub.mam.core.CatDVInteractor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class ClipIdsGetter extends CatDVInteractor {
	
	Logger logger = LoggerFactory.getLogger(ClipIdsGetter.class);

	// E.g.,
	// http://172.18.1.224:8080/catdv/api/8/clips?skip=0&take=100&cached=true&catalogID=25937&include=userfields%2Cthumbnails - GET
	public List<Integer> getClipIds(String jsessionId, int catalogId) throws Exception {
    	String endpointUrl = getURLPrefix() + "/catdv/api/8/clips?catalogID=" + catalogId + "&include=userfields";

        BasicClientCookie cookie = getCookie(jsessionId);
        List<Integer> clipIdsList = new ArrayList<Integer>();
    	String response = null;
		try {
			response = HttpClientUtil.getItWithCookie(cookie, endpointUrl, null);
			JSONObject jsonRespObject = new JSONObject(response);
			JSONObject dataObject = jsonRespObject.getJSONObject("data");
		    String data = dataObject.toString();
			List<Map<String, Object>> clips = JsonPathUtil.getArray(data , "items");
//			List<Map<String, Object>> clips = JsonPathUtil.getArray(response , "data");
			for (Map<String, Object> nthClip : clips) {
				int clipId = (int) nthClip.get("ID");
				clipIdsList.add(clipId);
			}

		} catch (Exception e) {
			String errorMsg = "Unable to get clid ids " + endpointUrl + " :: "  + e.getMessage();
			logger.error(errorMsg, e);
			throw new Exception(errorMsg);
		}  
		
		return clipIdsList;
	}
}
