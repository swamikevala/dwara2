package org.ishafoundation.dwaraapi.db.model.master.reference;
		
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassRequesttypeUser;
import org.ishafoundation.dwaraapi.db.model.master.jointables.RequesttypeUser;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name="requesttype")
public class Requesttype {

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name")
	private String name;
	
    @OneToMany(mappedBy = "requesttype",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<RequesttypeUser> users = new ArrayList<>();
    
    @OneToMany(mappedBy = "requesttype",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<LibraryclassRequesttypeUser> libraryclassRequesttypeUser = new ArrayList<>();     

	public Requesttype() {}
	
	public Requesttype(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
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
	public List<RequesttypeUser> getUsers() {
		return users;
	}
	
	@JsonIgnore
	public void setUsers(List<RequesttypeUser> users) {
		this.users = users;
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
        if (o == null || getClass() != o.getClass()) return false;
        Requesttype requesttype = (Requesttype) o;
        return Objects.equals(name, requesttype.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}