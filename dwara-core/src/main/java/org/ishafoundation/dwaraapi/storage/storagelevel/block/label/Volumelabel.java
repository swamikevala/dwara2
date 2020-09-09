package org.ishafoundation.dwaraapi.storage.storagelevel.block.label;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName="VolumeLabel")
public class Volumelabel {
	@JacksonXmlProperty(isAttribute=true)
	private Double version;
	@JacksonXmlProperty(localName="Uuid")
	private String uuid;
	@JacksonXmlProperty(localName="Barcode")
	private String barcode;
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
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
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