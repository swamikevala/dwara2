package org.ishafoundation.dwaraapi.db.model.master.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArtifactclassConfig {

	@JsonProperty("pathname_regex")
	private String pathnameRegex;

	public String getPathnameRegex() {
		return pathnameRegex;
	}

	public void setPathnameRegex(String pathnameRegex) {
		this.pathnameRegex = pathnameRegex;
	}
}
