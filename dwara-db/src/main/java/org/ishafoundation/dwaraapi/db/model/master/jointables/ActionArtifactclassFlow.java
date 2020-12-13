package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.ActionArtifactclassFlowKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Flow;
import org.ishafoundation.dwaraapi.db.model.master.reference.Action;

@Entity(name = "ActionArtifactclassFlow")
@Table(name="action_artifactclass_flow")
public class ActionArtifactclassFlow {

	@EmbeddedId
	private ActionArtifactclassFlowKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("actionId")
	private Action action;
	
    @Column(name = "active")
    private boolean active;
    
	public ActionArtifactclassFlow() {
		
	}

	public ActionArtifactclassFlow(Action action, String artifactclassId, Flow flow) {
		this.action = action;
		this.id = new ActionArtifactclassFlowKey(action.getId(), artifactclassId, flow.getId());
	}
	
    public ActionArtifactclassFlowKey getId() {
		return id;
	}

	public void setId(ActionArtifactclassFlowKey id) {
		this.id = id;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}


	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ActionArtifactclassFlow that = (ActionArtifactclassFlow) o;
        return 
        		Objects.equals(action, that.action) &&
        		Objects.equals(id.getArtifactclassId(), that.id.getArtifactclassId()) &&
                Objects.equals(id.getFlowId(), that.id.getFlowId());
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(action, id.getArtifactclassId(), id.getFlowId());
    }

}