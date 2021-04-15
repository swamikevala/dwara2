package org.ishafoundation.dwaraapi.api.resp.autoloader;

import java.util.Objects;

import org.ishafoundation.dwaraapi.utils.TapeUsageStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Tape
{
    private Element element;

    private int address;

    private TapeStatus status;

    private String barcode;
    
	private String volumeGroup;
	
	private String storagesubtype;

    private Boolean removeAfterJob;

    private TapeUsageStatus usageStatus;

    private String location;
    
    private String action;

    
	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
	}

	public int getAddress() {
		return address;
	}

	public void setAddress(int address) {
		this.address = address;
	}

	public TapeStatus getStatus() {
		return status;
	}

	public void setStatus(TapeStatus status) {
		this.status = status;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getVolumeGroup() {
		return volumeGroup;
	}

	public void setVolumeGroup(String volumeGroup) {
		this.volumeGroup = volumeGroup;
	}

	public String getStoragesubtype() {
		return storagesubtype;
	}

	public void setStoragesubtype(String storagesubtype) {
		this.storagesubtype = storagesubtype;
	}

	public Boolean isRemoveAfterJob() {
		return removeAfterJob;
	}

	public void setRemoveAfterJob(Boolean removeAfterJob) {
		this.removeAfterJob = removeAfterJob;
	}

	public TapeUsageStatus getUsageStatus() {
		return usageStatus;
	}

	public void setUsageStatus(TapeUsageStatus usageStatus) {
		this.usageStatus = usageStatus;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tape tape = (Tape) o;
        return Objects.equals(barcode, tape.barcode);
    }
}
