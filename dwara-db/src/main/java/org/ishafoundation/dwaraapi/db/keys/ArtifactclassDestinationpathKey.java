package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ArtifactclassDestinationpathKey implements Serializable {
 
	private static final long serialVersionUID = -620517854806310403L;

	@Column(name = "artifactclass_id")
    private int artifactclassId;
 
    @Column(name = "destinationpath_id")
    private int destinationpathId;
 
    public ArtifactclassDestinationpathKey() {}
    
    public ArtifactclassDestinationpathKey(
        int artifactclassId,
        int destinationpathId) {
        this.artifactclassId = artifactclassId;
        this.destinationpathId = destinationpathId;
    }
 
	public int getArtifactclassId() {
		return artifactclassId;
	}

	public void setArtifactclassId(int artifactclassId) {
		this.artifactclassId = artifactclassId;
	}

	public int getDestinationpathId() {
		return destinationpathId;
	}

	public void setDestinationpathId(int destinationpathId) {
		this.destinationpathId = destinationpathId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ArtifactclassDestinationpathKey that = (ArtifactclassDestinationpathKey) o;
        return Objects.equals(artifactclassId, that.artifactclassId) &&
               Objects.equals(destinationpathId, that.destinationpathId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(artifactclassId, destinationpathId);
    }
}