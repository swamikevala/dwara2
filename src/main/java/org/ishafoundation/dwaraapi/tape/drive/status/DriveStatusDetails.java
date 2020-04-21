package org.ishafoundation.dwaraapi.tape.drive.status;

import org.ishafoundation.dwaraapi.tape.library.components.DataTransferElement;

public class DriveStatusDetails {
	private int tapelibraryId;
	private String tapeLibraryName;
	private DataTransferElement dte;
	private int driveSNo;
	private MtStatus mtStatus;
	private int noOfWrites;
	private int noOfReads;
	private int hoursOfWrites;
	private int hoursOfReads;
	private int totalUsageInHours;
	

	
//	public int getTapelibraryId() {
//		return tapelibraryId;
//	}
//	public void setTapelibraryId(int tapelibraryId) {
//		this.tapelibraryId = tapelibraryId;
//	}
	public String getTapeLibraryName() {
		return tapeLibraryName;
	}
	public void setTapeLibraryName(String tapeLibraryName) {
		this.tapeLibraryName = tapeLibraryName;
	}	
	public DataTransferElement getDte() {
		return dte;
	}
	public void setDte(DataTransferElement dte) {
		this.dte = dte;
	}
	public int getDriveSNo() {
		return driveSNo;
	}
	public void setDriveSNo(int driveSNo) {
		this.driveSNo = driveSNo;
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
