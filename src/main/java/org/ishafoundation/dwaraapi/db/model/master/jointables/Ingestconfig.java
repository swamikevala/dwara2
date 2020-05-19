package org.ishafoundation.dwaraapi.db.model.master.jointables;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.Tapeset;
import org.ishafoundation.dwaraapi.enumreferences.Tasktype;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name = "Ingestconfig")
@Table(name = "ingestconfig", uniqueConstraints={@UniqueConstraint(columnNames = {"task_id", "tasktype_id","ingest_libraryclass_id","tapeset_id"})})
public class Ingestconfig {

	@Id
	@Column(name="id")
	private int id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="ingest_libraryclass_id")
	private Libraryclass ingestLibraryclass;

	@Column(name = "task_id")
	private int taskId;

	@Column(name="tasktype_id")
	private Tasktype tasktype;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="input_libraryclass_id")
	private Libraryclass inputLibraryclass;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="tapeset_id")
	private Tapeset tapeset;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="output_libraryclass_id")
	private Libraryclass outputLibraryclass;

	@Column(name = "encryption")
	private boolean encryption;

	// Certain tasks are dependent on a parent task. For eg., to update Mam, we need
	// proxies to be generated. So Proxy gen task is the prerequisite task for
	// MamUpdate task...
//	@OneToOne
//	@JoinColumn(name = "pre_task_id")
//	private Task preTask;
	@Column(name = "pre_processingtask_id")
	private Integer preProcessingTaskId;
	
	@Column(name = "display_order")
	private int displayOrder;



	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	@JsonIgnore
	public Libraryclass getIngestLibraryclass() {
		return ingestLibraryclass;
	}
	
	@JsonIgnore
	public void setIngestLibraryclass(Libraryclass ingestLibraryclass) {
		this.ingestLibraryclass = ingestLibraryclass;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	@JsonIgnore
	public Tasktype getTasktype() {
		return tasktype;
	}

	@JsonIgnore
	public void setTasktype(Tasktype tasktype) {
		this.tasktype = tasktype;
	}

	@JsonIgnore
	public Libraryclass getInputLibraryclass() {
		return inputLibraryclass;
	}

	@JsonIgnore
	public void setInputLibraryclass(Libraryclass inputLibraryclass) {
		this.inputLibraryclass = inputLibraryclass;
	}

	@JsonIgnore
	public Tapeset getTapeset() {
		return tapeset;
	}

	@JsonIgnore
	public void setTapeset(Tapeset tapeset) {
		this.tapeset = tapeset;
	}

	@JsonIgnore
	public Libraryclass getOutputLibraryclass() {
		return outputLibraryclass;
	}

	@JsonIgnore
	public void setOutputLibraryclass(Libraryclass outputLibraryclass) {
		this.outputLibraryclass = outputLibraryclass;
	}

	public boolean isEncryption() {
		return encryption;
	}

	public void setEncryption(boolean encryption) {
		this.encryption = encryption;
	}

	
	
//	public Task getPreTask() {
//		return preTask;
//	}
//
//	public void setPreTask(Task preTask) {
//		this.preTask = preTask;
//	}

	public Integer getPreProcessingTaskId() {
		return preProcessingTaskId;
	}

	public void setPreProcessingTaskId(Integer preProcessingTaskId) {
		this.preProcessingTaskId = preProcessingTaskId;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		Ingestconfig that = (Ingestconfig) o;
		return Objects.equals(taskId, that.taskId) && Objects.equals(tasktype, that.tasktype)
				&& Objects.equals(inputLibraryclass, that.inputLibraryclass) && Objects.equals(tapeset, that.tapeset);
	}

	@Override
	public int hashCode() {
		return Objects.hash(taskId, tasktype, inputLibraryclass, tapeset);
	}

}