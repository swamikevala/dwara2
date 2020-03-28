package org.ishafoundation.dwaraapi.db.model.master;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassRequesttypeUser;
import org.ishafoundation.dwaraapi.db.model.master.jointables.RequesttypeUser;
import org.ishafoundation.dwaraapi.db.model.master.reference.Requesttype;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="user")
public class User {
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name")
	private String name;

	@Column(name="hash")
	private String hash;	
	
	// Many user can use one particular priority band
	@ManyToOne
	private Priorityband priorityband;
	
    @OneToMany(mappedBy = "user",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<RequesttypeUser> requesttypes = new ArrayList<>();

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<LibraryclassRequesttypeUser> libraryclassRequesttypeUser = new ArrayList<>();     
    
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonIgnore
	public String getHash() {
		return hash;
	}

	@JsonIgnore
	public void setHash(String hash) {
		this.hash = hash;
	}

	@JsonIgnore
	public Priorityband getPriorityband() {
		return priorityband;
	}

	@JsonIgnore
	public void setPriorityband(Priorityband priorityband) {
		this.priorityband = priorityband;
	}
	
	@JsonIgnore
    public List<RequesttypeUser> getRequesttypes() {
		return requesttypes;
	}

	@JsonIgnore
	public void setRequesttypes(List<RequesttypeUser> requesttypes) {
		this.requesttypes = requesttypes;
	}

	public void addRequesttype(Requesttype requesttype, int permissionLevel) {
        RequesttypeUser requesttypeUser = new RequesttypeUser(requesttype, this);
        requesttypeUser.setPermissionLevel(permissionLevel);
        requesttypes.add(requesttypeUser);
        requesttype.getUsers().add(requesttypeUser);
    }
    
    public void removeRequesttype(Requesttype requesttype) {
        for (Iterator<RequesttypeUser> iterator = requesttypes.iterator();
             iterator.hasNext(); ) {
            RequesttypeUser requesttypeUser = iterator.next();
 
            if (requesttypeUser.getUser().equals(this) &&
                    requesttypeUser.getRequesttype().equals(requesttype)) {
                iterator.remove();
                requesttypeUser.getRequesttype().getUsers().remove(requesttypeUser);
                requesttypeUser.setUser(null);
                requesttypeUser.setRequesttype(null);
            }
        }
    }
	
	@JsonIgnore
	public List<LibraryclassRequesttypeUser> getLibraryclassRequesttypeUser() {
		return libraryclassRequesttypeUser;
	}

	@JsonIgnore
	public void setLibraryclassRequesttypeUser(List<LibraryclassRequesttypeUser> libraryclassRequesttypeUser) {
		this.libraryclassRequesttypeUser = libraryclassRequesttypeUser;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        User user = (User) o;
        return Objects.equals(name, user.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}


