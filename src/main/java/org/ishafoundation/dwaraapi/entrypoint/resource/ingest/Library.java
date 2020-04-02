package org.ishafoundation.dwaraapi.entrypoint.resource.ingest;
		
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;

public class Library {

	private int id;

	private Libraryclass libraryclass;

	private String name;

	private int fileCount;

	private double totalSize;
	
	private boolean deleted;

	private String fileStructureMd5;

	private Integer libraryRefId;
	
	private Integer qLatestSubrequestId;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Libraryclass getLibraryclass() {
		return libraryclass;
	}

	public void setLibraryclass(Libraryclass libraryclass) {
		this.libraryclass = libraryclass;
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

	public Integer getLibraryRefId() {
		return libraryRefId;
	}

	public void setLibraryRefId(Integer libraryRefId) {
		this.libraryRefId = libraryRefId;
	}

	public Integer getqLatestSubrequestId() {
		return qLatestSubrequestId;
	}

	public void setqLatestSubrequestId(Integer qLatestSubrequestId) {
		this.qLatestSubrequestId = qLatestSubrequestId;
	}
}