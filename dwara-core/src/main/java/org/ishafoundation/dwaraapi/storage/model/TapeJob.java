package org.ishafoundation.dwaraapi.storage.model;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.TActivedevice;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;

public class TapeJob extends SelectedStorageJob {
	
	private boolean optimizeTapeAccess = true; // by default we need tape optimisation // TODO is an attribute of storagetype. Should fit this in storagetype
	private TActivedevice tActivedevice;
	private String tapeLibraryName;
	private int tapedriveNo; // elementAddress
	private List<DriveDetails> driveDetails; // expensive to get the list again, so just passing the already prepared set..

	public boolean isOptimizeTapeAccess() {
		return optimizeTapeAccess;
	}
	public void setOptimizeTapeAccess(boolean optimizeTapeAccess) {
		this.optimizeTapeAccess = optimizeTapeAccess;
	}
	public TActivedevice gettActivedevice() {
		return tActivedevice;
	}
	public void settActivedevice(TActivedevice tActivedevice) {
		this.tActivedevice = tActivedevice;
	}
	public String getTapeLibraryName() {
		return tapeLibraryName;
	}
	public void setTapeLibraryName(String tapeLibraryName) {
		this.tapeLibraryName = tapeLibraryName;
	}
	public int getTapedriveNo() {
		return tapedriveNo;
	}
	public void setTapedriveNo(int tapedriveNo) {
		this.tapedriveNo = tapedriveNo;
	}
	public List<DriveDetails> getDriveDetails() {
		return driveDetails;
	}
	public void setDriveDetails(List<DriveDetails> driveDetails) {
		this.driveDetails = driveDetails;
	}
}
