package org.ishafoundation.dwaraapi.db.model.master.jointables.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Taskconfig {


	// Jobs are created with status = ‘on_hold’
	@JsonProperty("create_held_jobs")
	private boolean createHeldJobs;

	// Only files with matching pathnames are processed - used in processing tasks
	@JsonProperty("pathname_regex")
	private String pathnameRegex;
	
	// Location to create processed output file (relative to artifact root)
	@JsonProperty("output_path")
	private String outputPath;
	
	//Task is excluded if specified conditions are met
	@JsonProperty("exclude_if")
	private IncludeExcludeProperties excludeIf; 
	
	//Task is included if specified conditions are met
	@JsonProperty("include_if")
	private IncludeExcludeProperties includeIf;
	
	@JsonProperty("destination_id")
	private String destinationId;
	
	public boolean isCreateHeldJobs() {
		return createHeldJobs;
	}

	public void setCreateHeldJobs(boolean createHeldJobs) {
		this.createHeldJobs = createHeldJobs;
	}

	public String getPathnameRegex() {
		return pathnameRegex;
	}

	public void setPathnameRegex(String pathnameRegex) {
		this.pathnameRegex = pathnameRegex;
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
	
	public IncludeExcludeProperties getExcludeIf() {
		return excludeIf;
	}

	public void setExcludeIf(IncludeExcludeProperties excludeIf) {
		this.excludeIf = excludeIf;
	}

	public IncludeExcludeProperties getIncludeIf() {
		return includeIf;
	}

	public void setIncludeIf(IncludeExcludeProperties includeIf) {
		this.includeIf = includeIf;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public class IncludeExcludeProperties {

		// Artifact has this tag
		@JsonProperty("tag")
		private String tag; // TODO : multiple tags needed? we can evolve as needed

		//Artifact class matches this regex
		@JsonProperty("artifactclass_regex")
		private String artifactclassRegex;

		public String getTag() {
			return tag;
		}

		public void setTag(String tag) {
			this.tag = tag;
		}

		public String getArtifactclassRegex() {
			return artifactclassRegex;
		}

		public void setArtifactclassRegex(String artifactclassRegex) {
			this.artifactclassRegex = artifactclassRegex;
		}
	}
}
