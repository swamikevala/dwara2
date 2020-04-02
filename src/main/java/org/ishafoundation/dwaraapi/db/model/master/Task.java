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
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.jointables.TaskTaskset;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name="task")
public class Task {

	@Id
	@Column(name="id")
	private Integer id; // Is Integer so compatibe with - field in library class (only applicable where source = false)
	
	@Column(name="name")
	private String name;

	// Many tasks can be configured for the same tasktype - infact task is a flavour(with different attributes) of tasktype
	@ManyToOne(fetch = FetchType.LAZY)
	private Tasktype tasktype;
	
	@Column(name="max_errors")
	private int maxErrors;

	// Many Task can share the same filetype...
	// Eg.,
	// Pubvideo - video
	// Privvideo - video
	@ManyToOne(fetch = FetchType.LAZY)
	private Taskfiletype taskfiletype;
	
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

	public Tasktype getTasktype() {
		return tasktype;
	}

	public void setTasktype(Tasktype tasktype) {
		this.tasktype = tasktype;
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