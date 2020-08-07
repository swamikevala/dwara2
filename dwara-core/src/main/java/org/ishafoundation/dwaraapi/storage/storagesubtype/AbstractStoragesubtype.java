package org.ishafoundation.dwaraapi.storage.storagesubtype;

public abstract class AbstractStoragesubtype {
	
	protected Long capacity;
	protected int generation;
	
	public Long getCapacity(){
		return capacity;
	}

	public int getGeneration(){
		return generation;
	}
}
