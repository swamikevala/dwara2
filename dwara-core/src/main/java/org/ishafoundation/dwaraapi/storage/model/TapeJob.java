package org.ishafoundation.dwaraapi.storage.model;

public class TapeJob extends StoragetypeJob {
	
	private boolean optimizeTapeAccess = true; // by default we need tape optimisation // TODO is an attribute of storagetype. Should fit this in storagetype
	private String tapeLibraryName;
	private int tapedriveNo; // elementAddress
	private String tapedriveUid;//
	private boolean tapedriveAlreadyLoadedWithTape;

	public boolean isOptimizeTapeAccess() {
		return optimizeTapeAccess;
	}
	public void setOptimizeTapeAccess(boolean optimizeTapeAccess) {
		this.optimizeTapeAccess = optimizeTapeAccess;
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
	public String getTapedriveUid() {
		return tapedriveUid;
	}
	public void setTapedriveUid(String tapedriveUid) {
		this.tapedriveUid = tapedriveUid;
	}
	public boolean isTapedriveAlreadyLoadedWithTape() {
		return tapedriveAlreadyLoadedWithTape;
	}
	public void setTapedriveAlreadyLoadedWithTape(boolean tapedriveAlreadyLoadedWithTape) {
		this.tapedriveAlreadyLoadedWithTape = tapedriveAlreadyLoadedWithTape;
	}


}
