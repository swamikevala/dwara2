package org.ishafoundation.dwaraapi.storage.storagelevel.block.index;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "VolumeIndex")
public class Volumeindex {
//	@JacksonXmlProperty(isAttribute = true)
//	private String xmlns;
//	private Importinfo importinfo;
	@JacksonXmlProperty(localName="VolumeInfo")
	private Volumeinfo volumeinfo;
	@JacksonXmlProperty(localName="Artifact")
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<Artifact> artifact;
	@JacksonXmlProperty(localName="FinalizedAt")
	private String finalizedAt;
	
//	public String getXmlns() {
//		return xmlns;
//	}
//	public void setXmlns(String xmlns) {
//		this.xmlns = xmlns;
//	}
	//	public Importinfo getImportinfo() {
//		return importinfo;
//	}
//	public void setImportinfo(Importinfo importinfo) {
//		this.importinfo = importinfo;
//	}
	public Volumeinfo getVolumeinfo() {
		return volumeinfo;
	}
	public void setVolumeinfo(Volumeinfo volumeinfo) {
		this.volumeinfo = volumeinfo;
	}
	public List<Artifact> getArtifact() {
		return artifact;
	}
	public void setArtifact(List<Artifact> artifact) {
		this.artifact = artifact;
	}
	public String getFinalizedAt() {
		return finalizedAt;
	}
	public void setFinalizedAt(String finalizedAt) {
		this.finalizedAt = finalizedAt;
	}
}
