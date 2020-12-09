package org.ishafoundation.dwaraapi.db.model.transactional.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestDetails {
	
	@JsonUnwrapped
	private JsonNode body;
	
	@JsonProperty("autoloader_id")
	private String autoloaderId;
	
	// Initialize
	@JsonProperty("volume_id")
	private String volumeId;
	
	@JsonProperty("volume_group_id")
	private String volumeGroupId;
	
	@JsonProperty("volume_blocksize")
	private Integer volumeBlocksize;

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
	@JsonProperty("file_id")
	private Integer fileId;

	@JsonProperty("timecode_start")
	private String timecodeStart;

	@JsonProperty("timecode_end")
	private String timecodeEnd;

	@JsonProperty("copy_id")
	private Integer copyId;
	
	@JsonProperty("destinationpath")
	private String destinationPath;
	
	@JsonProperty("output_folder")
	private String outputFolder;
	
	private Boolean verify; // overwrites whatever is configured in archiveformat.restore_verify = true
	
	@JsonProperty("flow_name")
	private String flowName;
	
	@JsonProperty("domain_id")
	private Integer domainId;
	
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

	/********************  MAP_DRIVES  ********************/

	public String getAutoloaderId() {
		return autoloaderId;
	}

	public void setAutoloaderId(String autoloaderId) {
		this.autoloaderId = autoloaderId;
	}
	
	/********************  INITIALIZE  ********************/


	public String getVolumeId() {
		return volumeId;
	}

	public void setVolumeId(String volumeId) {
		this.volumeId = volumeId;
	}

	public String getVolumeGroupId() {
		return volumeGroupId;
	}

	public void setVolumeGroupId(String volumeGroupId) {
		this.volumeGroupId = volumeGroupId;
	}

	public Integer getVolumeBlocksize() {
		return volumeBlocksize;
	}

	public void setVolumeBlocksize(Integer volumeBlocksize) {
		this.volumeBlocksize = volumeBlocksize;
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
	
	/********************  RENAME  ********************/

//	public String getOldName() {
//		return oldName;
//	}
//
//	public void setOldName(String oldName) {
//		this.oldName = oldName;
//	}
//
//	public String getNewName() {
//		return newName;
//	}
//
//	public void setNewName(String newName) {
//		this.newName = newName;
//	}

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
	
	public Integer getFileId() {
		return fileId;
	}

	public void setFileId(Integer fileId) {
		this.fileId = fileId;
	}
	
	public String getTimecodeStart() {
		return timecodeStart;
	}

	public void setTimecodeStart(String timecodeStart) {
		this.timecodeStart = timecodeStart;
	}

	public String getTimecodeEnd() {
		return timecodeEnd;
	}

	public void setTimecodeEnd(String timecodeEnd) {
		this.timecodeEnd = timecodeEnd;
	}

	public Integer getCopyId() {
		return copyId;
	}

	public void setCopyId(Integer copyId) {
		this.copyId = copyId;
	}

	public String getDestinationPath() {
		return destinationPath;
	}

	public void setDestinationPath(String destinationPath) {
		this.destinationPath = destinationPath;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public Boolean getVerify() {
		return verify;
	}

	public void setVerify(Boolean verify) {
		this.verify = verify;
	}

	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public Integer getDomainId() {
		return domainId;
	}

	public void setDomainId(Integer domainId) {
		this.domainId = domainId;
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
