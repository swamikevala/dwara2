package org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain;
		
import javax.persistence.Entity;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificArtifactVolumeFactory;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;

/*
 * 
 * References - 
 * 1) "Bidirectional many-to-many with a link entity" - https://docs.jboss.org/hibernate/orm/5.3/volumeguide/html_single/Hibernate_Volume_Guide.html#associations-many-to-many
 * 2) https://vladmihalcea.com/the-best-way-to-map-a-many-to-many-association-with-extra-columns-when-using-jpa-and-hibernate/
 * 3) https://www.baeldung.com/jpa-many-to-many
 * 
 * 
*/
@Entity
@Table(name="artifact2_volume")
//@Table(name=Artifact.TABLE_NAME_PREFIX + "2_volume")
public class Artifact2Volume extends ArtifactVolume{
    static {
    	//DomainSpecificArtifactVolumeFactory.register(Artifact.TABLE_NAME_PREFIX + "2" + "_volume", Artifact2Volume.class);
    	DomainSpecificArtifactVolumeFactory.register(TABLE_NAME.replace("<<DOMAIN>>", "2"), Artifact2Volume.class);
    }
    
    
    public Artifact2Volume() {
    	super();
	}
    
    public Artifact2Volume(int artifactId, Volume volume) {
    	super(artifactId, volume);
	}

}    