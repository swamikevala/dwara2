package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ActionArtifactclassFlowKey implements Serializable {
	
	private static final long serialVersionUID = 4573206087894372384L;

	@Column(name = "action_id")
    private String actionId;

	@Column(name = "artifactclass_id")
    private String artifactclassId;
 
    @Column(name = "flow_id")
    private String flowId;
 
    public ActionArtifactclassFlowKey() {}
    
    public ActionArtifactclassFlowKey(
		String actionId,
		String artifactclassId,
		String flowId) {
    	this.actionId = actionId;
    	this.artifactclassId = artifactclassId;
        this.flowId = flowId;
    }

    public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}
 
	public String getArtifactclassId() {
		return artifactclassId;
	}

	public void setArtifactclassId(String artifactclassId) {
		this.artifactclassId = artifactclassId;
	}

	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ActionArtifactclassFlowKey that = (ActionArtifactclassFlowKey) o;
        return Objects.equals(artifactclassId, that.artifactclassId) && 
        		Objects.equals(actionId, that.actionId) &&
        		Objects.equals(flowId, that.flowId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(artifactclassId, actionId, flowId);
    }
}