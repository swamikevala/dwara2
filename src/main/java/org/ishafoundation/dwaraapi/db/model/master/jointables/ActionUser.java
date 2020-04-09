package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.ActionUserKey;
import org.ishafoundation.dwaraapi.db.model.master.User;
import org.ishafoundation.dwaraapi.db.model.master.reference.Action;

/*
 * 
 * References - 
 * 1) "Bidirectional many-to-many with a link entity" - https://docs.jboss.org/hibernate/orm/5.3/userguide/html_single/Hibernate_User_Guide.html#associations-many-to-many
 * 2) https://vladmihalcea.com/the-best-way-to-map-a-many-to-many-association-with-extra-columns-when-using-jpa-and-hibernate/
 * 3) https://www.baeldung.com/jpa-many-to-many
 * 
 * 
*/
@Entity(name = "ActionUser")
@Table(name="action_user")
public class ActionUser {

	@EmbeddedId
	private ActionUserKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("actionId")
	private Action action;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
	private User user;
	
	@Column(name="permission_level")
	private int permissionLevel;

	public ActionUser() {
		
	}
	
	public ActionUser(Action action, User user) {
		this.action = action;
		this.user = user;
		this.id = new ActionUserKey(action.getId(), user.getId());
	}
	
	public ActionUser(Action action, User user, int permissionLevel) {
		this(action, user);
		this.permissionLevel = permissionLevel;
	}
	
    public ActionUserKey getId() {
		return id;
	}

	public void setId(ActionUserKey id) {
		this.id = id;
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

    public int getPermissionLevel() {
		return permissionLevel;
	}

	public void setPermissionLevel(int permissionLevel) {
		this.permissionLevel = permissionLevel;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ActionUser that = (ActionUser) o;
        return Objects.equals(action, that.action) &&
               Objects.equals(user, that.user);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(action, user);
    }
}