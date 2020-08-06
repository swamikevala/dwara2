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
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassDestination;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name="Destination")
@Table(name="destination")
public class Destination{
	
	@Id
	@Column(name="id")
	private String id;
	
	@Column(name="path")
	private String path;	

    @OneToMany(mappedBy = "destination",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    List<ArtifactclassDestination> artifactclassDestination = new ArrayList<>();
    
    @Column(name="use_buffering")
    private boolean useBuffering;

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public boolean isUseBuffering() {
		return useBuffering;
	}

	public void setUseBuffering(boolean useBuffering) {
		this.useBuffering = useBuffering;
	}

	@JsonIgnore
	public List<ArtifactclassDestination> getArtifactclassDestination() {
		return artifactclassDestination;
	}
	
	@JsonIgnore
	public void setArtifactclassDestination(List<ArtifactclassDestination> artifactclassDestination) {
		this.artifactclassDestination = artifactclassDestination;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Destination destination = (Destination) o;
        return Objects.equals(id, destination.id);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }	
}
