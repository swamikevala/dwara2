package org.ishafoundation.dwaraapi.db.model.master.ingest;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="requesttype_libraryclass")
public class RequesttypeLibraryclass {

	@Id
	@Column(name="requesttype_libraryclass_id")
	private int requesttypeLibraryclassId;
	
	@Column(name="requesttype_id")
	private int requesttypeId;

	@Column(name="libraryclass_id")
	private int libraryclassId;

	@Column(name="task_id")
	private int taskId;

	@Column(name="taskset_id")
	private int tasksetId;

		
	public int getRequesttypeLibraryclassId() {
		return requesttypeLibraryclassId;
	}

	public void setRequesttypeLibraryclassId(int requesttypeLibraryclassId) {
		this.requesttypeLibraryclassId = requesttypeLibraryclassId;
	}
	
	public int getRequesttypeId() {
		return requesttypeId;
	}

	public void setRequesttypeId(int requesttypeId) {
		this.requesttypeId = requesttypeId;
	}
	
	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
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