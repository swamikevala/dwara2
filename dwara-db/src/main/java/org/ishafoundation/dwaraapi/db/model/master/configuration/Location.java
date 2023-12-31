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
	
	@Column(name="description", unique = true)
	private String description;
	
	@Column(name="\"default\"")
	private boolean default_;

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDefault_() {
		return default_;
	}

	public void setDefault_(boolean default_) {
		this.default_ = default_;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(id, location.id);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
