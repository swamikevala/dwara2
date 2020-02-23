package org.ishafoundation.dwaraapi.db.model.transactional;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="file")
public class File {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="file_id")
	private int fileId;
	
	@Column(name="library_id")
	private int libraryId;
	
	@Column(name="pathname")
	private String pathname;

	@Column(name="crc")
	private String crc;

	@Column(name="size")
	private double size;

	@Column(name="deleted")
	private boolean deleted;

	@Column(name="external_id")
	private String externalId;

	@Column(name="file_id_ref")
	private int fileIdRef;

	@Column(name="filetype_id")
	private int filetypeId;

		
	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}
	
	public int getLibraryId() {
		return libraryId;
	}

	public void setLibraryId(int libraryId) {
		this.libraryId = libraryId;
	}

	public String getPathname() {
		return pathname;
	}

	public void setPathname(String pathname) {
		this.pathname = pathname;
	}
	
	public String getCrc() {
		return crc;
	}

	public void setCrc(String crc) {
		this.crc = crc;
	}
	
	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	public int getFileIdRef() {
		return fileIdRef;
	}

	public void setFileIdRef(int fileIdRef) {
		this.fileIdRef = fileIdRef;
	}
	
	public int getFiletypeId() {
		return filetypeId;
	}

	public void setFiletypeId(int filetypeId) {
		this.filetypeId = filetypeId;
	}

}