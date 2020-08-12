package org.ishafoundation.dwaraapi.storage.storagesubtype;


import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("lto7")
public class Lto7 extends AbstractStoragesubtype{

    private static final Logger logger = LoggerFactory.getLogger(Lto7.class);

    public Lto7() {
    	capacity = 6000000000000L;
    	generation = 7;
	}

	@Override
	public void validateVolumeId(String volumeId) throws DwaraException{
		String suffixToEndWith = "L7";
		if(volumeId.endsWith(suffixToEndWith)) {
			return;
		}
		throw new DwaraException("Volume should end with " + suffixToEndWith, null);
	}
}
