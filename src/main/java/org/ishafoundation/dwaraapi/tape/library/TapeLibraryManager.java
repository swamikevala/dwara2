package org.ishafoundation.dwaraapi.tape.library;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.Tapelibrary;
import org.ishafoundation.dwaraapi.tape.drive.status.DriveStatusDetails;
import org.ishafoundation.dwaraapi.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.tape.library.components.StorageElement;

public interface TapeLibraryManager {
	
	public Iterable<Tapelibrary> getAllTapeLibraries();
	
	public List<DataTransferElement> getAllDrivesList(String tapeLibraryName);
	
	public List<DriveStatusDetails> getAvailableDrivesList();
	
	public List<DriveStatusDetails> getAvailableDrivesList(String tapeLibraryName);
	
	public List<StorageElement> getAllStorageElementsList(String tapeLibraryName);

	public List<String> getAllLoadedTapesInTheLibraries();
	
	public List<String> getAllLoadedTapesInTheLibrary(String tapeLibraryName);
	
	public boolean locateAndLoadTapeOnToDrive(String toBeUsedTapeBarcode, String tapeLibraryName, int toBeUsedDataTransferElementSNo) throws Exception;
	
	public boolean load(String tapeLibraryName, int seSNo, int driveSNo);
	
	public boolean unload(String tapeLibraryName, int seSNo, int driveSNo);
}
