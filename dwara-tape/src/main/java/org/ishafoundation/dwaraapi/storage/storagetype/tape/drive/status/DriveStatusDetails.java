package org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status;

public class DriveStatusDetails {
	private String driveName;
	private MtStatus mtStatus;
	private int noOfWrites;
	private int noOfReads;
	private int hoursOfWrites;
	private int hoursOfReads;
	private int totalUsageInHours;

	public String getDriveName() {
		return driveName;
	}
	public void setDriveName(String driveName) {
		this.driveName = driveName;
	}
	public MtStatus getMtStatus() {
		return mtStatus;
	}
	public void setMtStatus(MtStatus mtStatus) {
		this.mtStatus = mtStatus;
	}
	public int getNoOfWrites() {
		return noOfWrites;
	}
	public void setNoOfWrites(int noOfWrites) {
		this.noOfWrites = noOfWrites;
	}
	public int getNoOfReads() {
		return noOfReads;
	}
	public void setNoOfReads(int noOfReads) {
		this.noOfReads = noOfReads;
	}
	public int getHoursOfWrites() {
		return hoursOfWrites;
	}
	public void setHoursOfWrites(int hoursOfWrites) {
		this.hoursOfWrites = hoursOfWrites;
	}
	public int getHoursOfReads() {
		return hoursOfReads;
	}
	public void setHoursOfReads(int hoursOfReads) {
		this.hoursOfReads = hoursOfReads;
	}
	public int getTotalUsageInHours() {
		return totalUsageInHours;
	}
	public void setTotalUsageInHours(int totalUsageInHours) {
		this.totalUsageInHours = totalUsageInHours;
	}
}
