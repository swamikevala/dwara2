package org.ishafoundation.dwaraapi.db.model.master.workflow;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="taskset")
public class Taskset {

	@Id
	@Column(name="taskset_id")
	private int tasksetId;
	
	@Column(name="name")
	private String name;

		
	public int getTasksetId() {
		return tasksetId;
	}

	public void setTasksetId(int tasksetId) {
		this.tasksetId = tasksetId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}