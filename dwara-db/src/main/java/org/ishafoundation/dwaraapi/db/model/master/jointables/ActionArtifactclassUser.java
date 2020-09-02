package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.ActionArtifactclassUserKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.master.reference.Action;

@Entity(name = "ArtifactclassActionUser")
@Table(name="action_artifactclass_user")
public class ActionArtifactclassUser {

	@EmbeddedId
	private ActionArtifactclassUserKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("artifactclassId")
	private Artifactclass artifactclass;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("actionId")
	private Action action;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
	private User user;
	
	
	public ActionArtifactclassUser() {
		
	}

	public ActionArtifactclassUser(Action action, Artifactclass artifactclass, User user) {
		this.artifactclass = artifactclass;
		this.action = action;
		this.user = user;
		this.id = new ActionArtifactclassUserKey(action.getId(), artifactclass.getId(), user.getId());
	}
	
    public ActionArtifactclassUserKey getId() {
		return id;
	}

	public void setId(ActionArtifactclassUserKey id) {
		this.id = id;
	}

	public Artifactclass getArtifactclass() {
		return artifactclass;
	}

	public void setArtifactclass(Artifactclass artifactclass) {
		this.artifactclass = artifactclass;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ActionArtifactclassUser that = (ActionArtifactclassUser) o;
        return Objects.equals(artifactclass, that.artifactclass) &&
        		Objects.equals(action, that.action) &&
                Objects.equals(user, that.user);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(artifactclass, action, user);
    }

}