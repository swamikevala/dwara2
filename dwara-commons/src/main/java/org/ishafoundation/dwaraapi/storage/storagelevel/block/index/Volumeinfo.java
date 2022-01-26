package org.ishafoundation.dwaraapi.storage.storagelevel.block.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName="VolumeInfo")
public class Volumeinfo {
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JacksonXmlProperty(localName="VolumeUuid")
	private String volumeuuid;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JacksonXmlProperty(localName="VolumeUid")
	private String volumeuid;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JacksonXmlProperty(localName="Volume")
	private String volume;
	@JacksonXmlProperty(localName="VolumeBlocksize")
	private int volumeblocksize;
	@JacksonXmlProperty(localName="ArchiveFormat")
	private String archiveformat;
	@JacksonXmlProperty(localName="ArchiveBlocksize")
	private int archiveblocksize;
	@JacksonXmlProperty(localName="ChecksumAlgorithm")
	private String checksumalgorithm;
	@JacksonXmlProperty(localName="EncryptionAlgorithm")
	private String encryptionalgorithm;
	@JsonInclude(JsonInclude.Include.NON_EMPTY) 
	@JacksonXmlProperty(localName="ArtifactclassUid")
	private String artifactclassuid;
	@JsonInclude(JsonInclude.Include.NON_EMPTY) 
	@JacksonXmlProperty(localName="ArtifactclassId")
	private String artifactclassid;	
	@JacksonXmlProperty(localName="FinalizedAt")
	private String finalizedAt;	 
	@JacksonXmlProperty(localName="FirstWrittenAt")
	private String firstWrittenAt;	 
	@JacksonXmlProperty(localName="LastWrittenAt")
	private String lastWrittenAt;	 
	
	
	public String getVolumeuuid() {
		return volumeuuid;
	}
	public void setVolumeuuid(String volumeuuid) {
		this.volumeuuid = volumeuuid;
	}
	public String getVolumeuid() {
		return volumeuid;
	}
	public void setVolumeuid(String volumeuid) {
		this.volumeuid = volumeuid;
	}
	public String getVolume() {
		return volume;
	}
	public void setVolume(String volume) {
		this.volume = volume;
	}
	public int getVolumeblocksize() {
		return volumeblocksize;
	}
	public void setVolumeblocksize(int volumeblocksize) {
		this.volumeblocksize = volumeblocksize;
	}
	public String getArchiveformat() {
		return archiveformat;
	}
	public void setArchiveformat(String archiveformat) {
		this.archiveformat = archiveformat;
	}
	public int getArchiveblocksize() {
		return archiveblocksize;
	}
	public void setArchiveblocksize(int archiveblocksize) {
		this.archiveblocksize = archiveblocksize;
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
	public String getArtifactclassuid() {
		return artifactclassuid;
	}
	public void setArtifactclassuid(String artifactclassuid) {
		this.artifactclassuid = artifactclassuid;
	}
	public String getArtifactclassid() {
		return artifactclassid;
	}
	public void setArtifactclassid(String artifactclassid) {
		this.artifactclassid = artifactclassid;
	}
	public String getFinalizedAt() {
		return finalizedAt;
	}
	public void setFinalizedAt(String finalizedAt) {
		this.finalizedAt = finalizedAt;
	}
	public String getFirstWrittenAt() {
		return firstWrittenAt;
	}
	public void setFirstWrittenAt(String firstWrittenAt) {
		this.firstWrittenAt = firstWrittenAt;
	}
	public String getLastWrittenAt() {
		return lastWrittenAt;
	}
	public void setLastWrittenAt(String lastWrittenAt) {
		this.lastWrittenAt = lastWrittenAt;
	}
}

