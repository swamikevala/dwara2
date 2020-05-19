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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileJob;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Tasktype;

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

	@Column(name="tasktype_id")
	private Tasktype tasktype;
	
	@Column(name="task_id")
	private int taskId; // Points to a record either in storagetask or in processingtask

	// The FK constraint is removed so that this can point to one of the domain's library.
	@OneToOne
	@JoinColumn(name="input_library_id")
	private Library inputLibrary;

	@OneToOne(optional = true)
	@JoinColumn(name="output_library_id")
	private Library outputLibrary;

//	@Column(name="input_library_id")
//	private int input_library_id; // can contain one of the domain librarys id
//	
//	@Column(name="output_library_id")
//	private int output_library_id;

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
	private org.ishafoundation.dwaraapi.db.model.master.configuration.Error error; 
	
	@OneToOne(fetch = FetchType.LAZY)
	private Tape tape; 
	
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

//	public Task getTask() {
//		return task;
//	}
//
//	public void setTask(Task task) {
//		this.task = task;
//	}
//
	public Library getInputLibrary() {
		return inputLibrary;
	}

	public void setInputLibrary(Library inputLibrary) {
		this.inputLibrary = inputLibrary;
	}

	public Library getOutputLibrary() {
		return outputLibrary;
	}

	public void setOutputLibrary(Library outputLibrary) {
		this.outputLibrary = outputLibrary;
	}

	public Tasktype getTasktype() {
		return tasktype;
	}

	public void setTasktype(Tasktype tasktype) {
		this.tasktype = tasktype;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

//	public int getInput_library_id() {
//		return input_library_id;
//	}
//
//	public void setInput_library_id(int input_library_id) {
//		this.input_library_id = input_library_id;
//	}
//
//	public int getOutput_library_id() {
//		return output_library_id;
//	}
//
//	public void setOutput_library_id(int output_library_id) {
//		this.output_library_id = output_library_id;
//	}

	public Job getJobRef() {
		return jobRef;
	}

	public void setJobRef(Job jobRef) {
		this.jobRef = jobRef;
	}

	public org.ishafoundation.dwaraapi.db.model.master.configuration.Error getError() {
		return error;
	}

	public void setError(org.ishafoundation.dwaraapi.db.model.master.configuration.Error error) {
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

	public Subrequest getSubrequest() {
		return subrequest;
	}

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
	public Tape getTape() {
		return tape;
	}

	@JsonIgnore
	public void setTape(Tape tape) {
		this.tape = tape;
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