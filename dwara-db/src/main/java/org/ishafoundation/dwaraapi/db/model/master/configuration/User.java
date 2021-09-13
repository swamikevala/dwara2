package org.ishafoundation.dwaraapi.db.model.master.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.jointables.ActionArtifactclassUser;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="user")
public class User {//implements Cacheable{
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
	
	@Column(name="email", unique=true)
	private String email;
	
//    @OneToMany(mappedBy = "user",
//            cascade = CascadeType.MERGE,
//            orphanRemoval = true)
//    private List<ActionUser> actions = new ArrayList<>();

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ActionArtifactclassUser> artifactclassActionUser = new ArrayList<>();     
    
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
//	@JsonIgnore
//    public List<ActionUser> getActions() {
//		return actions;
//	}
//
//	@JsonIgnore
//	public void setActions(List<ActionUser> actions) {
//		this.actions = actions;
//	}
//
//	public void addAction(Action action, int permissionLevel) {
//        ActionUser actionUser = new ActionUser(action, this);
//        actionUser.setPermissionLevel(permissionLevel);
//        actions.add(actionUser);
//        action.getUsers().add(actionUser);
//    }
//    
//    public void removeAction(Action action) {
//        for (Iterator<ActionUser> iterator = actions.iterator();
//             iterator.hasNext(); ) {
//            ActionUser actionUser = iterator.next();
// 
//            if (actionUser.getUser().equals(this) &&
//                    actionUser.getAction().equals(action)) {
//                iterator.remove();
//                actionUser.getAction().getUsers().remove(actionUser);
//                actionUser.setUser(null);
//                actionUser.setAction(null);
//            }
//        }
//    }
	
	@JsonIgnore
	public List<ActionArtifactclassUser> getArtifactclassActionUser() {
		return artifactclassActionUser;
	}

	@JsonIgnore
	public void setArtifactclassActionUser(List<ActionArtifactclassUser> artifactclassActionUser) {
		this.artifactclassActionUser = artifactclassActionUser;
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


