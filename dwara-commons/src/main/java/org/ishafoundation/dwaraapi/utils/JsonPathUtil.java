package org.ishafoundation.dwaraapi.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;

public class JsonPathUtil {
	
	DocumentContext jsonContext = null;
			
	public JsonPathUtil(String jsonDataSourceString) {
		this.jsonContext = JsonPath.parse(jsonDataSourceString);
	}
	
	public String getValue(String jsonPath){
		String value = jsonContext.read(jsonPath);
		
		return value;		
	}
	
	public static String getValue(String jsonDataSourceString, String jsonPath){
		DocumentContext jsonContext = JsonPath.parse(jsonDataSourceString);
		String value = jsonContext.read(jsonPath).toString();
		
		return value;
	}
	
	public static List<Map<String, Object>> getArray(String jsonDataSourceString, String jsonPath, Filter... pathFilter){
		DocumentContext jsonContext = JsonPath.parse(jsonDataSourceString);
		List<Map<String, Object>> value  = jsonContext.read(jsonPath, pathFilter);

		return value;
	}
	
	public static Integer getInteger(String jsonDataSourceString, String jsonPath){
		DocumentContext jsonContext = JsonPath.parse(jsonDataSourceString);
		Integer value = jsonContext.read(jsonPath);
		
		return value;
	}
	
	public static String putEntry(String jsonDataSourceString, String jsonPath, String newKey, Object newValue){
		DocumentContext jsonContext = JsonPath.parse(jsonDataSourceString);
		jsonContext.put(jsonPath, newKey, newValue);
		
		return jsonContext.jsonString();
	}	
	
	public static String setValue(String jsonDataSourceString, String jsonPath, Object newValue){
		DocumentContext jsonContext = JsonPath.parse(jsonDataSourceString);
		jsonContext.set(jsonPath, newValue);
		
		return jsonContext.jsonString();
	}
	
	public static void main(String[] args) throws IOException {
		String response = FileUtils.readFileToString(new File("C:\\Users\\prakash\\projects\\videoarchives\\catalog.json"));
		List<Map<String, Object>> catalogs = JsonPathUtil.getArray(response , "data");
		if(catalogs.size() > 0) {
			Map<String, Object> catalog = catalogs.get(0);
			Integer catalogId = (Integer) catalog.get("ID");	
			System.out.println(catalogId);
		}
		
		String input = FileUtils.readFileToString(new File("C:\\Users\\prakash\\projects\\videoarchives\\ffmpeg_ffprobe-meta_VIVAH.json"));
		List<Map<String, Object>> videoStreamBlock = JsonPathUtil.getArray(input , "streams[?(@.codec_type=='video')]");
		String videoFormat = (String) videoStreamBlock.get(0).get("codec_name");
		
		System.out.println(getValue(input, "format.filename"));
		System.out.println(getInteger(input, "format.probe_score"));
		setValue(input, "abc.cde", "25");
		
		String jsonStr = FileUtils.readFileToString(new File("C:\\Users\\prakash\\src-code\\dwara-api\\src\\data\\seed\\contentgroup.addAll.json"));
		Filter contGrpNameFilter = Filter.filter(Criteria.where("contentGroupName").eq("public-video"));
		List<Map<String, Object>> contentGroup_Block = JsonPathUtil.getArray(jsonStr, "[?]", contGrpNameFilter);
		System.out.println(contentGroup_Block.get(0).get("contentGroupId"));
		
		String jsonStr0 = FileUtils.readFileToString(new File("C:\\Users\\prakash\\projects\\videoarchives\\drastic\\xmp.json"));
		
		List<Map<String, Object>> general_TrackType_Block = JsonPathUtil.getArray(jsonStr0, "x:xmpmeta.rdf:RDF.rdf:Description[?(@.xmlns:dt=='http://www.drastictech.com/metadata/dtmes-xml-dtd.dtd')].dt:tracks.rdf:Bag.rdf:li[?(@.dt:Track_Type=='General')]");
		String fileSize = (String) general_TrackType_Block.get(0).get("dt:File_size");
		System.out.println(fileSize);
		
		Filter drastic_Description_Filter = Filter.filter(Criteria.where("xmlns:dt").eq("http://www.drastictech.com/metadata/dtmes-xml-dtd.dtd"));
		List<Map<String, Object>> general_TrackType_Block_2 = JsonPathUtil.getArray(jsonStr0, "x:xmpmeta.rdf:RDF.rdf:Description[?].dt:tracks.rdf:Bag.rdf:li[?(@.dt:Track_Type=='General')]", drastic_Description_Filter);
		String fileSize2 = (String) general_TrackType_Block_2.get(0).get("dt:UniversalLocator");
		System.out.println(fileSize2);
		
		Filter general_TrackType_Filter = Filter.filter(Criteria.where("dt:Track_Type").eq("General"));
		Filter[] abcde = {drastic_Description_Filter, general_TrackType_Filter};
		List<Map<String, Object>> general_TrackType_Block_3 = JsonPathUtil.getArray(jsonStr0, "x:xmpmeta.rdf:RDF.rdf:Description[?].dt:tracks.rdf:Bag.rdf:li[?]", abcde);
		String fileSize3 = (String) general_TrackType_Block_3.get(0).get("dt:Duration");
		System.out.println(fileSize3);	
		/*
		String jsonStr1 = FileUtils.readFileToString(new File("C:\\Prakash\\Isha\\MAM\\drastic\\Test1.txt"));
		System.out.println("in " + getValue(jsonStr1, "request.result.value"));
		
		String jsonStr2 = FileUtils.readFileToString(new File("C:\\Prakash\\Isha\\MAM\\drastic\\Test2.txt"));
		System.out.println("in " + getValue(jsonStr2, "getCopyInOut.copyInOutInfo.in.value"));
		System.out.println("out " + getValue(jsonStr2, "getCopyInOut.copyInOutInfo.out.value"));
		System.out.println("tempFile " + getValue(jsonStr2, "getCopyInOut.copyInOutInfo.tempFile.value"));
		 */
	}

}
