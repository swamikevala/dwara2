package org.ishafoundation.dwaraapi.storage.storagelevel.block.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JacksonXmlRootElement(localName="File")
public class File {
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JacksonXmlProperty(isAttribute=true, localName="volumeBlock")
	private Integer volumeblock; // volume start block
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JacksonXmlProperty(isAttribute=true, localName="volumeStartBlock")
	private Integer volumeStartBlock; // volume start block
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JacksonXmlProperty(isAttribute=true, localName="volumeEndBlock")
	private Integer volumeEndBlock; // volume end block
	@JacksonXmlProperty(isAttribute=true, localName="archiveBlock")
	private Long archiveblock; // archive start block
	@JacksonXmlProperty(isAttribute=true, localName="size") 
	private Long size;
	@JacksonXmlProperty(isAttribute=true, localName="directory")
	private Boolean directory;
	@JacksonXmlProperty(isAttribute=true, localName="checksum")
	private String checksum;
	@JacksonXmlProperty(isAttribute=true, localName="encrypted")
	private Boolean encrypted;
	@JacksonXmlText
	private String name;
	
	public Integer getVolumeblock() {
		return volumeblock;
	}
	public void setVolumeblock(Integer volumeblock) {
		this.volumeblock = volumeblock;
	}
	public Integer getVolumeStartBlock() {
		return volumeStartBlock;
	}
	public void setVolumeStartBlock(Integer volumeStartBlock) {
		this.volumeStartBlock = volumeStartBlock;
	}
	public Integer getVolumeEndBlock() {
		return volumeEndBlock;
	}
	public void setVolumeEndBlock(Integer volumeEndBlock) {
		this.volumeEndBlock = volumeEndBlock;
	}
	public Long getArchiveblock() {
		return archiveblock;
	}
	public void setArchiveblock(Long archiveblock) {
		this.archiveblock = archiveblock;
	}
	public Long getSize() {
		return size;
	}
	public void setSize(Long size) {
		this.size = size;
	}
	public Boolean getDirectory() {
		return directory;
	}
	public void setDirectory(Boolean directory) {
		this.directory = directory;
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
