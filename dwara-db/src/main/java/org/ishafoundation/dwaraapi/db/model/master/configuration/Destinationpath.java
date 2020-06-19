package org.ishafoundation.dwaraapi.db.model.master.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.cache.Cacheable;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassDestinationpath;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name="Destinationpath")
@Table(name="destinationpath")
public class Destinationpath implements Cacheable{
	
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name", unique = true)
	private String name;
	
	@Column(name="path")
	private String path;	

    @OneToMany(mappedBy = "destinationpath",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    List<ArtifactclassDestinationpath> artifactclassDestinationpath = new ArrayList<>();
	

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
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@JsonIgnore
	public List<ArtifactclassDestinationpath> getArtifactclassDestinationpath() {
		return artifactclassDestinationpath;
	}
	
	@JsonIgnore
	public void setArtifactclassDestinationpath(List<ArtifactclassDestinationpath> artifactclassDestinationpath) {
		this.artifactclassDestinationpath = artifactclassDestinationpath;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Destinationpath destinationpath = (Destinationpath) o;
        return Objects.equals(name, destinationpath.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }	
}
