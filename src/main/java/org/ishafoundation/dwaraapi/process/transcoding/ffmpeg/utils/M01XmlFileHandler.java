package org.ishafoundation.dwaraapi.process.transcoding.ffmpeg.utils;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

@Component
public class M01XmlFileHandler {
	
	public static final String TIMECODE_XPATH = "/NonRealTimeMeta/LtcChangeTable/LtcChange[@status = 'increment']/@value";
	
	public String getTimeCode(File fXmlFile) throws Throwable{
		String value = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

            // Create XPathFactory object
            XPathFactory xpathFactory = XPathFactory.newInstance();

            // Create XPath object
            XPath xpath = xpathFactory.newXPath();

            XPathExpression expr = xpath.compile(TIMECODE_XPATH);
            value = (String) expr.evaluate(doc, XPathConstants.STRING);
		} catch (Exception e) {
			throw new Exception("TODO Handle this - " + e);
		}
		return value;
	}
}
