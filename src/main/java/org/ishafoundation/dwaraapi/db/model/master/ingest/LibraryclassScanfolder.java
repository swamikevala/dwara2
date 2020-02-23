package org.ishafoundation.dwaraapi.db.model.master.ingest;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="libraryclass_scanfolder")
public class LibraryclassScanfolder {

	@Id
	@Column(name="libraryclass_scanfolder_id")
	private int libraryclassScanfolderId;
	
	@Column(name="scanfolder_id")
	private int scanfolderId;

	@Column(name="libraryclass_id")
	private int libraryclassId;

		
	public int getLibraryclassScanfolderId() {
		return libraryclassScanfolderId;
	}

	public void setLibraryclassScanfolderId(int libraryclassScanfolderId) {
		this.libraryclassScanfolderId = libraryclassScanfolderId;
	}
	
	public int getScanfolderId() {
		return scanfolderId;
	}

	public void setScanfolderId(int scanfolderId) {
		this.scanfolderId = scanfolderId;
	}
	
	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
	}

}