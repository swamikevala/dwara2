package org.ishafoundation.dwaraapi.db.model.transactional.json;

import java.util.List;

import org.ishafoundation.dwaraapi.enumreferences.RewriteMode;

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
	
	@JsonProperty("frame")
	private List<Frame> frame;	

	@JsonProperty("copy_id")
	private Integer copyId;
	
	@JsonProperty("destinationpath")
	private String destinationPath;
	
	@JsonProperty("output_folder")
	private String outputFolder;
	
	@Deprecated // retaining so as already created DB records wont fail
	private Boolean verify; // overwrites whatever is configured in archiveformat.restore_verify = true
	
	@JsonProperty("flow_id")
	private String flowId;
	
	// for backward compatibility...
	@JsonProperty("domain_id")
	private Integer domainId;
	
	// rewrite stuff
	@JsonProperty("artifact_id")
	private Integer artifactId;

	@JsonProperty("rewrite_copy")
	private Integer rewriteCopy;

	@JsonProperty("source_copy")
	private Integer sourceCopy;

	private RewriteMode mode;
	
	@JsonProperty("artifactclass_regex")
	private String artifactclassRegex;
	
	@JsonProperty("destination_copy")
	private Integer destinationCopy;

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
	
	public List<Frame> getFrame() {
		return frame;
	}

	public void setFrame(List<Frame> frame) {
		this.frame = frame;
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

	@Deprecated
	public Boolean getVerify() {
		return verify;
	}

	@Deprecated
	public void setVerify(Boolean verify) {
		this.verify = verify;
	}

	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	public Integer getDomainId() {
		return domainId;
	}

	public void setDomainId(Integer domainId) {
		this.domainId = domainId;
	}

	/********************  REWRITE  ********************/
	
	public Integer getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(Integer artifactId) {
		this.artifactId = artifactId;
	}

	public Integer getRewriteCopy() {
		return rewriteCopy;
	}

	public void setRewriteCopy(Integer rewriteCopy) {
		this.rewriteCopy = rewriteCopy;
	}

	public Integer getSourceCopy() {
		return sourceCopy;
	}

	public void setSourceCopy(Integer sourceCopy) {
		this.sourceCopy = sourceCopy;
	}

	public RewriteMode getMode() {
		return mode;
	}

	public void setMode(RewriteMode mode) {
		this.mode = mode;
	}

	public String getArtifactclassRegex() {
		return artifactclassRegex;
	}

	public void setArtifactclassRegex(String artifactclassRegex) {
		this.artifactclassRegex = artifactclassRegex;
	}

	public Integer getDestinationCopy() {
		return destinationCopy;
	}

	public void setDestinationCopy(Integer destinationCopy) {
		this.destinationCopy = destinationCopy;
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
