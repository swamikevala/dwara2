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

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("flowId")
	private Flow flow;
	
    @Column(name = "active")
    private boolean active;
    
	public ActionArtifactclassFlow() {
		
	}

	public ActionArtifactclassFlow(Action action, String artifactclassId, Flow flow) {
		this.action = action;
		this.flow = flow;
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

	public Flow getFlow() {
		return flow;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
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
                Objects.equals(flow, that.flow);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(action, id.getArtifactclassId(), flow);
    }

}