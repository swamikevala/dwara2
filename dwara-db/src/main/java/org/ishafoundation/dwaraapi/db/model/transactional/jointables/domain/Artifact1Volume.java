package org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain;
		
import javax.persistence.Entity;
import javax.persistence.Table;

/*
 * 
 * References - 
 * 1) "Bidirectional many-to-many with a link entity" - https://docs.jboss.org/hibernate/orm/5.3/volumeguide/html_single/Hibernate_Volume_Guide.html#associations-many-to-many
 * 2) https://vladmihalcea.com/the-best-way-to-map-a-many-to-many-association-with-extra-columns-when-using-jpa-and-hibernate/
 * 3) https://www.baeldung.com/jpa-many-to-many
 * 
 * 
*/
@Entity(name = "Artifact1Volume")
@Table(name="artifact1_volume")//@Table(name="artifact_volume")
public class Artifact1Volume extends ArtifactVolume{

}