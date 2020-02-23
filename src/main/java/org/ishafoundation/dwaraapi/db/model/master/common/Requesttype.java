package org.ishafoundation.dwaraapi.db.model.master.common;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="requesttype")
public class Requesttype {

	@Id
	@Column(name="requesttype_id")
	private int requesttypeId;
	
	@Column(name="name")
	private String name;

	@Column(name="task_id")
	private int taskId;

	@Column(name="taskset_id")
	private int tasksetId;
	
	
	public int getRequesttypeId() {
		return requesttypeId;
	}

	public void setRequesttypeId(int requesttypeId) {
		this.requesttypeId = requesttypeId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getTasksetId() {
		return tasksetId;
	}

	public void setTasksetId(int tasksetId) {
		this.tasksetId = tasksetId;
	}
}