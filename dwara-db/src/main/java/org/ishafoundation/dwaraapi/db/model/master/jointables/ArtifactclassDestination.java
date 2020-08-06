package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.ArtifactclassDestinationKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Destination;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;

@Entity(name = "ArtifactclassDestination")
@Table(name="artifactclass_destination")
public class ArtifactclassDestination {

	@EmbeddedId
	private ArtifactclassDestinationKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("artifactclassId")
    Artifactclass artifactclass;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("destinationId")
    Destination destination;

	public ArtifactclassDestination() {
		
	}

	public ArtifactclassDestination(Artifactclass artifactclass, Destination destination) {
		this.artifactclass = artifactclass;
		this.destination = destination;
		this.id = new ArtifactclassDestinationKey(artifactclass.getId(), destination.getId());
	}
	
    public ArtifactclassDestinationKey getId() {
		return id;
	}

	public void setId(ArtifactclassDestinationKey id) {
		this.id = id;
	}

	public Artifactclass getArtifactclass() {
		return artifactclass;
	}

	public void setArtifactclass(Artifactclass artifactclass) {
		this.artifactclass = artifactclass;
	}

	public Destination getDestinationpath() {
		return destination;
	}

	public void setDestinationpath(Destination destination) {
		this.destination = destination;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ArtifactclassDestination that = (ArtifactclassDestination) o;
        return Objects.equals(artifactclass, that.artifactclass) &&
               Objects.equals(destination, that.destination);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(artifactclass, destination);
    }

}