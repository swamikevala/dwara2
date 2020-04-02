package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ExtensionTaskfiletypeKey implements Serializable {
 
	private static final long serialVersionUID = -620517854806310403L;

	@Column(name = "extension_id")
    private int extensionId;
 
    @Column(name = "taskfiletype_id")
    private int taskfiletypeId;
 
    public ExtensionTaskfiletypeKey() {}
    
    public ExtensionTaskfiletypeKey(
        int extensionId,
        int taskfiletypeId) {
        this.extensionId = extensionId;
        this.taskfiletypeId = taskfiletypeId;
    }
 
    public int getExtensionId() {
		return extensionId;
	}

	public void setExtensionId(int extensionId) {
		this.extensionId = extensionId;
	}

	public int getTaskfiletypeId() {
		return taskfiletypeId;
	}

	public void setTaskfiletypeId(int taskfiletypeId) {
		this.taskfiletypeId = taskfiletypeId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ExtensionTaskfiletypeKey that = (ExtensionTaskfiletypeKey) o;
        return Objects.equals(extensionId, that.extensionId) &&
               Objects.equals(taskfiletypeId, that.taskfiletypeId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(extensionId, taskfiletypeId);
    }
}