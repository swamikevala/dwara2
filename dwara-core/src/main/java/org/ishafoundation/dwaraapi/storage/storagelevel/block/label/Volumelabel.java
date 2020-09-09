package org.ishafoundation.dwaraapi.storage.storagelevel.block.label;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName="VolumeLabel")
public class Volumelabel {
	@JacksonXmlProperty(isAttribute=true)
	private Double version;
	@JacksonXmlProperty(localName="Volume")
	private String volume;
	@JacksonXmlProperty(localName="VolumeGroup")
	private String volumegroup;
	@JacksonXmlProperty(localName="BlockSize")
	private int blocksize;
	@JacksonXmlProperty(localName="Owner")
	private String owner;
	@JacksonXmlProperty(localName="InitializedAt")
	private String initializedAt;
	@JacksonXmlProperty(localName="ArchiveFormat")
	private String archiveformat;
	@JacksonXmlProperty(localName="ArchiveCreator")
	private ArchiveCreator archiveCreator;
	@JacksonXmlProperty(localName="ChecksumAlgorithm")
	private String checksumalgorithm;
	@JacksonXmlProperty(localName="EncryptionAlgorithm")
	private String encryptionalgorithm;
	@JacksonXmlProperty(localName="OperatingSystem")
	private OperatingSystem operatingSystem;
	
	
	
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
	public String getArchiveformat() {
		return archiveformat;
	}
	public void setArchiveformat(String archiveformat) {
		this.archiveformat = archiveformat;
	}
	public ArchiveCreator getArchiveCreator() {
		return archiveCreator;
	}
	public void setArchiveCreator(ArchiveCreator archiveCreator) {
		this.archiveCreator = archiveCreator;
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
	public OperatingSystem getOperatingSystem() {
		return operatingSystem;
	}
	public void setOperatingSystem(OperatingSystem operatingSystem) {
		this.operatingSystem = operatingSystem;
	}
}