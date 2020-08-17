package org.ishafoundation.dwaraapi.storage.storagesubtype;

import org.ishafoundation.dwaraapi.exception.DwaraException;

public abstract class AbstractStoragesubtype {
	
	protected Long capacity;
	protected int generation;
	protected int[] writeSupportedGenerations;
	protected int[] readSupportedGenerations;
	
	public Long getCapacity(){
		return capacity;
	}

	public int getGeneration(){
		return generation;
	}
	
	public int[] getWriteSupportedGenerations() {
		return writeSupportedGenerations;
	}

	public int[] getReadSupportedGenerations() {
		return readSupportedGenerations;
	}
	
	public abstract void validateVolumeId(String volumeId) throws DwaraException;
}
