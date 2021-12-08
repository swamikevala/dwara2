package org.ishafoundation.dwaraapi.storage.storagelevel.block.index;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName="Artifact")
public class Artifact {

	@JacksonXmlProperty(isAttribute = true, localName="name")
	private String name;
	@JacksonXmlProperty(isAttribute=true, localName="startBlock")
	private int startblock; // archive start block
	@JacksonXmlProperty(isAttribute=true, localName="endBlock")
	private int endblock; // archive end block
	@JacksonXmlProperty(isAttribute = true, localName="artifactclassUid")
	private String artifactclassuid;
	@JacksonXmlProperty(isAttribute = true, localName="sequenceCode")
	private String sequencecode;
	@JacksonXmlProperty(isAttribute = true, localName="rename")
	private String rename;
	@JacksonXmlProperty(isAttribute = true, localName="totalSize")
	private Long totalSize;
	@JacksonXmlProperty(localName="File")
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<File> file;
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

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
	
	public Long getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(Long totalSize) {
		this.totalSize = totalSize;
	}

	public List<File> getFile() {
		return file;
	}

	public void setFile(List<File> file) {
		this.file = file;
	}
}