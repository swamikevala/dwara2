package org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.MapsId;

import org.ishafoundation.dwaraapi.db.keys.domain.ArtifactVolumeKey;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.json.ArtifactVolumeDetails;

/*
 * 
 * References - 
 * 1) "Bidirectional many-to-many with a link entity" - https://docs.jboss.org/hibernate/orm/5.3/volumeguide/html_single/Hibernate_Volume_Guide.html#associations-many-to-many
 * 2) https://vladmihalcea.com/the-best-way-to-map-a-many-to-many-association-with-extra-columns-when-using-jpa-and-hibernate/
 * 3) https://www.baeldung.com/jpa-many-to-many
 * 
 * 
*/
@MappedSuperclass
public class ArtifactVolume {
	
	public static final String TABLE_NAME = Artifact.TABLE_NAME_PREFIX +"<<DOMAIN>>_volume";
	
	@EmbeddedId
	private ArtifactVolumeKey id;

//	@ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("artifact1Id")
//	private Artifact1 artifact1;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("volumeId")
	private Volume volume;
	
	@Lob
	@Column(name="details")
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