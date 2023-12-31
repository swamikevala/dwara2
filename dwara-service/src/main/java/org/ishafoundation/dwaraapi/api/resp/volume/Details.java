package org.ishafoundation.dwaraapi.api.resp.volume;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Details {
	private Boolean barcoded;
	private Integer blocksize;
	private String blocksizeUnit;
	private String storagesubtype;
	private String mountPoint = null;
	private String provider = null;
	private Boolean removeAfterJob;
	private boolean expandCapacity; // is capacity to be bumped by adding more volumes?
	private String nextBarcode;

	// Getter Methods 

	public Boolean getBarcoded() {
		return barcoded;
	}

	public Integer getBlocksize() {
		return blocksize;
	}

	public String getBlocksizeUnit() {
		return blocksizeUnit;
	}

	public String getStoragesubtype() {
		return storagesubtype;
	}

	public String getMountPoint() {
		return mountPoint;
	}

	public String getProvider() {
		return provider;
	}

	public Boolean getRemoveAfterJob() {
		return removeAfterJob;
	}

	public String getNextBarcode() {
		return nextBarcode;
	}
	
	// Setter Methods 

	public void setBarcoded(Boolean barcoded) {
		this.barcoded = barcoded;
	}

	public void setBlocksize(Integer blocksize) {
		this.blocksize = blocksize;
	}

	public void setBlocksizeUnit(String blocksizeUnit) {
		this.blocksizeUnit = blocksizeUnit;
	}

	public void setStoragesubtype(String storagesubtype) {
		this.storagesubtype = storagesubtype;
	}

	public void setMountPoint(String mountPoint) {
		this.mountPoint = mountPoint;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public boolean isExpandCapacity() {
		return expandCapacity;
	}

	public void setExpandCapacity(boolean expandCapacity) {
		this.expandCapacity = expandCapacity;
	}

	public void setRemoveAfterJob(Boolean removeAfterJob) {
		this.removeAfterJob = removeAfterJob;
	}

	public void setNextBarcode(String nextBarcode) {
		this.nextBarcode = nextBarcode;
	}
}
