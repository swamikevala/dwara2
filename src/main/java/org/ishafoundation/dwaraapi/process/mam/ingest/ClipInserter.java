package org.ishafoundation.dwaraapi.process.mam.ingest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.ishafoundation.dwaraapi.process.mam.core.CatDVInteractor;
import org.ishafoundation.dwaraapi.utils.HttpClientUtil;
import org.ishafoundation.dwaraapi.utils.JsonPathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class ClipInserter extends CatDVInteractor {
	
	Logger logger = LoggerFactory.getLogger(ClipInserter.class);
	// E.g.,
	// http://35.244.61.125:8080/catdv/api/8/clips?jsessionid=EA590416FFC36EBD993201F97032CD52 - POST
	public String insertClip(String jsessionId, int fileId, int catalogId, String metaDataJson, String highResFilePath) throws Exception {
    	String endpointUrl = getURLPrefix() + "/catdv/api/8/clips";

        BasicClientCookie cookie = getCookie(jsessionId);
        
    	String bodyPayload = frameCreateClipPayloadFromTemplate(fileId, catalogId, metaDataJson, highResFilePath);
    	
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
	
	private String frameCreateClipPayloadFromTemplate(int fileId, int catalogId, String metaDataJson, String highResFilePath) throws Exception {
		
		URL fileUrl = getClass().getResource("/catdv/InsertClipPayloadTemplate.json");
		java.io.File  templateFile = new java.io.File(fileUrl.getFile());
		String jsonDataSourceString = null;
		try {
			jsonDataSourceString = FileUtils.readFileToString(templateFile, "UTF-8");
		} catch (IOException e) {
			String errorMsg = "Unable to read insert clip template " + templateFile.getAbsolutePath() + " :: "  + e.getMessage();
			logger.error(errorMsg, e);
			throw new Exception(errorMsg);
		}

		/*** format section ***/
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
		
		String formatSize = JsonPathUtil.getValue(metaDataJson, "format.size");
		long mediaSize = formatSize != null ? Long.parseLong(formatSize) : 0;
		
		String format = JsonPathUtil.getValue(metaDataJson, "format.format_name");
		

		Date formattedRecordedDate = null;
		long recordedDate = 0;
		try {
			String recordedDateAsString = JsonPathUtil.getValue(metaDataJson, "format.tags.creation_time"); // returns "2019-03-13 09:53:26"
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			
			formattedRecordedDate = sdf.parse(recordedDateAsString);
			recordedDate = formattedRecordedDate.getTime();
		} catch (Exception e) {
			logger.warn("Unable to format recorded Date from the key dt:Encoded_date. Defaulting to null");
		}
				
		
		/*** codec_type = Video section ***/
		int width = 0;
		int height = 0;
		Double aspectRatio = null;
		Float fps = (float) 25;
		int tcFmt = 25;
		Float in =  (float) 0;
		Float out = (float) 0;		
		String catDVStyleVideoFormat = "";
		try {
			List<Map<String, Object>> videoStreamBlock = JsonPathUtil.getArray(metaDataJson , "streams[?(@.codec_type=='video')]");
			Map<String, Object> vsb = videoStreamBlock.get(0);
			
			in = Float.parseFloat((String) vsb.get("start_time"));
			
			Float duration = Float.parseFloat((String) vsb.get("duration"));
			
			out = in + duration;
		//if(vsb != null) {
			String videoFormat = (String) vsb.get("codec_name");
			
			width = (int) vsb.get("width");
			
			height = (int) vsb.get("height");
			
			String display_aspect_ratio = (String) vsb.get("display_aspect_ratio");
			
			if(display_aspect_ratio != null) {
				String[] aspectRatioParts = display_aspect_ratio.split(":");
				DecimalFormat df = new DecimalFormat("#.#######");      
				Double aspectRatioNotRounded = Double.parseDouble(aspectRatioParts[0])/Double.parseDouble(aspectRatioParts[1]);
				aspectRatio = Double.valueOf(df.format(aspectRatioNotRounded));		
			}
	
			
			catDVStyleVideoFormat = videoFormat + " (" + width + "x" + height + " " + fps + ")";
		//}
		}catch (Exception e) {
			logger.warn("Unable to get video stream block meta" + e.getMessage());
		}			
		
		/*** codec_type = Audio section ***/
		String catDVStyleAudioFormat = "";
		try {
			List<Map<String, Object>> audioStreamBlock = JsonPathUtil.getArray(metaDataJson , "streams[?(@.codec_type=='audio')]");
			Map<String, Object> asb = audioStreamBlock.get(0);
			
			if(asb != null) {
				String audioFormatSettings = "";
				String sampleRateAsString = (String) asb.get("sample_rate"); // returns something like "48000"
				String samplingRate = Integer.parseInt(sampleRateAsString)/1000  + " kHz"; // 48 kHz
				Integer bits_per_sample = (Integer) asb.get("bits_per_sample"); // returns something like "24"
				
				String bitDepth = bits_per_sample + " bits";
		
				catDVStyleAudioFormat = audioFormatSettings + " (" + samplingRate + ", " + bitDepth + ")";		
			}	
		}catch (Exception e) {
			logger.warn("Unable to get audio stream block meta");
		}		
		/*** Frame the Payload ***/

		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "catalogID", catalogId);
		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "name", name);
		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "bin", bin);
		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "format", format);
		 
		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "in.fmt", tcFmt);
		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "in.secs", in);
		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "out.fmt", tcFmt);
		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "out.secs", out);

		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "recordedDate", recordedDate);// TODO need customisation
		
		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "catalog.ID", catalogId);
		
		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "media.filePath", highResFilePath);
		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "media.fileSize", mediaSize);// TODO need customisation
		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "media.videoFormat", catDVStyleVideoFormat);
		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "media.audioFormat", catDVStyleAudioFormat);
		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "media.aspectRatio", aspectRatio);// TODO need customisation
		jsonDataSourceString = JsonPathUtil.putEntry(jsonDataSourceString, "media.fields", "dwaraId", fileId);

		jsonDataSourceString = JsonPathUtil.setValue(jsonDataSourceString, "importSource.file", mediaPath);
		
		return jsonDataSourceString;
	}
}
