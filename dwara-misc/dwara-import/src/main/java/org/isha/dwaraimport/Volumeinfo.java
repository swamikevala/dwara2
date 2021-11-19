package org.isha.dwaraimport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

@JacksonXmlRootElement(localName="VolumeInfo")
@Setter
@Getter
public class Volumeinfo {
	@JacksonXmlProperty(localName="VolumeUid")
	private String volumeuid;
	@JacksonXmlProperty(localName="VolumeBlocksize")
	private String volumeblocksize;
	@JacksonXmlProperty(localName="ArchiveFormat")
	private String archiveformat;
	@JacksonXmlProperty(localName="ArchiveBlocksize")
	private String archiveblocksize;
	@JacksonXmlProperty(localName="ChecksumAlgorithm")
	private String checksumalgorithm;
	@JacksonXmlProperty(localName="EncryptionAlgorithm")
	private String encryptionalgorithm;
	@JsonInclude(JsonInclude.Include.NON_EMPTY) 
	@JacksonXmlProperty(localName="ArtifactclassUid")
	private String artifactclassuid;
	@JacksonXmlProperty(localName="FinalizedAt")
	private String finalizedAt;	 

}

