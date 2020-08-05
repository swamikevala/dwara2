package org.ishafoundation.dwaraapi.storage.format;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Volumeindex;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XMLTest {

	public static void main(String[] args) throws Exception {
	    XmlMapper xmlMapper = new XmlMapper();
	    xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
	    URL fileUrl = XMLTest.class.getResource("/test-samples/index-test.xml");
	    String xml = FileUtils.readFileToString(new File(fileUrl.getFile()));
	    Volumeindex vi = xmlMapper.readValue(xml, Volumeindex.class);

//	    System.out.println(" - " + vi.getImportinfo().getVolumesetuid());
//	    vi.getImportinfo().setVolumesetuid("some changed value");
	    String xmlFromJava = xmlMapper.writeValueAsString(vi);
	    System.out.println(xmlFromJava);
	    /*
	    URL fileUrl = XMLTest.class.getResource("/test-samples/format-test.xml");
	    String xml = FileUtils.readFileToString(new File(fileUrl.getFile()));
	    Volumelabel vl = xmlMapper.readValue(xml, Volumelabel.class);
	    System.out.println(" - " + vl.getVersion());
	    System.out.println(" - " + vl.getChecksumalgorithm());
	    vl.setChecksumalgorithm("someotheralgo");
	    String xmlFromJava = xmlMapper.writeValueAsString(vl);
	    System.out.println(xmlFromJava);
	    */
	    
	    
	}
}
