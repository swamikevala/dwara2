package org.ishafoundation.dwaraapi.api.resp;

public class Libraryclass {
	private int libraryclassId;
	
	private String name;
	
	private Integer[] targetVolumeIds; 
	
	private Integer displayOrder;
	

	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer[] getTargetVolumeIds() {
		return targetVolumeIds;
	}

	public void setTargetVolumeIds(Integer[] targetVolumeIds) {
		this.targetVolumeIds = targetVolumeIds;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}
}
