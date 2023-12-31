package org.ishafoundation.dwaraapi.db.model.master.reference;
		
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.cache.Cacheable;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ActionArtifactclassUser;
import org.ishafoundation.dwaraapi.enumreferences.Actiontype;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name="action")
public class Action implements Cacheable{
	@Id
	@Column(name="id")
	private String id;

	@Enumerated(EnumType.STRING)
	@Column(name="type")
	private Actiontype type;
	
	@Column(name="description")
	private String description;
	
//    @OneToMany(mappedBy = "action",
//            cascade = CascadeType.MERGE,
//            orphanRemoval = true)
//    private List<ActionUser> users = new ArrayList<>();
    
    @OneToMany(mappedBy = "action",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ActionArtifactclassUser> artifactclassActionUser = new ArrayList<>();     

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Actiontype getType() {
		return type;
	}

	public void setType(Actiontype type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
//
//	@JsonIgnore
//	public List<ActionUser> getUsers() {
//		return users;
//	}
//	
//	@JsonIgnore
//	public void setUsers(List<ActionUser> users) {
//		this.users = users;
//	}
	
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
        if (o == null || getClass() != o.getClass()) return false;
        Action action = (Action) o;
        return Objects.equals(id, action.id);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}