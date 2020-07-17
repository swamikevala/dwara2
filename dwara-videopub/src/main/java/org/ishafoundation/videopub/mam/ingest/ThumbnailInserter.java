package org.ishafoundation.videopub.mam.ingest;

import java.io.File;
import java.net.URL;
import java.util.Base64;

import org.apache.commons.io.FileUtils;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.ishafoundation.dwaraapi.utils.HttpClientUtil;
import org.ishafoundation.dwaraapi.utils.JsonPathUtil;
import org.ishafoundation.videopub.mam.core.CatDVInteractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ThumbnailInserter extends CatDVInteractor {

	Logger logger = LoggerFactory.getLogger(ThumbnailInserter.class);
	
	// http://localhost:8080/catdv/api/8/sourcemedia/15/thumbnails?jsessionid=A214B210360509D08D05624969D55B3F - POST
	public String insertThumbnail(String jsessionId, Integer sourceMediaId, String imgPath) throws Exception {
		//String endpointUrl = getURLPrefix() + "/catdv/api/8/sourcemedia/"+ sourceMediaId + "/thumbnails?jsessionid=" + jsessionId;
		String endpointUrl = getURLPrefix() + "/catdv/api/8/sourcemedia/"+ sourceMediaId + "/thumbnails";

		BasicClientCookie cookie = getCookie(jsessionId);

		String postBodyPayload = framePayloadFromTemplate(imgPath);

		String response = null;
		try {
			//response = HttpClientUtil.postIt(endpointUrl, null, postBodyPayload);
			response = HttpClientUtil.postItWithCookie(cookie, endpointUrl, null, postBodyPayload);
		} catch (Exception e) {
			String errorMsg = "Unable to insert thumbnail " + endpointUrl + " :: "  + e.getMessage();
			logger.error(errorMsg, e);
			throw new Exception(errorMsg);
		}

		return response;
	}

	private String framePayloadFromTemplate(String imgPath) {
		URL fileUrl = getClass().getResource("/catdv/InsertThumbnailPayloadTemplate.json");
		File  templateFile = new File(fileUrl.getFile());
		String jsonDataSourceString = null;
		try {
			jsonDataSourceString = FileUtils.readFileToString(templateFile, "UTF-8");

			byte[] imgFileContent = FileUtils.readFileToByteArray(new File(imgPath));

			String urlBase64EncodedImgData = Base64.getUrlEncoder().withoutPadding().encodeToString(imgFileContent);
			
			jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "image", urlBase64EncodedImgData);

		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}
		return jsonDataSourceString;
	}

}
