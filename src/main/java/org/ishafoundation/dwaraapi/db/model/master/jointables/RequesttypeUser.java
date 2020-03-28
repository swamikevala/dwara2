package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.RequesttypeUserKey;
import org.ishafoundation.dwaraapi.db.model.master.User;
import org.ishafoundation.dwaraapi.db.model.master.reference.Requesttype;

/*
 * 
 * References - 
 * 1) "Bidirectional many-to-many with a link entity" - https://docs.jboss.org/hibernate/orm/5.3/userguide/html_single/Hibernate_User_Guide.html#associations-many-to-many
 * 2) https://vladmihalcea.com/the-best-way-to-map-a-many-to-many-association-with-extra-columns-when-using-jpa-and-hibernate/
 * 3) https://www.baeldung.com/jpa-many-to-many
 * 
 * 
*/
@Entity(name = "RequesttypeUser")
@Table(name="requesttype_user")
public class RequesttypeUser {

	@EmbeddedId
	private RequesttypeUserKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("requesttypeId")
	private Requesttype requesttype;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
	private User user;
	
	@Column(name="permission_level")
	private int permissionLevel;

	public RequesttypeUser() {
		
	}
	
	public RequesttypeUser(Requesttype requesttype, User user) {
		this.requesttype = requesttype;
		this.user = user;
		this.id = new RequesttypeUserKey(requesttype.getId(), user.getId());
	}
	
	public RequesttypeUser(Requesttype requesttype, User user, int permissionLevel) {
		this(requesttype, user);
		this.permissionLevel = permissionLevel;
	}
	
    public RequesttypeUserKey getId() {
		return id;
	}

	public void setId(RequesttypeUserKey id) {
		this.id = id;
	}

	public Requesttype getRequesttype() {
		return requesttype;
	}

	public void setRequesttype(Requesttype requesttype) {
		this.requesttype = requesttype;
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
 
        RequesttypeUser that = (RequesttypeUser) o;
        return Objects.equals(requesttype, that.requesttype) &&
               Objects.equals(user, that.user);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(requesttype, user);
    }
}