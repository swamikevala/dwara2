package org.ishafoundation.dwaraapi.db.model.transactional.domain;
		
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificArtifactFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@SequenceGenerator(initialValue = 1000, name = "artifact_sequence", allocationSize = 1)
@Table(name=Artifact.TABLE_NAME_PREFIX + "1")
public class Artifact1 extends Artifact{
    static {
    	DomainSpecificArtifactFactory.register(TABLE_NAME_PREFIX + "1", Artifact1.class);
    }

	// Causes cyclic associations 
	// Many derived artifact1 reference the same parent artifact1 - Hence many to one
 	@ManyToOne
	@JoinColumn(name="artifact_ref_id")
	private Artifact1 artifact1Ref;
    
	@JsonIgnore
	public Artifact1 getArtifact1Ref() {
		return artifact1Ref;
	}

	@JsonIgnore
	public void setArtifact1Ref(Artifact1 artifact1Ref) {
		this.artifact1Ref = artifact1Ref;
	}

	public int getArtifact1RefId() {
		return this.artifact1Ref != null ? this.artifact1Ref.getId() : 0;
	}
	
//	@Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Artifact1 artifact1 = (Artifact1) o;
//        return Objects.equals(fileStructureMd5, artifact1.fileStructureMd5);
//    }
// 
//    @Override
//    public int hashCode() {
//        return Objects.hash(fileStructureMd5);
//    }
}