package org.ishafoundation.dwaraapi.db.model.transactional.json;

public class SubrequestDetails {
	
	// format
	private String volume_uid;
	
//	private Integer volumetype_id; should be group volume uid
	
	private Boolean force;
	
	// ingest stuff
	private Integer artifactclass_id;
	
	private String sourcepath;
	
	// TODO not needed for ingest may be for rename or delete - private Integer artifact_id;
	
	private String artifact_name;

	private String prev_sequence_code;

	private Boolean rerun;

	private Integer rerun_no;

	private Integer[] skip_storagetasks;

	private Integer[] skip_processingtasks;
	
	// restore stuff
	private Integer file_id;
	
	private Integer priority;
	
	private Integer location_id; // specifies which location(copy) to retrieve the data from.
	
	private String destinationpath;
	
	private String output_folder;
	
	private Boolean verify; // overwrites whatever is configured in archiveformat.restore_verify = true
	

	/********************  FORMAT  ********************/


	public String getVolume_uid() {
		return volume_uid;
	}

	public void setVolume_uid(String volume_uid) {
		this.volume_uid = volume_uid;
	}

//	public Integer getVolumetype_id() {
//		return volumetype_id;
//	}
//
//	public void setVolumetype_id(Integer volumetype_id) {
//		this.volumetype_id = volumetype_id;
//	}

	public Boolean getForce() {
		return force;
	}

	public void setForce(Boolean force) {
		this.force = force;
	}

	/********************  INGEST  ********************/
	
	public Integer getArtifactclass_id() {
		return artifactclass_id;
	}

	public void setArtifactclass_id(Integer artifactclass_id) {
		this.artifactclass_id = artifactclass_id;
	}

	public String getSourcepath() {
		return sourcepath;
	}

	public void setSourcepath(String sourcepath) {
		this.sourcepath = sourcepath;
	}

	public String getArtifact_name() {
		return artifact_name;
	}

	public void setArtifact_name(String artifact_name) {
		this.artifact_name = artifact_name;
	}

	public String getPrev_sequence_code() {
		return prev_sequence_code;
	}

	public void setPrev_sequence_code(String prev_sequence_code) {
		this.prev_sequence_code = prev_sequence_code;
	}

	public Boolean getRerun() {
		return rerun;
	}

	public void setRerun(Boolean rerun) {
		this.rerun = rerun;
	}

	public Integer getRerun_no() {
		return rerun_no;
	}

	public void setRerun_no(Integer rerun_no) {
		this.rerun_no = rerun_no;
	}

	public Integer[] getSkip_storagetasks() {
		return skip_storagetasks;
	}

	public void setSkip_storagetasks(Integer[] skip_storagetasks) {
		this.skip_storagetasks = skip_storagetasks;
	}

	public Integer[] getSkip_processingtasks() {
		return skip_processingtasks;
	}

	public void setSkip_processingtasks(Integer[] skip_processingtasks) {
		this.skip_processingtasks = skip_processingtasks;
	}

	/********************  RESTORE  ********************/
	
	public Integer getFile_id() {
		return file_id;
	}

	public void setFile_id(Integer file_id) {
		this.file_id = file_id;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Integer getLocation_id() {
		return location_id;
	}

	public void setLocation_id(Integer location_id) {
		this.location_id = location_id;
	}

	public String getDestinationpath() {
		return destinationpath;
	}

	public void setDestinationpath(String destinationpath) {
		this.destinationpath = destinationpath;
	}

	public String getOutput_folder() {
		return output_folder;
	}

	public void setOutput_folder(String output_folder) {
		this.output_folder = output_folder;
	}

	public Boolean getVerify() {
		return verify;
	}

	public void setVerify(Boolean verify) {
		this.verify = verify;
	}

	
	
	
	/*
	public Integer getArtifact_id() {
		return artifact_id;
	}

	public void setArtifact_id(Integer artifact_id) {
		this.artifact_id = artifact_id;
	}
	*/
}
