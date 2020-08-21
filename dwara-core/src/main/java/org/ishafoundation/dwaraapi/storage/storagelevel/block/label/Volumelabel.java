package org.ishafoundation.dwaraapi.storage.storagelevel.block.label;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "volumelabel")
public class Volumelabel {
	@JacksonXmlProperty(isAttribute=true)
	private Double version;
	private String volume;
	private String volumegroup;
	private int blocksize;
	private String owner;
	private String initializedAt;
	private Archiveformat archiveformat;
	private String checksumalgorithm;
	private String encryptionalgorithm;
	private String systeminfo;
	private String creator;
	
	
	public Double getVersion() {
		return version;
	}
	public void setVersion(Double version) {
		this.version = version;
	}
	public String getVolume() {
		return volume;
	}
	public void setVolume(String volume) {
		this.volume = volume;
	}
	public String getVolumegroup() {
		return volumegroup;
	}
	public void setVolumegroup(String volumegroup) {
		this.volumegroup = volumegroup;
	}
	public int getBlocksize() {
		return blocksize;
	}
	public void setBlocksize(int blocksize) {
		this.blocksize = blocksize;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getInitializedAt() {
		return initializedAt;
	}
	public void setInitializedAt(String initializedAt) {
		this.initializedAt = initializedAt;
	}
	public Archiveformat getArchiveformat() {
		return archiveformat;
	}
	public void setArchiveformat(Archiveformat archiveformat) {
		this.archiveformat = archiveformat;
	}
	public String getChecksumalgorithm() {
		return checksumalgorithm;
	}
	public void setChecksumalgorithm(String checksumalgorithm) {
		this.checksumalgorithm = checksumalgorithm;
	}
	public String getEncryptionalgorithm() {
		return encryptionalgorithm;
	}
	public void setEncryptionalgorithm(String encryptionalgorithm) {
		this.encryptionalgorithm = encryptionalgorithm;
	}
	public String getSysteminfo() {
		return systeminfo;
	}
	public void setSysteminfo(String systeminfo) {
		this.systeminfo = systeminfo;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
}