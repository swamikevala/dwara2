package org.ishafoundation.dwaraapi.storage.storagesubtype;


import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("lto6")
public class Lto6 extends AbstractStoragesubtype{

    private static final Logger logger = LoggerFactory.getLogger(Lto6.class);

    public Lto6() {
    	capacity = 2500000000000L;
    	generation = 6;
	}

	@Override
	public void validateVolumeId(String volumeId) throws DwaraException{
		String suffixToEndWith = "L6";
		if(volumeId.endsWith(suffixToEndWith)) {
			return;
		}
		throw new DwaraException("Volume should end with " + suffixToEndWith, null);
	}
}
