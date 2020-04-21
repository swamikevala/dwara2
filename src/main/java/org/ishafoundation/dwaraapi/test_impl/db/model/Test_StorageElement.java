package org.ishafoundation.dwaraapi.test_impl.db.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.Tapelibrary;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="test_storage_element")
public class Test_StorageElement {
	
	@Id
	private int id;
	
	private int sNo;
	
	private boolean importExport;
	
	private boolean empty;
	
	private String volumeTag;

	@ManyToOne(fetch = FetchType.LAZY)
	private Tapelibrary tapelibrary;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
	
	@JsonIgnore
	public Tapelibrary getTapelibrary() {
		return tapelibrary;
	}

	@JsonIgnore
	public void setTapelibrary(Tapelibrary tapelibrary) {
		this.tapelibrary = tapelibrary;
	}
	
	@Override
	public String toString() {
		return "sNo : " + sNo + " vt : " + volumeTag;
	}
}
