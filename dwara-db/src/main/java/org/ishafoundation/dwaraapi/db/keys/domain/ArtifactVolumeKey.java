package org.ishafoundation.dwaraapi.db.keys.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ArtifactVolumeKey implements Serializable {

	private static final long serialVersionUID = 8051084463733905503L;

	@Column(name = "artifact_id")
    private int artifactId;
 
    @Column(name = "volume_id")
    private int volumeId;
 
    public ArtifactVolumeKey() {}
    
    public ArtifactVolumeKey(
        int artifactId,
        int volumeId) {
        this.artifactId = artifactId;
        this.volumeId = volumeId;
    }
 
    public int getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(int artifactId) {
		this.artifactId = artifactId;
	}

	public int getVolumeId() {
		return volumeId;
	}

	public void setVolumeId(int volumeId) {
		this.volumeId = volumeId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ArtifactVolumeKey that = (ArtifactVolumeKey) o;
        return Objects.equals(artifactId, that.artifactId) &&
               Objects.equals(volumeId, that.volumeId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(artifactId, volumeId);
    }
}