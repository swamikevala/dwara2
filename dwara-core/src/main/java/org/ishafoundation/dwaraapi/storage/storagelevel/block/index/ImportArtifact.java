package org.ishafoundation.dwaraapi.storage.storagelevel.block.index;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class ImportArtifact extends Artifact {
	@JacksonXmlProperty(isAttribute = true, localName="ignore")
	private Boolean ignoreImport;

	public Boolean getIgnoreImport() {
		return ignoreImport;
	}

	public void setIgnoreImport(Boolean ignoreImport) {
		this.ignoreImport = ignoreImport;
	}
}
