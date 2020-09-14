package org.ishafoundation.dwaraapi.storage.storagesubtype;

import org.ishafoundation.dwaraapi.exception.DwaraException;

public abstract class AbstractStoragesubtype {
	
	protected Long capacity;
	protected int generation;
	protected String suffixToEndWith;
	protected int[] writeSupportedGenerations;
	protected int[] readSupportedGenerations;
	
	public Long getCapacity(){
		return capacity;
	}

	public int getGeneration(){
		return generation;
	}
	
	public String getSuffixToEndWith() {
		return suffixToEndWith;
	}

	public int[] getWriteSupportedGenerations() {
		return writeSupportedGenerations;
	}

	public int[] getReadSupportedGenerations() {
		return readSupportedGenerations;
	}
	
	public void validateVolumeId(String volumeId) throws DwaraException{
		if(volumeId.endsWith(suffixToEndWith)) {
			return;
		}
		throw new DwaraException("Volume should end with " + suffixToEndWith, null);
	}
}
