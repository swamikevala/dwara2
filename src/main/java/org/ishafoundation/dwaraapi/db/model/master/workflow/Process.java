package org.ishafoundation.dwaraapi.db.model.master.workflow;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="process")
public class Process {

	@Id
	@Column(name="process_id")
	private int processId;
	
	@Column(name="name")
	private String name;

		
	public int getProcessId() {
		return processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}