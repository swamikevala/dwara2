package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.LibraryclassActionUserKey;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.User;
import org.ishafoundation.dwaraapi.db.model.master.reference.Action;

@Entity(name = "LibraryclassActionUser")
@Table(name="libraryclass_action_user")
public class LibraryclassActionUser {

	@EmbeddedId
	private LibraryclassActionUserKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("libraryclassId")
	private Libraryclass libraryclass;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("actionId")
	private Action action;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
	private User user;
	
	
	public LibraryclassActionUser() {
		
	}

	public LibraryclassActionUser(Libraryclass libraryclass, Action action, User user) {
		this.libraryclass = libraryclass;
		this.action = action;
		this.user = user;
		this.id = new LibraryclassActionUserKey(libraryclass.getId(), action.getId(), user.getId());
	}
	
    public LibraryclassActionUserKey getId() {
		return id;
	}

	public void setId(LibraryclassActionUserKey id) {
		this.id = id;
	}

	public Libraryclass getLibraryclass() {
		return libraryclass;
	}

	public void setLibraryclass(Libraryclass libraryclass) {
		this.libraryclass = libraryclass;
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
 
        LibraryclassActionUser that = (LibraryclassActionUser) o;
        return Objects.equals(libraryclass, that.libraryclass) &&
        		Objects.equals(action, that.action) &&
                Objects.equals(user, that.user);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(libraryclass, action, user);
    }

}