package org.ishafoundation.dwaraapi.db.model.transactional.domain;
		
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificFileFactory;


@Entity(name="File1")
@SequenceGenerator(initialValue = 1, name = "file_sequence", allocationSize = 1)
@Table(name=File.TABLE_NAME_PREFIX + "1")
public class File1 extends File{
    static {
    	DomainSpecificFileFactory.register(TABLE_NAME_PREFIX + "1", File1.class);
    }
    
	// Many file1s from the same artifact1
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="artifact_id")
	private Artifact1 artifact1;

	@OneToOne
	@JoinColumn(name="file_ref_id")
	private File1 file1Ref;

	public Artifact1 getArtifact1() {
		return artifact1;
	}

	public void setArtifact1(Artifact1 artifact1) {
		this.artifact1 = artifact1;
	}

	public File1 getFile1Ref() {
		return file1Ref;
	}

	public void setFile1Ref(File1 file1Ref) {
		this.file1Ref = file1Ref;
	}

//	@Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        File1 file1 = (File1) o;
//        return Objects.equals(getPathname(), file1.getPathname());
//    }
// 
//    @Override
//    public int hashCode() {
//        return Objects.hash(getPathname());
//    }
}