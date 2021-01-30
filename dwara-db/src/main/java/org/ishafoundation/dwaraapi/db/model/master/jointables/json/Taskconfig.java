package org.ishafoundation.dwaraapi.db.model.master.jointables.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Taskconfig {

	@JsonProperty("pathname_regex")
	private String pathnameRegex;
	
	@JsonProperty("create_held_jobs")
	private boolean createHeldJobs;

	@JsonProperty("output_path")
	private String outputPath;
	
	@JsonProperty("destination_id")
	private String destinationId;
	
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

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public String getDestinationId() {
		return destinationId;
	}

	public void setDestinationId(String destinationId) {
		this.destinationId = destinationId;
	}
}
