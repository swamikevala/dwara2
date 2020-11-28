package org.ishafoundation.dwaraapi.process.request;

public class Artifact {

	private Integer id;
	
	private String name;
		
	private Artifactclass artifactclass;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Artifactclass getArtifactclass() {
		return artifactclass;
	}
	public void setArtifactclass(Artifactclass artifactclass) {
		this.artifactclass = artifactclass;
	}
}
