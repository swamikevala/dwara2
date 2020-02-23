package org.ishafoundation.dwaraapi.db.model.master.storage;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="tapeset")
public class Tapeset {

	@Id
	@Column(name="tapeset_id")
	private int tapesetId;
	
	@Column(name="name")
	private String name;

	@Column(name="barcode_prefix")
	private String barcodePrefix;

	@Column(name="storageformat_id")
	private int storageformatId;

		
	public int getTapesetId() {
		return tapesetId;
	}

	public void setTapesetId(int tapesetId) {
		this.tapesetId = tapesetId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getBarcodePrefix() {
		return barcodePrefix;
	}

	public void setBarcodePrefix(String barcodePrefix) {
		this.barcodePrefix = barcodePrefix;
	}
	
	public int getStorageformatId() {
		return storageformatId;
	}

	public void setStorageformatId(int storageformatId) {
		this.storageformatId = storageformatId;
	}

}