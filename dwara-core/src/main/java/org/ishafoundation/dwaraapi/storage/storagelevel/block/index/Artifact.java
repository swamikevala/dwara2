package org.ishafoundation.dwaraapi.storage.storagelevel.block.index;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Artifact {
	
	@JacksonXmlProperty(isAttribute=true)
	private int startblock; // archive start block
	@JacksonXmlProperty(isAttribute=true)
	private int endblock; // archive end block
	@JacksonXmlProperty(isAttribute = true)
	private String artifactclassuid;
	@JacksonXmlProperty(isAttribute = true)
	private String sequencecode;
	@JacksonXmlProperty(isAttribute = true)
	private String rename;
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<File> file;
	

	public int getStartblock() {
		return startblock;
	}

	public void setStartblock(int startblock) {
		this.startblock = startblock;
	}

	public int getEndblock() {
		return endblock;
	}

	public void setEndblock(int endblock) {
		this.endblock = endblock;
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