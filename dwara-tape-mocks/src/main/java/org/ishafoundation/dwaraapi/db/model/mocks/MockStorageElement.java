package org.ishafoundation.dwaraapi.db.model.mocks;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="zmock_storage_element")
public class MockStorageElement{// extends StorageElement{
	
	@Id
	private int id;

	private String tapelibraryUid;
	
	private int sNo;
	private boolean importExport;
	private boolean empty;
	private String volumeTag;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTapelibraryUid() {
		return tapelibraryUid;
	}

	public void setTapelibraryUid(String tapelibraryUid) {
		this.tapelibraryUid = tapelibraryUid;
	}

	public int getsNo() {
		return sNo;
	}

	public void setsNo(int sNo) {
		this.sNo = sNo;
	}

	public boolean isImportExport() {
		return importExport;
	}

	public void setImportExport(boolean importExport) {
		this.importExport = importExport;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public String getVolumeTag() {
		return volumeTag;
	}

	public void setVolumeTag(String volumeTag) {
		this.volumeTag = volumeTag;
	}
	
	@Override
	public String toString() {
		return "sNo : " + getsNo() + " vt : " + getVolumeTag();
	}
}
