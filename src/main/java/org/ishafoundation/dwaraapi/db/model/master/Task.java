package org.ishafoundation.dwaraapi.db.model.master;
		
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.jointables.TaskTaskset;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name="task")
public class Task {

	@Id
	@Column(name="id")
	private Integer id; // Is Integer so compatibe with - field in library class (only applicable where source = false)
	
	@Column(name="name", unique=true)
	private String name;

	@Column(name="description")
	private String description;
	
	@Column(name="max_errors")
	private int maxErrors;

	// Many Task can share the same filetype...
	// Eg.,
	// Pubvideo - video
	// Privvideo - video
	@ManyToOne(fetch = FetchType.LAZY)
	private Taskfiletype taskfiletype;

	@OneToOne(fetch = FetchType.LAZY)
	private Application application;
	
	@Column(name="copy_number")
	private Integer copyNumber;

	@OneToOne
	private Libraryclass outputLibraryclass;
	
    @OneToMany(mappedBy = "task",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<TaskTaskset> tasksets = new ArrayList<>();
	
	
    public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getMaxErrors() {
		return maxErrors;
	}

	public void setMaxErrors(int maxErrors) {
		this.maxErrors = maxErrors;
	}

	public Taskfiletype getTaskfiletype() {
		return taskfiletype;
	}

	public void setTaskfiletype(Taskfiletype taskfiletype) {
		this.taskfiletype = taskfiletype;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}
	
	public Integer getCopyNumber() {
		return copyNumber;
	}

	public void setCopyNumber(Integer copyNumber) {
		this.copyNumber = copyNumber;
	}

	@JsonIgnore
	public Libraryclass getOutputLibraryclass() {
		return outputLibraryclass;
	}

	@JsonIgnore
	public void setOutputLibraryclass(Libraryclass outputLibraryclass) {
		this.outputLibraryclass = outputLibraryclass;
	}

	@JsonIgnore
	public List<TaskTaskset> getTasksets() {
		return tasksets;
	}

	@JsonIgnore
	public void setTasksets(List<TaskTaskset> tasksets) {
		this.tasksets = tasksets;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        Task task = (Task) o;
        return Objects.equals(name, task.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}