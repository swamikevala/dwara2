package org.ishafoundation.dwaraapi.api.resp.volume;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class VolumeResponse {
	private String id;
	private String volumetype;
	private String storagetype;
	private String storagelevel;
	private String volumeRef;
	private String checksumtype;
	private String initializedAt;
	private boolean finalized;
	private boolean imported;
	private String archiveformat;
	private float totalCapacity;
	private float usedCapacity;
	private float unusedCapacity;
	private float maxPhysicalUnusedCapacity;
	private String sizeUnit;
	private String location;
	private Details details;
	private int copyNumber;
	
	public int getCopyNumber() {
		return copyNumber;
	}
	public void setCopyNumber(int copyNumber) {
		this.copyNumber = copyNumber;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getVolumetype() {
		return volumetype;
	}
	public void setVolumetype(String volumetype) {
		this.volumetype = volumetype;
	}
	public String getStoragetype() {
		return storagetype;
	}
	public void setStoragetype(String storagetype) {
		this.storagetype = storagetype;
	}
	public String getStoragelevel() {
		return storagelevel;
	}
	public void setStoragelevel(String storagelevel) {
		this.storagelevel = storagelevel;
	}
	public String getVolumeRef() {
		return volumeRef;
	}
	public void setVolumeRef(String volumeRef) {
		this.volumeRef = volumeRef;
	}
	public String getChecksumtype() {
		return checksumtype;
	}
	public void setChecksumtype(String checksumtype) {
		this.checksumtype = checksumtype;
	}
	public String getInitializedAt() {
		return initializedAt;
	}
	public void setInitializedAt(String initializedAt) {
		this.initializedAt = initializedAt;
	}
	public boolean isFinalized() {
		return finalized;
	}
	public void setFinalized(boolean finalized) {
		this.finalized = finalized;
	}
	public boolean isImported() {
		return imported;
	}
	public void setImported(boolean imported) {
		this.imported = imported;
	}
	public String getArchiveformat() {
		return archiveformat;
	}
	public void setArchiveformat(String archiveformat) {
		this.archiveformat = archiveformat;
	}
	public float getTotalCapacity() {
		return totalCapacity;
	}
	public void setTotalCapacity(float totalCapacity) {
		this.totalCapacity = totalCapacity;
	}
	public float getUsedCapacity() {
		return usedCapacity;
	}
	public void setUsedCapacity(float usedCapacity) {
		this.usedCapacity = usedCapacity;
	}
	public float getUnusedCapacity() {
		return unusedCapacity;
	}
	public void setUnusedCapacity(float unusedCapacity) {
		this.unusedCapacity = unusedCapacity;
	}
	public float getMaxPhysicalUnusedCapacity() {
		return maxPhysicalUnusedCapacity;
	}
	public void setMaxPhysicalUnusedCapacity(float maxPhysicalUnusedCapacity) {
		this.maxPhysicalUnusedCapacity = maxPhysicalUnusedCapacity;
	}
	public String getSizeUnit() {
		return sizeUnit;
	}
	public void setSizeUnit(String sizeUnit) {
		this.sizeUnit = sizeUnit;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public Details getDetails() {
		return details;
	}
	public void setDetails(Details details) {
		this.details = details;
	}
}
