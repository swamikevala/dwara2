package org.ishafoundation.dwaraapi.api.resp.artifactclass;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ArtifactclassResponse {

	private String id;
	private String name;
	private int domain;
	private boolean source;
	private int displayOrder;
	List<ComplexAction> complexActions = new ArrayList<ComplexAction>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getDomain() {
		return domain;
	}
	public void setDomain(int domain) {
		this.domain = domain;
	}
	public boolean isSource() {
		return source;
	}
	public void setSource(boolean source) {
		this.source = source;
	}
	public int getDisplayOrder() {
		return displayOrder;
	}
	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}
	public List<ComplexAction> getComplexActions() {
		return complexActions;
	}
	public void setComplexActions(List<ComplexAction> complexActions) {
		this.complexActions = complexActions;
	}
}
