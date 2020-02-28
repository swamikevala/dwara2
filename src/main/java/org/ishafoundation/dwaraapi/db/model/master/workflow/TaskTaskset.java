package org.ishafoundation.dwaraapi.db.model.master.workflow;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="task_taskset")
public class TaskTaskset {

	@Id
	@Column(name="task_taskset_id")
	private int taskTasksetId;
	
	@Column(name="taskset_id")
	private int tasksetId;

	@Column(name="task_id")
	private int taskId;

	@Column(name="pre_task_id")
	private int preTaskId;
	
		
	public int getTaskTasksetId() {
		return taskTasksetId;
	}

	public void setTaskTasksetId(int taskTasksetId) {
		this.taskTasksetId = taskTasksetId;
	}
	
	public int getTasksetId() {
		return tasksetId;
	}

	public void setTasksetId(int tasksetId) {
		this.tasksetId = tasksetId;
	}
	
	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getPreTaskId() {
		return preTaskId;
	}

	public void setPreTaskId(int preTaskId) {
		this.preTaskId = preTaskId;
	}
	
	@Override
	public String toString() {
		return "taskTasksetId - " + taskTasksetId + "tasksetId - " + tasksetId + "taskId - " + taskId + "preTaskId - " + preTaskId;
	}
}