package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ArtifactclassDestinationKey implements Serializable {
 
	private static final long serialVersionUID = -620517854806310403L;

	@Column(name = "artifactclass_id")
    private String artifactclassId;
 
    @Column(name = "destination_id")
    private String destinationId;
 
    public ArtifactclassDestinationKey() {}
    
    public ArtifactclassDestinationKey(
        String artifactclassId,
        String destinationId) {
        this.artifactclassId = artifactclassId;
        this.destinationId = destinationId;
    }
 
	public String getArtifactclassId() {
		return artifactclassId;
	}

	public void setArtifactclassId(String artifactclassId) {
		this.artifactclassId = artifactclassId;
	}

	public String getDestinationpathId() {
		return destinationId;
	}

	public void setDestinationpathId(String destinationId) {
		this.destinationId = destinationId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ArtifactclassDestinationKey that = (ArtifactclassDestinationKey) o;
        return Objects.equals(artifactclassId, that.artifactclassId) &&
               Objects.equals(destinationId, that.destinationId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(artifactclassId, destinationId);
    }
}