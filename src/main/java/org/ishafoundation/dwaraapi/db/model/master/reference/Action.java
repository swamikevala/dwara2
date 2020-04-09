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

import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassActionUser;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ActionUser;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name="action")
public class Action {

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name")
	private String name;
	
	@Column(name="description")
	private String description;
	
    @OneToMany(mappedBy = "action",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ActionUser> users = new ArrayList<>();
    
    @OneToMany(mappedBy = "action",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<LibraryclassActionUser> libraryclassActionUser = new ArrayList<>();     

	public Action() {}
	
	public Action(int id, String name) {
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
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonIgnore
	public List<ActionUser> getUsers() {
		return users;
	}
	
	@JsonIgnore
	public void setUsers(List<ActionUser> users) {
		this.users = users;
	}
	
	@JsonIgnore
	public List<LibraryclassActionUser> getLibraryclassActionUser() {
		return libraryclassActionUser;
	}

	@JsonIgnore
	public void setLibraryclassActionUser(List<LibraryclassActionUser> libraryclassActionUser) {
		this.libraryclassActionUser = libraryclassActionUser;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Action action = (Action) o;
        return Objects.equals(name, action.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}