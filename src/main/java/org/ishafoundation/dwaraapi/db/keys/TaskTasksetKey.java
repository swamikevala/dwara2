package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TaskTasksetKey implements Serializable {
 
	private static final long serialVersionUID = -7404827563818031483L;

	@Column(name = "task_id")
    private int taskId;
 
    @Column(name = "taskset_id")
    private int tasksetId;
 
    public TaskTasksetKey() {}
    
    public TaskTasksetKey(
        int taskId,
        int tasksetId) {
        this.taskId = taskId;
        this.tasksetId = tasksetId;
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

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        TaskTasksetKey that = (TaskTasksetKey) o;
        return Objects.equals(taskId, that.taskId) &&
               Objects.equals(tasksetId, that.tasksetId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(taskId, tasksetId);
    }
}