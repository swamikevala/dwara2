package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.LibraryclassRequesttypeUserKey;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.User;
import org.ishafoundation.dwaraapi.db.model.master.reference.Requesttype;

@Entity(name = "LibraryclassRequesttypeUser")
@Table(name="libraryclass_requesttype_user")
public class LibraryclassRequesttypeUser {

	@EmbeddedId
	private LibraryclassRequesttypeUserKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("libraryclassId")
	private Libraryclass libraryclass;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("requesttypeId")
	private Requesttype requesttype;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
	private User user;
	
	
	public LibraryclassRequesttypeUser() {
		
	}

	public LibraryclassRequesttypeUser(Libraryclass libraryclass, Requesttype requesttype, User user) {
		this.libraryclass = libraryclass;
		this.requesttype = requesttype;
		this.user = user;
		this.id = new LibraryclassRequesttypeUserKey(libraryclass.getId(), requesttype.getId(), user.getId());
	}
	
    public LibraryclassRequesttypeUserKey getId() {
		return id;
	}

	public void setId(LibraryclassRequesttypeUserKey id) {
		this.id = id;
	}

	public Libraryclass getLibraryclass() {
		return libraryclass;
	}

	public void setLibraryclass(Libraryclass libraryclass) {
		this.libraryclass = libraryclass;
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

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        LibraryclassRequesttypeUser that = (LibraryclassRequesttypeUser) o;
        return Objects.equals(libraryclass, that.libraryclass) &&
        		Objects.equals(requesttype, that.requesttype) &&
                Objects.equals(user, that.user);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(libraryclass, requesttype, user);
    }

}