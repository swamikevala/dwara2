package org.ishafoundation.dwaraapi.storage.model;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.TActivedevice;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;

public class TapeJob extends SelectedStorageJob {
	
	private boolean optimizeTapeAccess = true; // by default we need tape optimisation // TODO is an attribute of storagetype. Should fit this in storagetype
	private TActivedevice tActivedevice;
	private String tapeLibraryName;
	private int tapedriveNo; // elementAddress
//	private boolean tapedriveAlreadyLoadedWithNeededTape;
	private List<DriveDetails> preparedDrives; // expensive to get the list again, so just passing the already prepared set..
	private int artifactVolumeCount;

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
	public List<DriveDetails> getPreparedDrives() {
		return preparedDrives;
	}
	public void setPreparedDrives(List<DriveDetails> preparedDrives) {
		this.preparedDrives = preparedDrives;
	}
	//	public boolean isTapedriveAlreadyLoadedWithNeededTape() {
//		return tapedriveAlreadyLoadedWithNeededTape;
//	}
//	public void setTapedriveAlreadyLoadedWithNeededTape(boolean tapedriveAlreadyLoadedWithNeededTape) {
//		this.tapedriveAlreadyLoadedWithNeededTape = tapedriveAlreadyLoadedWithNeededTape;
//	}
	public int getArtifactVolumeCount() {
		return artifactVolumeCount;
	}
	public void setArtifactVolumeCount(int artifactVolumeCount) {
		this.artifactVolumeCount = artifactVolumeCount;
	}
}
