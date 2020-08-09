package org.ishafoundation.dwaraapi.db.model.transactional.json;

import org.ishafoundation.dwaraapi.enumreferences.Storagesubtype;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestDetails {
	
	private JsonNode body;
	
	// format
	private String volume_id;
	
	private String volume_group_id; // TODO : Schema deviation - Schema to change
	
	private Integer volume_blocksize;

	private Storagesubtype storagesubtype;
	
	private Boolean force;

	// ingest stuff
	private String artifactclass_id;
	
	private String sourcepath;

	
	// TODO not needed for ingest may be for rename or delete - private Integer artifact_id;
	
	private String artifact_name;

	private String prev_sequence_code;

	private Boolean rerun;

	private Integer rerun_no;

	private String[] skip_storagetask_actions;

	private String[] skip_processingtasks;
	
	// restore stuff
	private Integer file_id;
	
	private Integer priority;
	
	private String location_id; // specifies which location(copy) to retrieve the data from.
	
	private String destinationpath;
	
	private String output_folder;
	
	private Boolean verify; // overwrites whatever is configured in archiveformat.restore_verify = true
	
	
	// rewrite stuff
	private Integer artifact_id;// artifact_id or name???
	
	private String from_volume_uid;
	
	private String to_volume_uid;
	

	public JsonNode getBody() {
		return body;
	}

	public void setBody(JsonNode body) {
		this.body = body;
	}

	/********************  FORMAT  ********************/

	public String getVolume_id() {
		return volume_id;
	}

	public void setVolume_id(String volume_id) {
		this.volume_id = volume_id;
	}

	public String getVolume_group_id() {
		return volume_group_id;
	}

	public void setVolume_group_id(String volume_group_id) {
		this.volume_group_id = volume_group_id;
	}

	public Integer getVolume_blocksize() {
		return volume_blocksize;
	}

	public void setVolume_blocksize(Integer volume_blocksize) {
		this.volume_blocksize = volume_blocksize;
	}

	public Storagesubtype getStoragesubtype() {
		return storagesubtype;
	}

	public void setStoragesubtype(Storagesubtype storagesubtype) {
		this.storagesubtype = storagesubtype;
	}

	public Boolean isForce() {
		return force;
	}

	public void setForce(Boolean force) {
		this.force = force;
	}

	/********************  INGEST  ********************/
	
	public String getArtifactclass_id() {
		return artifactclass_id;
	}

	public void setArtifactclass_id(String artifactclass_id) {
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

	public String[] getSkip_storagetask_actions() {
		return skip_storagetask_actions;
	}

	public void setSkip_storagetask_actions(String[] skip_storagetask_actions) {
		this.skip_storagetask_actions = skip_storagetask_actions;
	}

	public String[] getSkip_processingtasks() {
		return skip_processingtasks;
	}

	public void setSkip_processingtasks(String[] skip_processingtasks) {
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

	public String getLocation_id() {
		return location_id;
	}

	public void setLocation_id(String location_id) {
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

	/********************  REWRITE  ********************/
	
	public Integer getArtifact_id() {
		return artifact_id;
	}

	public void setArtifact_id(Integer artifact_id) {
		this.artifact_id = artifact_id;
	}

	public String getFrom_volume_uid() {
		return from_volume_uid;
	}

	public void setFrom_volume_uid(String from_volume_uid) {
		this.from_volume_uid = from_volume_uid;
	}

	public String getTo_volume_uid() {
		return to_volume_uid;
	}

	public void setTo_volume_uid(String to_volume_uid) {
		this.to_volume_uid = to_volume_uid;
	}
	
	// TODO : equals and hashCode
//	@Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Action action = (Action) o;
//        return Objects.equals(id, action.id);
//    }
// 
//    @Override
//    public int hashCode() {
//        return Objects.hash(id);
//    }
}
