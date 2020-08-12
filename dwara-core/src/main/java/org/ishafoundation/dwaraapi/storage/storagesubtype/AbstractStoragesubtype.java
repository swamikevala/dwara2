package org.ishafoundation.dwaraapi.storage.storagesubtype;

import org.ishafoundation.dwaraapi.exception.DwaraException;

public abstract class AbstractStoragesubtype {
	
	protected Long capacity;
	protected int generation;
	
	public Long getCapacity(){
		return capacity;
	}

	public int getGeneration(){
		return generation;
	}
	
	public abstract void validateVolumeId(String volumeId) throws DwaraException;
}
