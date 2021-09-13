package org.ishafoundation.dwaraapi.db.model.master.configuration;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="role")
public class Role {
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name", unique=true)
	private String name;

	@Column(name="description")
	private String description;	

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

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        Role user = (Role) o;
        return Objects.equals(name, user.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}


