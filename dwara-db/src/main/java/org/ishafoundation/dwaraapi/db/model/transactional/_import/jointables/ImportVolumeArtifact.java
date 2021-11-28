package org.ishafoundation.dwaraapi.db.model.transactional._import.jointables;
		
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.TypeDef;
import org.ishafoundation.dwaraapi.db.keys.ImportVolumeArtifactKey;
import org.ishafoundation.dwaraapi.enumreferences.Status;

import com.vladmihalcea.hibernate.type.json.JsonStringType;


// TODO - Should this be action_volume_artifact ???
@Entity
@Table(name="import_volume_artifact")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class ImportVolumeArtifact {

	@EmbeddedId
	private ImportVolumeArtifactKey id;
	
	@Enumerated(EnumType.STRING)
	@Column(name="status")
	private Status status;
	
	@Lob
	@Column(name="message")
	private String message; 

	public ImportVolumeArtifactKey getId() {
		return id;
	}

	public void setId(ImportVolumeArtifactKey id) {
		this.id = id;
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