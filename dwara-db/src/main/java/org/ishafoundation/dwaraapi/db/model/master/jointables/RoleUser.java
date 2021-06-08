package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.EmbeddedId;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.RoleUserKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Role;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;

/*
 * 
 * References - 
 * 1) "Bidirectional many-to-many with a link entity" - https://docs.jboss.org/hibernate/orm/5.3/userguide/html_single/Hibernate_User_Guide.html#associations-many-to-many
 * 2) https://vladmihalcea.com/the-best-way-to-map-a-many-to-many-association-with-extra-columns-when-using-jpa-and-hibernate/
 * 3) https://www.baeldung.com/jpa-many-to-many
 * 
 * 
*/
@Table(name="role_user")
public class RoleUser {

	@EmbeddedId
	private RoleUserKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
	private Role role;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
	private User user;

	public RoleUser() {
		
	}
	
	public RoleUser(Role role, User user) {
		this.user = user;
		this.id = new RoleUserKey(role.getId(), user.getId());
	}
	
    public RoleUserKey getId() {
		return id;
	}

	public void setId(RoleUserKey id) {
		this.id = id;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
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
 
        RoleUser that = (RoleUser) o;
        return Objects.equals(role, that.role) &&
               Objects.equals(user, that.user);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(role, user);
    }
}