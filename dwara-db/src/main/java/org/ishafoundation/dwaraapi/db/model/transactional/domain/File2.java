package org.ishafoundation.dwaraapi.db.model.transactional.domain;
		
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificFileFactory;


@Entity
@SequenceGenerator(initialValue = 1, name = "file")
@Table(name=File.TABLE_NAME_PREFIX + "2")
public class File2 extends File {
    static {
    	DomainSpecificFileFactory.register(TABLE_NAME_PREFIX + "2", File2.class);
    }
    
    // Many file2s from the same artifact2
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="artifact_id")
	private Artifact2 artifact2;

	@OneToOne
	@JoinColumn(name="file_ref_id")
	private File2 file2Ref;

	public Artifact2 getArtifact2() {
		return artifact2;
	}

	public void setArtifact2(Artifact2 artifact2) {
		this.artifact2 = artifact2;
	}

	public File2 getFile2Ref() {
		return file2Ref;
	}

	public void setFile2Ref(File2 file2Ref) {
		this.file2Ref = file2Ref;
	}
    
//	// Many file2s from the same artifact2
//	@ManyToOne(fetch = FetchType.LAZY)
//	private Artifact2 artifact;
//	
//	@OneToOne
//	@JoinColumn(name="file_ref_id")
//	private File2 fileRef;
//
//	public Artifact2 getArtifact() {
//		return artifact;
//	}
//
//	public void setArtifact(Artifact2 artifact) {
//		this.artifact = artifact;
//	}
//
//	public File2 getFileRef() {
//		return fileRef;
//	}
//
//	public void setFileRef(File2 fileRef) {
//		this.fileRef = fileRef;
//	}
    

//	@Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        File2 file2 = (File2) o;
//        return Objects.equals(getPathname(), file2.getPathname());
//    }
// 
//    @Override
//    public int hashCode() {
//        return Objects.hash(getPathname());
//    }
}