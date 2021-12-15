package org.ishafoundation.dwaraapi.db.model.transactional.jointables;
		
import java.util.Objects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.TypeDef;
import org.ishafoundation.dwaraapi.db.keys.ArtifactFlagKey;

import com.vladmihalcea.hibernate.type.json.JsonStringType;

/*
 * 
 * References - 
 * 1) "Bidirectional many-to-many with a link entity" - https://docs.jboss.org/hibernate/orm/5.3/volumeguide/html_single/Hibernate_Volume_Guide.html#associations-many-to-many
 * 2) https://vladmihalcea.com/the-best-way-to-map-a-many-to-many-association-with-extra-columns-when-using-jpa-and-hibernate/
 * 3) https://www.baeldung.com/jpa-many-to-many
 * 
 * 
*/
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Entity
@Table(name="artifact_flag")
public class ArtifactFlag {
	
	@EmbeddedId
	private ArtifactFlagKey id;

	public ArtifactFlag() {
		
	}
	
	public ArtifactFlag(int artifactId, int flagId) {
		this.id = new ArtifactFlagKey(artifactId, flagId);
	}
	
    public ArtifactFlagKey getId() {
		return id;
	}

	public void setId(ArtifactFlagKey id) {
		this.id = id;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ArtifactFlag that = (ArtifactFlag) o;
        return Objects.equals(id, that.id);
    }
 
    @Override
    public int hashCode() {
    	 return Objects.hash(id);
    }

}