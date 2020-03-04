package org.ishafoundation.dwaraapi.db.model.transactional;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="job")
public class Job {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="job_id")
	private int jobId;
	
	@Column(name="task_id")
	private int taskId;

	@Column(name="input_library_id")
	private int inputLibraryId;

	@Column(name="output_library_id")
	private int outputLibraryId;
	
	@Column(name="completed_at")
	private long completedAt;

	@Column(name="created_at")
	private long createdAt;

	@Column(name="subrequest_id")
	private int subrequestId;

	@Column(name="started_at")
	private long startedAt;

	@Column(name="status_id")
	private int statusId;

		
	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	
	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	
	public int getInputLibraryId() {
		return inputLibraryId;
	}

	public void setInputLibraryId(int inputLibraryId) {
		this.inputLibraryId = inputLibraryId;
	}

	public int getOutputLibraryId() {
		return outputLibraryId;
	}

	public void setOutputLibraryId(int outputLibraryId) {
		this.outputLibraryId = outputLibraryId;
	}

	public long getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(long completedAt) {
		this.completedAt = completedAt;
	}
	
	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}
	
	public int getSubrequestId() {
		return subrequestId;
	}

	public void setSubrequestId(int subrequestId) {
		this.subrequestId = subrequestId;
	}
	
	public long getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(long startedAt) {
		this.startedAt = startedAt;
	}
	
	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

}