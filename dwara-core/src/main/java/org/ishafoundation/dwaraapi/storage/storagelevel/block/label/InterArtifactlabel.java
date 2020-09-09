package org.ishafoundation.dwaraapi.storage.storagelevel.block.label;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName="ArtifactLabel")
public class InterArtifactlabel {
	
	@JacksonXmlProperty(isAttribute=true)
	private Double version;
	
	@JacksonXmlProperty(localName="Artifact")
	private String artifact;
	
	@JacksonXmlProperty(localName="SequenceCode")
	private String sequenceCode;
	
	@JacksonXmlProperty(localName="VolumeUuid")
	private String volumeUuid;
	
	@JacksonXmlProperty(localName="WrittenAt")
	private String writtenAt;
	
	@JacksonXmlProperty(localName="FileCount")
	private int fileCount;
	
	@JacksonXmlProperty(localName="TotalSize")
	private long totalSize;
	
	@JacksonXmlProperty(localName="BlockSize")
	private int blocksize;
	
	@JacksonXmlProperty(localName="Blocks")
	private Blocks blocks;

	public Double getVersion() {
		return version;
	}

	public void setVersion(Double version) {
		this.version = version;
	}

	public String getArtifact() {
		return artifact;
	}

	public void setArtifact(String artifact) {
		this.artifact = artifact;
	}

	public String getSequenceCode() {
		return sequenceCode;
	}

	public void setSequenceCode(String sequenceCode) {
		this.sequenceCode = sequenceCode;
	}

	public String getVolumeUuid() {
		return volumeUuid;
	}

	public void setVolumeUuid(String volumeUuid) {
		this.volumeUuid = volumeUuid;
	}

	public String getWrittenAt() {
		return writtenAt;
	}

	public void setWrittenAt(String writtenAt) {
		this.writtenAt = writtenAt;
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public long getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	public int getBlocksize() {
		return blocksize;
	}

	public void setBlocksize(int blocksize) {
		this.blocksize = blocksize;
	}

	public Blocks getBlocks() {
		return blocks;
	}

	public void setBlocks(Blocks blocks) {
		this.blocks = blocks;
	}
}