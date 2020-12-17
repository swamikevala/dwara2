package org.ishafoundation.dwaraapi.process.request;

public class Artifactclass {
	
	private String id;

	private Boolean source;
	
	private String pathPrefix;
	
	private String domain;
	
	private String category;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Boolean getSource() {
		return source;
	}

	public void setSource(Boolean source) {
		this.source = source;
	}

	public String getPathPrefix() {
		return pathPrefix;
	}

	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix = pathPrefix;
	}
	
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}
