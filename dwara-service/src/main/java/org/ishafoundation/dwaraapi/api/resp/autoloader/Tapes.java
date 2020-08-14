package org.ishafoundation.dwaraapi.api.resp.autoloader;

public class Tapes
{
    private Element element;

    private int address;

    private TapeStatus status;

    private String barcode;

    private Boolean removeAfterJob;

    private TapeUsageStatus usageStatus;

    private String location;

    
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
}
