package org.ishafoundation.dwaraapi.db.model.transactional;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="library")
public class Library {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="library_id")
	private int libraryId;

	@Column(name="libraryclass_id")
	private int libraryclassId;

	@Column(name="name")
	private String name;

	@Column(name="file_count")
	private int fileCount;

	@Column(name="total_size")
	private double totalSize;
	
	@Column(name="deleted")
	private boolean deleted;

	@Column(name="file_structure_md5")
	private String fileStructureMd5;

	@Column(name="library_id_ref")
	private int libraryIdRef;

		
	public int getLibraryId() {
		return libraryId;
	}

	public void setLibraryId(int libraryId) {
		this.libraryId = libraryId;
	}
	
	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}
	
	public double getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(double totalSize) {
		this.totalSize = totalSize;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public String getFileStructureMd5() {
		return fileStructureMd5;
	}

	public void setFileStructureMd5(String fileStructureMd5) {
		this.fileStructureMd5 = fileStructureMd5;
	}
	
	public int getLibraryIdRef() {
		return libraryIdRef;
	}

	public void setLibraryIdRef(int libraryIdRef) {
		this.libraryIdRef = libraryIdRef;
	}

}