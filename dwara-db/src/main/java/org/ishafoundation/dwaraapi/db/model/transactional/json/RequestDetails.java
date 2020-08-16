package org.ishafoundation.dwaraapi.db.model.transactional.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestDetails {
	
	private JsonNode body;
	
	// format
	@JsonProperty("volume_id")
	private String volume_id;
	
	@JsonProperty("volume_group_id")
	private String volume_group_id;
	
	@JsonProperty("volume_blocksize")
	private Integer volume_blocksize;

	private String storagesubtype;
	
	private Boolean force;

	// ingest stuff
	@JsonProperty("artifactclass_id")
	private String artifactclassId;
	
	@JsonProperty("staged_filepath")
	private String stagedFilepath;

	// TODO not needed for ingest may be for rename or delete - private Integer artifact_id;
	
	@JsonProperty("staged_filename")
	private String stagedFilename;

	@JsonProperty("prev_sequence_code")
	private String prevSequenceCode;

	@JsonProperty("rerun_no")
	private Integer rerunNo;

	@JsonProperty("skip_actionelements")
	private List<Integer> skipActionelements;

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

	public String getStoragesubtype() {
		return storagesubtype;
	}

	public void setStoragesubtype(String storagesubtype) {
		this.storagesubtype = storagesubtype;
	}

	public Boolean isForce() {
		return force;
	}

	public void setForce(Boolean force) {
		this.force = force;
	}

	/********************  INGEST  ********************/
	
	public String getArtifactclassId() {
		return artifactclassId;
	}

	public void setArtifactclassId(String artifactclassId) {
		this.artifactclassId = artifactclassId;
	}

	public String getStagedFilepath() {
		return stagedFilepath;
	}

	public void setStagedFilepath(String stagedFilepath) {
		this.stagedFilepath = stagedFilepath;
	}

	public String getStagedFilename() {
		return stagedFilename;
	}

	public void setStagedFilename(String stagedFilename) {
		this.stagedFilename = stagedFilename;
	}

	public String getPrevSequenceCode() {
		return prevSequenceCode;
	}

	public void setPrevSequenceCode(String prevSequenceCode) {
		this.prevSequenceCode = prevSequenceCode;
	}

	public Integer getRerunNo() {
		return rerunNo;
	}

	public void setRerunNo(Integer rerunNo) {
		this.rerunNo = rerunNo;
	}

	public List<Integer> getSkipActionelements() {
		return skipActionelements;
	}

	public void setSkipActionelements(List<Integer> skipActionelements) {
		this.skipActionelements = skipActionelements;
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
