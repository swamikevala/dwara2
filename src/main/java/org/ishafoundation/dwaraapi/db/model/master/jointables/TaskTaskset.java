package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.TaskTasksetKey;
import org.ishafoundation.dwaraapi.db.model.master.Task;
import org.ishafoundation.dwaraapi.db.model.master.Taskset;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name = "TaskTaskset")
@Table(name="task_taskset")
public class TaskTaskset {
	@EmbeddedId
	private TaskTasksetKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tasksetId")
	private Taskset taskset;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
	private Task task;

	// Certain tasks are dependent on a parent task. For eg., to update Mam, we need proxies to be generated. So Proxy gen task is the prerequisite task for MamUpdate task...
	@OneToOne
	@JoinColumn(name = "pre_task_id")
	private Task preTask; 
	
	public TaskTaskset() {
		
	}

	public TaskTaskset(Task task, Taskset taskset) {
		this.task = task;
		this.taskset = taskset;
		this.id = new TaskTasksetKey(task.getId(), taskset.getId());
	}
	
	@JsonIgnore
	public TaskTasksetKey getId() {
		return id;
	}
	
	@JsonIgnore
	public void setId(TaskTasksetKey id) {
		this.id = id;
	}

	public Taskset getTaskset() {
		return taskset;
	}

	public void setTaskset(Taskset taskset) {
		this.taskset = taskset;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}
	
	public Task getPreTask() {
		return preTask;
	}

	public void setPreTask(Task preTask) {
		this.preTask = preTask;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        TaskTaskset that = (TaskTaskset) o;
        return Objects.equals(task, that.task) &&
               Objects.equals(taskset, that.taskset);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(task, taskset);
    }

}