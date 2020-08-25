package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ArtifactclassVolumeKey implements Serializable {

	private static final long serialVersionUID = 4007063517540743995L;

	@Column(name = "artifactclass_id")
    private String artifactclassId;
 
    @Column(name = "volume_id")
    private String volumeId;
 
    public ArtifactclassVolumeKey() {}
    
    public ArtifactclassVolumeKey(
        String artifactclassId,
        String volumeId) {
        this.artifactclassId = artifactclassId;
        this.volumeId = volumeId;
    }
 
	public String getArtifactclassId() {
		return artifactclassId;
	}

	public void setArtifactclassId(String artifactclassId) {
		this.artifactclassId = artifactclassId;
	}

	public String getVolumeId() {
		return volumeId;
	}

	public void setVolumeId(String volumeId) {
		this.volumeId = volumeId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ArtifactclassVolumeKey that = (ArtifactclassVolumeKey) o;
        return Objects.equals(artifactclassId, that.artifactclassId) &&
               Objects.equals(volumeId, that.volumeId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(artifactclassId, volumeId);
    }
}
