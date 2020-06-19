package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.ArtifactclassDestinationpathKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Destinationpath;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;

@Entity(name = "ArtifactclassDestinationpath")
@Table(name="artifactclass_destinationpath")
public class ArtifactclassDestinationpath {

	@EmbeddedId
	private ArtifactclassDestinationpathKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("artifactclassId")
    Artifactclass artifactclass;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("destinationpathId")
    Destinationpath destinationpath;

	public ArtifactclassDestinationpath() {
		
	}

	public ArtifactclassDestinationpath(Artifactclass artifactclass, Destinationpath destinationpath) {
		this.artifactclass = artifactclass;
		this.destinationpath = destinationpath;
		this.id = new ArtifactclassDestinationpathKey(artifactclass.getId(), destinationpath.getId());
	}
	
    public ArtifactclassDestinationpathKey getId() {
		return id;
	}

	public void setId(ArtifactclassDestinationpathKey id) {
		this.id = id;
	}

	public Artifactclass getArtifactclass() {
		return artifactclass;
	}

	public void setArtifactclass(Artifactclass artifactclass) {
		this.artifactclass = artifactclass;
	}

	public Destinationpath getDestinationpath() {
		return destinationpath;
	}

	public void setDestinationpath(Destinationpath destinationpath) {
		this.destinationpath = destinationpath;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ArtifactclassDestinationpath that = (ArtifactclassDestinationpath) o;
        return Objects.equals(artifactclass, that.artifactclass) &&
               Objects.equals(destinationpath, that.destinationpath);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(artifactclass, destinationpath);
    }

}