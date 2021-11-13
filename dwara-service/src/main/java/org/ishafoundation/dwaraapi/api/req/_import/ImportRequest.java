package org.ishafoundation.dwaraapi.api.req._import;

public class ImportRequest {
	
	private String stagingDir; // /data/dwara/import-staging

	private String xmlPathname;

	
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
