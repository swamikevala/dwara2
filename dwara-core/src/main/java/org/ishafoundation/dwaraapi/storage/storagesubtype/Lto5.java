package org.ishafoundation.dwaraapi.storage.storagesubtype;


import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("LTO-5")
public class Lto5 extends AbstractStoragesubtype{

    private static final Logger logger = LoggerFactory.getLogger(Lto5.class);

    public Lto5() {
    	capacity = 1500000000000L;
    	generation = 5;
    	int[] writeSupportedGenerations = {4,5};
    	this.writeSupportedGenerations = writeSupportedGenerations;
    	int[] readSupportedGenerations = {3,4,5};
    	this.readSupportedGenerations = readSupportedGenerations;
	}

	@Override
	public void validateVolumeId(String volumeId) throws DwaraException{
		String suffixToEndWith = "L5";
		if(volumeId.endsWith(suffixToEndWith)) {
			return;
		}
		throw new DwaraException("Volume should end with " + suffixToEndWith, null);
	}
}
