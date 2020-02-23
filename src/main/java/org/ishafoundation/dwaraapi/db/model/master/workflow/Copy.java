package org.ishafoundation.dwaraapi.db.model.master.workflow;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="copy")
public class Copy {

	@Id
	@Column(name="copy_id")
	private int copyId;
	
	@Column(name="libraryclass_id")
	private int libraryclassId;

	@Column(name="tapeset_id")
	private int tapesetId;

	@Column(name="copy_number")
	private int copyNumber;

	@Column(name="task_id")
	private int taskId;

	@Column(name="encrypted")
	private boolean encrypted;

		
	public int getCopyId() {
		return copyId;
	}

	public void setCopyId(int copyId) {
		this.copyId = copyId;
	}
	
	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
	}
	
	public int getTapesetId() {
		return tapesetId;
	}

	public void setTapesetId(int tapesetId) {
		this.tapesetId = tapesetId;
	}
	
	public int getCopyNumber() {
		return copyNumber;
	}

	public void setCopyNumber(int copyNumber) {
		this.copyNumber = copyNumber;
	}
	
	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	
	public boolean isEncrypted() {
		return encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

}