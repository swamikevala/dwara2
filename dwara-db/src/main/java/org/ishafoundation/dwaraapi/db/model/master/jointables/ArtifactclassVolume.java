package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.ArtifactclassVolumeKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;

@Entity(name = "ArtifactclassVolume")
@Table(name="artifactclass_volume")
public class ArtifactclassVolume {

	@EmbeddedId
	private ArtifactclassVolumeKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("artifactclassId")
	private Artifactclass artifactclass;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("volumeId")
	private Volume volume;
	
	@Column(name="encrypted")
	private boolean encrypted;
	
	@Column(name="active")
	private boolean active;
	
	public ArtifactclassVolume() {
		
	}

	public ArtifactclassVolume(Artifactclass artifactclass, Volume volume) {
		this.artifactclass = artifactclass;
		this.volume = volume;
		this.id = new ArtifactclassVolumeKey(artifactclass.getId(), volume.getId());
	}
	
    public ArtifactclassVolumeKey getId() {
		return id;
	}

	public void setId(ArtifactclassVolumeKey id) {
		this.id = id;
	}

	public Artifactclass getArtifactclass() {
		return artifactclass;
	}

	public void setArtifactclass(Artifactclass artifactclass) {
		this.artifactclass = artifactclass;
	}

	public Volume getVolume() {
		return volume;
	}

	public void setVolume(Volume volume) {
		this.volume = volume;
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ArtifactclassVolume that = (ArtifactclassVolume) o;
        return Objects.equals(artifactclass, that.artifactclass) &&
               Objects.equals(volume, that.volume);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(artifactclass, volume);
    }

}