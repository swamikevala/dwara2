package org.ishafoundation.dwaraapi.storage.storagelevel.block.label;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class Archiveformat {
	
	@JacksonXmlProperty(isAttribute=true)
	private Double version;
	@JacksonXmlText
	private String text;
	
	
	public Double getVersion() {
		return version;
	}
	public void setVersion(Double version) {
		this.version = version;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}

