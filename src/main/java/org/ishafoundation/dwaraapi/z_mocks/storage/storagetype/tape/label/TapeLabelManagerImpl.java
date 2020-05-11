package org.ishafoundation.dwaraapi.z_mocks.storage.storagetype.tape.label;

import org.ishafoundation.dwaraapi.storage.storagetype.tape.label.TapeLabelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "dev | stage" })
public class TapeLabelManagerImpl implements TapeLabelManager{
	
	Logger logger = LoggerFactory.getLogger(TapeLabelManagerImpl.class);

	@Override
	public boolean isRightTape(String dataTransferElementName, String barcode) throws Exception {
		return true;
	}
	
	public boolean writeVolumeHeaderLabelSet(String tapeBarcode, String storageFormat, String dataTransferElementName) throws Exception{
		return true;
	}

}
