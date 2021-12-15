package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ArtifactFlagKey implements Serializable {

	private static final long serialVersionUID = 6231347795977603021L;

	@Column(name = "artifact_id")
    private int artifactId;
 
    @Column(name = "flag_id")
    private int flagId;
 
    public ArtifactFlagKey() {}
    
    public ArtifactFlagKey(
        int artifactId,
        int flagId) {
        this.artifactId = artifactId;
        this.flagId = flagId;
    }
 
    public int getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(int artifactId) {
		this.artifactId = artifactId;
	}


	public int getFlagId() {
		return flagId;
	}

	public void setFlagId(int flagId) {
		this.flagId = flagId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ArtifactFlagKey that = (ArtifactFlagKey) o;
        return Objects.equals(artifactId, that.artifactId) &&
               Objects.equals(flagId, that.flagId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(artifactId, flagId);
    }
}