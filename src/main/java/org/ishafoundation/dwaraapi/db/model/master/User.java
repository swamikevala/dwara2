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

import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassActionUser;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ActionUser;
import org.ishafoundation.dwaraapi.db.model.master.reference.Action;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="user")
public class User {
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name", unique=true)
	private String name;

	@Column(name="hash")
	private String hash;	
	
	// Many user can use one particular priority band
	@ManyToOne
	private Priorityband priorityband;
	
    @OneToMany(mappedBy = "user",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ActionUser> actions = new ArrayList<>();

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<LibraryclassActionUser> libraryclassActionUser = new ArrayList<>();     
    
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
    public List<ActionUser> getActions() {
		return actions;
	}

	@JsonIgnore
	public void setActions(List<ActionUser> actions) {
		this.actions = actions;
	}

	public void addAction(Action action, int permissionLevel) {
        ActionUser actionUser = new ActionUser(action, this);
        actionUser.setPermissionLevel(permissionLevel);
        actions.add(actionUser);
        action.getUsers().add(actionUser);
    }
    
    public void removeAction(Action action) {
        for (Iterator<ActionUser> iterator = actions.iterator();
             iterator.hasNext(); ) {
            ActionUser actionUser = iterator.next();
 
            if (actionUser.getUser().equals(this) &&
                    actionUser.getAction().equals(action)) {
                iterator.remove();
                actionUser.getAction().getUsers().remove(actionUser);
                actionUser.setUser(null);
                actionUser.setAction(null);
            }
        }
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


