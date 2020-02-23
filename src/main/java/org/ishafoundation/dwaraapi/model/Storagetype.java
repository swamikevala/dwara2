package org.ishafoundation.dwaraapi.model;

public class Storagetype {

	private int storagetypeId;
	private String name;
	private boolean sequential;

		
	public int getStoragetypeId() {
		return storagetypeId;
	}

	public void setStoragetypeId(int storagetypeId) {
		this.storagetypeId = storagetypeId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isSequential() {
		return sequential;
	}

	public void setSequential(boolean sequential) {
		this.sequential = sequential;
	}
}