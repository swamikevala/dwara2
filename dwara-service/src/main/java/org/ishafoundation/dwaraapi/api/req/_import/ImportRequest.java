package org.ishafoundation.dwaraapi.api.req._import;

public class ImportRequest {
	
	private String xmlPathname; // /data/dwara/import-staging/todo/C16007L6.xml
	private Boolean forceMatch; // overrides the sequence.forceMatch DB Config...

	public String getXmlPathname() {
		return xmlPathname;
	}

	public void setXmlPathname(String xmlPathname) {
		this.xmlPathname = xmlPathname;
	}

	public Boolean getForceMatch() {
		return forceMatch;
	}

	public void setForceMatch(Boolean forceMatch) {
		this.forceMatch = forceMatch;
	}
}
