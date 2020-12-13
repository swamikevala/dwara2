package org.ishafoundation.dwaraapi.db.model.mocks;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

// @Profile({ "dev | stage" }) wont work for Entity as they are not spring beans. 
// So we will create this table using flyway scripts only for test environment...
@Entity
@Table(name="zmock_data_transfer_element")
public class MockDataTransferElement{// extends DataTransferElement{
	
	@Id
	private int id;
	
	@Column(name="tapelibrary_uid")
	private String tapelibraryUid;
	
	@Column(name="tapedrive_uid", unique = true)
	private String tapedriveUid;
		
	@OneToOne(cascade = CascadeType.PERSIST)
	private MockMtStatus mockMtStatus;

	@Column(name="\"s_num\"")
	private int sNum;
	
	private boolean empty;
	private Integer storageElementNo;
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

	public String getTapedriveUid() {
		return tapedriveUid;
	}

	public void setTapedriveUid(String tapedriveUid) {
		this.tapedriveUid = tapedriveUid;
	}

	public MockMtStatus getMockMtStatus() {
		return mockMtStatus;
	}

	public void setMockMtStatus(MockMtStatus mockMtStatus) {
		this.mockMtStatus = mockMtStatus;
	}

	


	public int getsNo() {
		return sNum;
	}

	public void setsNo(int sNo) {
		this.sNum = sNo;
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
	@Override
	public String toString() {
		return "sNo : " + getsNo() + " empty : " + isEmpty() + " sen : " + getStorageElementNo() + " vt : " + getVolumeTag();
	}
}
