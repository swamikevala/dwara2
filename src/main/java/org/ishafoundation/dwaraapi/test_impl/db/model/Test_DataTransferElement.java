package org.ishafoundation.dwaraapi.test_impl.db.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.Tapelibrary;

import com.fasterxml.jackson.annotation.JsonIgnore;

// @Profile({ "dev | test" }) wont work for Entity as they are not spring beans. 
// So we will create this table using flyway scripts
@Entity
@Table(name="test_data_transfer_element")
public class Test_DataTransferElement {
	
	@Id
	private int id;
	
	private int sNo;
	
	private boolean empty;
	
	private Integer storageElementNo;
	
	private String volumeTag;

	@ManyToOne(fetch = FetchType.LAZY)
	private Tapelibrary tapelibrary;
	
	@OneToOne(fetch = FetchType.LAZY)
	private Test_MtStatus test_MtStatus;

	
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

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public Integer getStorageElementNo() {
		return storageElementNo;
	}

	public void setStorageElementNo(Integer storageElementNo) {
		this.storageElementNo = storageElementNo;
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
	
	@JsonIgnore
	public Test_MtStatus getTest_MtStatus() {
		return test_MtStatus;
	}
	
	@JsonIgnore
	public void setTest_MtStatus(Test_MtStatus test_MtStatus) {
		this.test_MtStatus = test_MtStatus;
	}

	@Override
	public String toString() {
		return "sNo : " + sNo + " empty : " + empty + " sen : " + storageElementNo + " vt : " + volumeTag;
	}
}