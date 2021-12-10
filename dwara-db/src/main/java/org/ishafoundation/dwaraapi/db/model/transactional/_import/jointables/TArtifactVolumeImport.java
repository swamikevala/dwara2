package org.ishafoundation.dwaraapi.db.model.transactional._import.jointables;
		
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.TypeDef;
import org.ishafoundation.dwaraapi.db.keys.TArtifactVolumeImportKey;
import org.ishafoundation.dwaraapi.enumreferences.Status;

import com.vladmihalcea.hibernate.type.json.JsonStringType;


// TODO - Should this be action_volume_artifact or artifact_volume_action ???
@Entity
@Table(name="t_artifact_volume_import") // holds a list of artifacts of a volume we are importing 
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class TArtifactVolumeImport {

	@EmbeddedId
	private TArtifactVolumeImportKey id;
	
	@Column(name="artifact_id")
	private Integer artifactId;
	
	@Enumerated(EnumType.STRING)
	@Column(name="status")
	private Status status;
	
	@Lob
	@Column(name="message")
	private String message; 
	
	public TArtifactVolumeImportKey getId() {
		return id;
	}

	public void setId(TArtifactVolumeImportKey id) {
		this.id = id;
	}

	public Integer getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(Integer artifactId) {
		this.artifactId = artifactId;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}