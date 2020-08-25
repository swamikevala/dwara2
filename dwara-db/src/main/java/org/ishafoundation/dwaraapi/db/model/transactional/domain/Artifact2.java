package org.ishafoundation.dwaraapi.db.model.transactional.domain;
		
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificArtifactFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@SequenceGenerator(initialValue = 1, name = "artifact")
@Table(name=Artifact.TABLE_NAME_PREFIX + "2")
public class Artifact2 extends Artifact {
    static {
    	DomainSpecificArtifactFactory.register(TABLE_NAME_PREFIX + "2", Artifact2.class);
    }
    
	// Causes cyclic associations 
	// Many derived artifact2 reference the same parent artifact2 - Hence many to one
 	@ManyToOne
	@JoinColumn(name="artifact_ref_id")
	private Artifact2 artifact2Ref;

 	@JsonIgnore
	public Artifact2 getArtifact2Ref() {
		return artifact2Ref;
	}

	@JsonIgnore
	public void setArtifact2Ref(Artifact2 artifact2Ref) {
		this.artifact2Ref = artifact2Ref;
	}

	public int getArtifact2RefId() {
		return this.artifact2Ref != null ? this.artifact2Ref.getId() : 0;
	}
	
	
//	@Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Artifact2 artifact2 = (Artifact2) o;
//        return Objects.equals(fileStructureMd5, artifact2.fileStructureMd5);
//    }
// 
//    @Override
//    public int hashCode() {
//        return Objects.hash(fileStructureMd5);
//    }
}