package org.ishafoundation.dwaraapi.storage.storagelevel.block.index;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "volumeindex")
public class Volumeindex {
	@JacksonXmlProperty(isAttribute = true)
	private String version;
	private Importinfo importinfo;
	private Volumeinfo volumeinfo;
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<Artifact> artifact;

	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public Importinfo getImportinfo() {
		return importinfo;
	}
	public void setImportinfo(Importinfo importinfo) {
		this.importinfo = importinfo;
	}
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
}
