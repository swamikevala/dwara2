package org.ishafoundation.dwaraapi.storage.storagetype.tape.label;

public interface TapeLabelManager {

	public boolean isRightTape(String dataTransferElementName, String barcode) throws Exception;
	
	public boolean writeVolumeHeaderLabelSet(String tapeBarcode, String storageFormat, String dataTransferElementName) throws Exception;
}
