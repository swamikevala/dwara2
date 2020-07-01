package org.ishafoundation.dwaraapi.db.model.transactional.domain;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@MappedSuperclass
public class File {

	public static final String TABLE_NAME_PREFIX = "file";
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="id")
	private int id;
	
	@Column(name="pathname", unique = true)
	private String pathname;

	@Column(name="checksum")
	private Long checksum;

	@Column(name="size")
	private long size;

	@Column(name="deleted")
	private boolean deleted;
	
//	// Many file1s from the same artifact1
//	@ManyToOne(fetch = FetchType.LAZY)
//	private Artifact artifact;
//
//	@OneToOne
//	@JoinColumn(name="file_ref_id")
//	private File fileRef;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPathname() {
		return pathname;
	}

	public void setPathname(String pathname) {
		this.pathname = pathname;
	}

	public Long getChecksum() {
		return checksum;
	}

	public void setChecksum(Long checksum) {
		this.checksum = checksum;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

//	public Artifact getArtifact() {
//		return artifact;
//	}
//
//	public void setArtifact(Artifact artifact) {
//		this.artifact = artifact;
//	}
//
//	public File getFileRef() {
//		return fileRef;
//	}
//
//	public void setFileRef(File fileRef) {
//		this.fileRef = fileRef;
//	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        File file = (File) o;
        return Objects.equals(pathname, file.pathname);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(pathname);
    }
}