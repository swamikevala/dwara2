package org.ishafoundation.dwaraapi.db.model.master;
		
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.jointables.TaskTaskset;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name="taskset")
public class Taskset {

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name")
	private String name;
	
    @OneToMany(mappedBy = "taskset",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<TaskTaskset> tasks = new ArrayList<>();
		
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonIgnore
    public List<TaskTaskset> getTasks() {
		return tasks;
	}

	@JsonIgnore
	public void setTasks(List<TaskTaskset> tasks) {
		this.tasks = tasks;
	}

	public void addTask(Task task, Task preTask) {
        TaskTaskset taskTaskset = new TaskTaskset(task, this);
        // TODO set the pre-requisite task also 
        taskTaskset.setPreTask(preTask);
        tasks.add(taskTaskset);
        task.getTasksets().add(taskTaskset);
    }
    
    public void removeTask(Task task) {
        for (Iterator<TaskTaskset> iterator = tasks.iterator();
             iterator.hasNext(); ) {
            TaskTaskset taskTaskset = iterator.next();
 
            if (taskTaskset.getTaskset().equals(this) &&
                    taskTaskset.getTask().equals(task)) {
                iterator.remove();
                taskTaskset.getTask().getTasksets().remove(taskTaskset);
                taskTaskset.setTaskset(null);
                taskTaskset.setTask(null);
            }
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        Taskset taskset = (Taskset) o;
        return Objects.equals(name, taskset.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}