package org.ishafoundation.dwaraapi.db.model.master.configuration;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.cache.Cacheable;

@Entity
@Table(name="copy")
public class Copy {
	@Id
	@Column(name="id")
	private int id;
	
	@ManyToOne(fetch = FetchType.LAZY) // Many copies can be in same location
	private Location location;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Copy location = (Copy) o;
        return Objects.equals(id, location.id);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
