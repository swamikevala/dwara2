package org.ishafoundation.dwaraapi.api.req._import;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportRequest {
	
	private String stagingDir; // used in bulk import - /data/dwara/import-staging

	private String xmlPathname; // used in single import -  /data/dwara/import-staging/todo/C16007L6.xml

	
	public String getStagingDir() {
		return stagingDir;
	}

	public void setStagingDir(String stagingDir) {
		this.stagingDir = stagingDir;
	}
	
	public String getXmlPathname() {
		return xmlPathname;
	}

	public void setXmlPathname(String xmlPathname) {
		this.xmlPathname = xmlPathname;
	}
}
