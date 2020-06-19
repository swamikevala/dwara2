package org.ishafoundation.dwaraapi.db.model.transactional;
		
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.ishafoundation.dwaraapi.db.model.master.jointables.Actionelement;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileJob;
import org.ishafoundation.dwaraapi.enumreferences.Status;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="job")
public class Job {

	@Id
	@GeneratedValue(generator = "dwara_seq_generator", strategy=GenerationType.TABLE)
	@TableGenerator(name="dwara_seq_generator", 
	 table="dwara_sequences", 
	 pkColumnName="primary_key_fields", 
	 valueColumnName="current_val", 
	 pkColumnValue="job_id", allocationSize = 1)
	@Column(name="id")
	private int id;

	@Column(name="storagetask_id")
	private Integer storagetaskId; 

	@Column(name="processingtask_id")
	private Integer processingtaskId;

	@ManyToOne(fetch = FetchType.LAZY)
    private Actionelement actionelement;

	// The FK constraints are removed so that this can point to one of the domain's artifact.
	@Column(name="input_artifact_id")
	private Integer inputArtifactId; // can contain one of the domain artifacts id
	
	@Column(name="output_artifact_id")
	private Integer outputArtifactId;
	
	@ManyToOne(fetch = FetchType.LAZY)
    private Job jobRef;
	
	@Column(name="completed_at")
	private LocalDateTime completedAt;

	@Column(name="created_at")
	private LocalDateTime createdAt;

	// Many jobs are possible for a subrequest
	@ManyToOne(fetch = FetchType.LAZY)
	private Subrequest subrequest;
	
	@Column(name="started_at")
	private LocalDateTime startedAt;

	@Column(name="status_id")
	private Status status;
	
	@OneToOne(fetch = FetchType.LAZY)
	private org.ishafoundation.dwaraapi.db.model.master.reference.Error error; 
	
	@OneToOne(fetch = FetchType.LAZY)
	private Volume volume; 
	
    @OneToMany(mappedBy = "job",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<TFileJob> tFileJob = new ArrayList<>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Integer getStoragetaskId() {
		return storagetaskId;
	}

	public void setStoragetaskId(Integer storagetaskId) {
		this.storagetaskId = storagetaskId;
	}

	public Integer getProcessingtaskId() {
		return processingtaskId;
	}

	public void setProcessingtaskId(Integer processingtaskId) {
		this.processingtaskId = processingtaskId;
	}

	public Actionelement getActionelement() {
		return actionelement;
	}

	public void setActionelement(Actionelement actionelement) {
		this.actionelement = actionelement;
	}

	public Integer getInputArtifactId() {
		return inputArtifactId;
	}

	public void setInputArtifactId(Integer inputArtifactId) {
		this.inputArtifactId = inputArtifactId;
	}

	public Integer getOutputArtifactId() {
		return outputArtifactId;
	}

	public void setOutputArtifactId(Integer outputArtifactId) {
		this.outputArtifactId = outputArtifactId;
	}

	@JsonIgnore
	public Job getJobRef() {
		return jobRef;
	}

	@JsonIgnore
	public void setJobRef(Job jobRef) {
		this.jobRef = jobRef;
	}
	
	public org.ishafoundation.dwaraapi.db.model.master.reference.Error getError() {
		return error;
	}

	public void setError(org.ishafoundation.dwaraapi.db.model.master.reference.Error error) {
		this.error = error;
	}
	
	
	public LocalDateTime getCompletedAt() {
		return completedAt;
	}


	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@JsonIgnore
	public Subrequest getSubrequest() {
		return subrequest;
	}

	@JsonIgnore
	public void setSubrequest(Subrequest subrequest) {
		this.subrequest = subrequest;
	}

	public LocalDateTime getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(LocalDateTime startedAt) {
		this.startedAt = startedAt;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@JsonIgnore
	public Volume getVolume() {
		return volume;
	}

	@JsonIgnore
	public void setVolume(Volume volume) {
		this.volume = volume;
	}

	@JsonIgnore
	public List<TFileJob> gettFileJob() {
		return tFileJob;
	}
	
	@JsonIgnore
	public void settFileJob(List<TFileJob> tFileJob) {
		this.tFileJob = tFileJob;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return Objects.equals(id, job.id);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}