package org.ishafoundation.dwaraapi.entrypoint.resource.ingest.list;

public class LibraryclassForList {
	
	private int libraryclassId;
	
	private String name;
	
	private String[] targetVolumes; 
	
	private Integer displayOrder;
	
	private String[] permittedActions;
	

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

	public String[] getTargetVolumes() {
		return targetVolumes;
	}

	public void setTargetVolumes(String[] targetVolumes) {
		this.targetVolumes = targetVolumes;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	public String[] getPermittedActions() {
		return permittedActions;
	}

	public void setPermittedActions(String[] permittedActions) {
		this.permittedActions = permittedActions;
	}
	
}
