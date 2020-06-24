package org.ishafoundation.dwaraapi.storage.storagetype.tape.library;

import java.util.List;

import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.StorageElement;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.status.MtxStatus;

public interface TapeLibraryManager {
	
	public MtxStatus getMtxStatus(String tapeLibraryName) throws Exception;
	
	public List<DataTransferElement> getAllDrivesList(String tapeLibraryName) throws Exception;
	
	public List<StorageElement> getAllStorageElementsList(String tapeLibraryName) throws Exception;
	
	public List<String> getAllLoadedTapesInTheLibrary(String tapeLibraryName) throws Exception;
	
	public boolean load(String tapeLibraryName, int storageElementSNo, int dataTransferElementSNo) throws Exception;
	
	public boolean unload(String tapeLibraryName, int storageElementSNo, int dataTransferElementSNo) throws Exception;

	// Is this needed here? - Sort of a link method connecting drivemanager needing the drivename as well
	public boolean locateAndLoadTapeOnToDrive(String toBeUsedTapeBarcode, String tapeLibraryName, int dataTransferElementSNo, String dataTransferElementName) throws Exception;
}
