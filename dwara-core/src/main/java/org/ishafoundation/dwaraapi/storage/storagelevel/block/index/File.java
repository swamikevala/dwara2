package org.ishafoundation.dwaraapi.storage.storagelevel.block.index;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class File {
	// TODO Volume block???
	@JacksonXmlProperty(isAttribute=true)
	private int startblock; // archive start block
	@JacksonXmlProperty(isAttribute=true) 
	private Long size;
	@JacksonXmlProperty(isAttribute=true)
	private String checksum;
	@JacksonXmlProperty(isAttribute=true)
	private boolean encrypted;
	@JacksonXmlText
	private String name;
	
	
	public int getStartblock() {
		return startblock;
	}
	public void setStartblock(int startblock) {
		this.startblock = startblock;
	}
	public Long getSize() {
		return size;
	}
	public void setSize(Long size) {
		this.size = size;
	}
	public String getChecksum() {
		return checksum;
	}
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}
	public boolean getEncrypted() {
		return encrypted;
	}
	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
