package org.ishafoundation.dwaraapi.db.model.master.configuration;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.cache.Cacheable;

@Entity
@Table(name="location")
public class Location implements Cacheable{
	@Id
	@Column(name="id")
	private String id;
	
	@Column(name="name", unique = true)
	private String name;
	
	@Column(name="restore_default")
	private boolean restoreDefault;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRestoreDefault() {
		return restoreDefault;
	}

	public void setRestoreDefault(boolean restoreDefault) {
		this.restoreDefault = restoreDefault;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location domain = (Location) o;
        return Objects.equals(name, domain.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
