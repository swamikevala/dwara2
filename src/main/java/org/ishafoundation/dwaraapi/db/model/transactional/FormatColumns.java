package org.ishafoundation.dwaraapi.db.model.transactional;

public class FormatColumns {

	private String barcode;
	
	private String storageType;
	
	private boolean force;
	
	
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public String getStorageType() {
		return storageType;
	}
	public void setStorageType(String storageType) {
		this.storageType = storageType;
	}
	public boolean isForce() {
		return force;
	}
	public void setForce(boolean force) {
		this.force = force;
	}
}
