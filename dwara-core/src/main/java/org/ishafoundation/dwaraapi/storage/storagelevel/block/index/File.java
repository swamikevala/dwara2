package org.ishafoundation.dwaraapi.storage.storagelevel.block.index;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(localName="File")
public class File {
	
	@JacksonXmlProperty(isAttribute=true, localName="volumeBlock")
	private int volumeblock; // volume start block
	@JacksonXmlProperty(isAttribute=true, localName="archiveBlock")
	private int archiveblock; // archive start block
	@JacksonXmlProperty(isAttribute=true, localName="size") 
	private Long size;
	@JacksonXmlProperty(isAttribute=true, localName="checksum")
	private String checksum;
	@JacksonXmlProperty(isAttribute=true, localName="encrypted")
	private Boolean encrypted;
	@JacksonXmlText
	private String name;
	
	
	public int getVolumeblock() {
		return volumeblock;
	}
	public void setVolumeblock(int volumeblock) {
		this.volumeblock = volumeblock;
	}
	public int getArchiveblock() {
		return archiveblock;
	}
	public void setArchiveblock(int archiveblock) {
		this.archiveblock = archiveblock;
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
	public Boolean getEncrypted() {
		return encrypted;
	}
	public void setEncrypted(Boolean encrypted) {
		this.encrypted = encrypted;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
