package org.ishafoundation.dwaraapi.storage.storagelevel.block.index;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Artifact {
	
	@JacksonXmlProperty(isAttribute = true)
	private int archivenumber; // TODO - Do we need this...
	@JacksonXmlProperty(isAttribute = true)
	private String artifactclassuid;
	@JacksonXmlProperty(isAttribute = true)
	private String sequencecode;
	@JacksonXmlProperty(isAttribute = true)
	private String rename;
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<File> file;
	
	public int getArchivenumber() {
		return archivenumber;
	}

	public void setArchivenumber(int archivenumber) {
		this.archivenumber = archivenumber;
	}

	public String getArtifactclassuid() {
		return artifactclassuid;
	}

	public void setArtifactclassuid(String artifactclassuid) {
		this.artifactclassuid = artifactclassuid;
	}

	public String getSequencecode() {
		return sequencecode;
	}

	public void setSequencecode(String sequencecode) {
		this.sequencecode = sequencecode;
	}

	public String getRename() {
		return rename;
	}

	public void setRename(String rename) {
		this.rename = rename;
	}
	
	public List<File> getFile() {
		return file;
	}

	public void setFile(List<File> file) {
		this.file = file;
	}
}