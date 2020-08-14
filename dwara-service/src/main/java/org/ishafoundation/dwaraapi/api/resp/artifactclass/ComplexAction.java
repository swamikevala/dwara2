package org.ishafoundation.dwaraapi.api.resp.artifactclass;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ComplexAction {
	private String action;
	private List<Actionelement> elements = new ArrayList<Actionelement> ();
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public List<Actionelement> getElements() {
		return elements;
	}
	public void setElements(List<Actionelement> elements) {
		this.elements = elements;
	}
}
