package org.ishafoundation.dwaraapi.db.model.transactional.domain;
		
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@Entity(name="File1")
@Table(name="file1")
public class File1 extends File{
	
	// Many file1s from the same artifact1
	@ManyToOne(fetch = FetchType.LAZY)
	private Artifact1 artifact1;

	@OneToOne
	@JoinColumn(name="file1_ref_id")
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