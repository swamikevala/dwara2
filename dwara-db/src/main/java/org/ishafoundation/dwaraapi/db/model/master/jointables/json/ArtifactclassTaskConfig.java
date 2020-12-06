package org.ishafoundation.dwaraapi.db.model.master.jointables.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArtifactclassTaskConfig {

	@JsonProperty("pathname_regex")
	private String pathnameRegex;
	
	@JsonProperty("create_held_jobs")
	private boolean createHeldJobs;

	public String getPathnameRegex() {
		return pathnameRegex;
	}

	public void setPathnameRegex(String pathnameRegex) {
		this.pathnameRegex = pathnameRegex;
	}

	public boolean isCreateHeldJobs() {
		return createHeldJobs;
	}

	public void setCreateHeldJobs(boolean createHeldJobs) {
		this.createHeldJobs = createHeldJobs;
	}
}
