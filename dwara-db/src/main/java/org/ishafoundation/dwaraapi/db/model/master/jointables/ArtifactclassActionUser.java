package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.ArtifactclassActionUserKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.master.reference.Action;

@Entity(name = "ArtifactclassActionUser")
@Table(name="artifactclass_action_user")
public class ArtifactclassActionUser {

	@EmbeddedId
	private ArtifactclassActionUserKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("artifactclassId")
	private Artifactclass artifactclass;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("actionId")
	private Action action;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
	private User user;
	
	
	public ArtifactclassActionUser() {
		
	}

	public ArtifactclassActionUser(Artifactclass artifactclass, Action action, User user) {
		this.artifactclass = artifactclass;
		this.action = action;
		this.user = user;
		this.id = new ArtifactclassActionUserKey(artifactclass.getId(), action.getId(), user.getId());
	}
	
    public ArtifactclassActionUserKey getId() {
		return id;
	}

	public void setId(ArtifactclassActionUserKey id) {
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
 
        ArtifactclassActionUser that = (ArtifactclassActionUser) o;
        return Objects.equals(artifactclass, that.artifactclass) &&
        		Objects.equals(action, that.action) &&
                Objects.equals(user, that.user);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(artifactclass, action, user);
    }

}