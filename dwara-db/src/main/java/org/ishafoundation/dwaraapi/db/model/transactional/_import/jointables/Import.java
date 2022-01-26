package org.ishafoundation.dwaraapi.db.model.transactional._import.jointables;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.TypeDef;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.enumreferences.Status;

import com.vladmihalcea.hibernate.type.json.JsonStringType;


// TODO - Should this be action_volume_artifact or artifact_volume_action ???
@Entity
@Table(name="import") // holds a list of artifacts of a volume we are importing 
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class Import {

	@Id
	@GeneratedValue(generator = "dwara_seq_generator", strategy=GenerationType.TABLE)
	@TableGenerator(name="dwara_seq_generator", 
	 table="dwara_sequences", 
	 pkColumnName="primary_key_fields", 
	 valueColumnName="current_val", 
	 pkColumnValue="import_id", allocationSize = 1)
	@Column(name="id")
	private int id;
	
	@Column(name = "volume_id")
    private String volumeId;
    
    @Column(name = "artifact_name")
    private String artifactName;
    
    @Column(name = "requeue_id")
    private int requeueId;
	
	@Column(name="artifact_id")
	private Integer artifactId;
	
	@OneToOne(fetch = FetchType.LAZY)
    private Request request;
	
	@Enumerated(EnumType.STRING)
	@Column(name="status")
	private Status status;
	
	@Lob
	@Column(name="message")
	private String message; 
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getVolumeId() {
		return volumeId;
	}

	public void setVolumeId(String volumeId) {
		this.volumeId = volumeId;
	}

	public String getArtifactName() {
		return artifactName;
	}

	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}

	public int getRequeueId() {
		return requeueId;
	}

	public void setRequeueId(int requeueId) {
		this.requeueId = requeueId;
	}

	public Integer getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(Integer artifactId) {
		this.artifactId = artifactId;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
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