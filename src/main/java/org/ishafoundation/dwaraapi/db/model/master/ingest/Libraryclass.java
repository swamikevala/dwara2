package org.ishafoundation.dwaraapi.db.model.master.ingest;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="libraryclass")
public class Libraryclass {

	@Id
	@Column(name="libraryclass_id")
	private int libraryclassId;
	
	@Column(name="name")
	private String name;
	
	@Column(name="path_prefix")
	private String pathPrefix;	

	@Column(name="sequence_id")
	private int sequenceId;

	@Column(name="source")
	private boolean source;

	@Column(name="filetype_id")
	private int filetypeId;

	@Column(name="task_id")
	private int taskId;

	@Column(name="no_file_records")
	private boolean noFileRecords;

	@Column(name="concurrent_copies")
	private boolean concurrentCopies;

		
	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getPathPrefix() {
		return pathPrefix;
	}

	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix = pathPrefix;
	}

	public int getSequenceId() {
		return sequenceId;
	}

	public void setSequenceId(int sequenceId) {
		this.sequenceId = sequenceId;
	}
	
	public boolean isSource() {
		return source;
	}

	public void setSource(boolean source) {
		this.source = source;
	}
	
	public int getFiletypeId() {
		return filetypeId;
	}

	public void setFiletypeId(int filetypeId) {
		this.filetypeId = filetypeId;
	}
	
	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public boolean isNoFileRecords() {
		return noFileRecords;
	}

	public void setNoFileRecords(boolean noFileRecords) {
		this.noFileRecords = noFileRecords;
	}
	
	public boolean isConcurrentCopies() {
		return concurrentCopies;
	}

	public void setConcurrentCopies(boolean concurrentCopies) {
		this.concurrentCopies = concurrentCopies;
	}

}