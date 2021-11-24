package org.ishafoundation.dwaraapi.db.model.transactional.jointables;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ishafoundation.dwaraapi.db.keys.ArtifactVolumeKey;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.ArtifactVolumeDetails;
import org.ishafoundation.dwaraapi.enumreferences.ArtifactVolumeStatus;

import com.vladmihalcea.hibernate.type.json.JsonStringType;

/*
 * 
 * References - 
 * 1) "Bidirectional many-to-many with a link entity" - https://docs.jboss.org/hibernate/orm/5.3/volumeguide/html_single/Hibernate_Volume_Guide.html#associations-many-to-many
 * 2) https://vladmihalcea.com/the-best-way-to-map-a-many-to-many-association-with-extra-columns-when-using-jpa-and-hibernate/
 * 3) https://www.baeldung.com/jpa-many-to-many
 * 
 * 
*/
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Entity
@Table(name="artifact_volume")
public class ArtifactVolume {
	
	@EmbeddedId
	private ArtifactVolumeKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("volumeId")
	private Volume volume;

	@OneToOne(fetch = FetchType.LAZY)
	private Job job;
	
	private String name; // artifact name on volume
	
	@Enumerated(EnumType.STRING)
	@Column(name="status")
	private ArtifactVolumeStatus status;
	
	@Type(type = "json")
	@Column(name="details", columnDefinition = "json")
	private ArtifactVolumeDetails details;


	public ArtifactVolume() {
		
	}
	
	public ArtifactVolume(int artifactId, Volume volume) {
		this.setVolume(volume);
		this.id = new ArtifactVolumeKey(artifactId, volume.getId());
	}
	
	public Volume getVolume() {
		return volume;
	}

	public void setVolume(Volume volume) {
		this.volume = volume;
	}
	
    public ArtifactVolumeKey getId() {
		return id;
	}

	public void setId(ArtifactVolumeKey id) {
		this.id = id;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArtifactVolumeStatus getStatus() {
		return status;
	}

	public void setStatus(ArtifactVolumeStatus status) {
		this.status = status;
	}

	public ArtifactVolumeDetails getDetails() {
		return details;
	}

	public void setDetails(ArtifactVolumeDetails details) {
		this.details = details;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ArtifactVolume that = (ArtifactVolume) o;
        return Objects.equals(id, that.id);
    }
 
    @Override
    public int hashCode() {
    	 return Objects.hash(id);
    }

}