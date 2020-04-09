package org.ishafoundation.dwaraapi.api.resp.restore;

public class File {

	private int id;
	
	private String name;
	
	private double size;
	
	private String libraryclass;
	
	private String barcode;
	
	private boolean online;
	
	private boolean listPermitted;
	
	private boolean restorePermitted;
	
	private boolean targetVolumePermitted;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public String getLibraryclass() {
		return libraryclass;
	}

	public void setLibraryclass(String libraryclass) {
		this.libraryclass = libraryclass;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public boolean isListPermitted() {
		return listPermitted;
	}

	public void setListPermitted(boolean listPermitted) {
		this.listPermitted = listPermitted;
	}

	public boolean isRestorePermitted() {
		return restorePermitted;
	}

	public void setRestorePermitted(boolean restorePermitted) {
		this.restorePermitted = restorePermitted;
	}

	public boolean isTargetVolumePermitted() {
		return targetVolumePermitted;
	}

	public void setTargetVolumePermitted(boolean targetVolumePermitted) {
		this.targetVolumePermitted = targetVolumePermitted;
	}

}
