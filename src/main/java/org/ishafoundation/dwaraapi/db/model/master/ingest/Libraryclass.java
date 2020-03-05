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

	@Column(name="concurrent_copies")
	private boolean concurrentCopies;
//
//	@Transient
//	private String path;
	
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
	
	public boolean isConcurrentCopies() {
		return concurrentCopies;
	}

	public void setConcurrentCopies(boolean concurrentCopies) {
		this.concurrentCopies = concurrentCopies;
	}

	public String getCategory() {
		String category = "public";
		// TODO : should this be private1/2/3 
		if(getName().toLowerCase().startsWith("private")) {
			category = "private";
		}
		return category;
	}

	public String getPath() {
		String pathWithOutLibrary = null;
		if(isSource())
			pathWithOutLibrary = getPathPrefix();
		else
			pathWithOutLibrary = getPathPrefix() + java.io.File.separator + getCategory();

		return pathWithOutLibrary;
	}
	
	/*
	public String getPathWithLibrary(String libraryName) {
		String pathWithLibrary = null;
		if(isSource()) {
			pathWithLibrary = getPathPrefix() + java.io.File.separator + libraryName;
		}
		else {
			String libraryNamePrefix = sequenceDao.findById(getSequenceId()).get().getPrefix();
			pathWithLibrary = getPathPrefix() + java.io.File.separator + getCategory() + java.io.File.separator + libraryNamePrefix + libraryName;
		}
		return pathWithLibrary;
	}*/
}