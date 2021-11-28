package org.ishafoundation.dwaraapi.api.resp._import;

import org.ishafoundation.dwaraapi.enumreferences.ImportStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Artifact {
	
	private int id;
	private String name;
	private ImportStatus artifactStatus; // skipped if its a copy / rerun
	private ImportStatus artifactVolumeStatus; // skipped if its a rerun - even for copy we should have it as completed 
	private ImportStatus fileStatus; // skipped if its a copy / rerun
	private ImportStatus fileVolumeStatus; // skipped if its a rerun - even for copy we should have it as completed
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ImportStatus getArtifactStatus() {
		return artifactStatus;
	}
	public void setArtifactStatus(ImportStatus artifactStatus) {
		this.artifactStatus = artifactStatus;
	}
	public ImportStatus getArtifactVolumeStatus() {
		return artifactVolumeStatus;
	}
	public void setArtifactVolumeStatus(ImportStatus artifactVolumeStatus) {
		this.artifactVolumeStatus = artifactVolumeStatus;
	}
	public ImportStatus getFileStatus() {
		return fileStatus;
	}
	public void setFileStatus(ImportStatus fileStatus) {
		this.fileStatus = fileStatus;
	}
	public ImportStatus getFileVolumeStatus() {
		return fileVolumeStatus;
	}
	public void setFileVolumeStatus(ImportStatus fileVolumeStatus) {
		this.fileVolumeStatus = fileVolumeStatus;
	}
}
