package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ImportVolumeArtifactKey implements Serializable {

	private static final long serialVersionUID = 3094076589216752917L;

    @Column(name = "volume_id")
    private String volumeId;
    
    @Column(name = "artifact_name")
    private String artifactName;
 
    public ImportVolumeArtifactKey() {}
    
    public ImportVolumeArtifactKey(String volumeId, String artifactName) {
        this.volumeId = volumeId;
        this.artifactName = artifactName;
    }

	public String getVolumeId() {
		return volumeId;
	}

	public void setVolumeId(String volumeId) {
		this.volumeId = volumeId;
	}

	public String getArtifactName() {
		return artifactName;
	}

	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ImportVolumeArtifactKey that = (ImportVolumeArtifactKey) o;
        return Objects.equals(volumeId, that.volumeId) &&
               Objects.equals(artifactName, that.artifactName);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(volumeId, artifactName);
    }
}