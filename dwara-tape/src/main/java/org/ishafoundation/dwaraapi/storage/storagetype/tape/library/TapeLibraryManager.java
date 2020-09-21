package org.ishafoundation.dwaraapi.storage.storagetype.tape.library;

import java.util.List;

import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.StorageElement;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.status.MtxStatus;

/**
 * 
 * The impl can choose to handle exception in scenarios when the magazine is open and the library throws error
 * For eg., when a command is issued when the library's magazine is open, commands are responded back with 
 * READ ELEMENT STATUS Command Failed
 * The impl may choose to cascade the failure upstream or wait and retry for n attempts before bubbling up the failure...
 *
 */
public interface TapeLibraryManager {
	
	public MtxStatus getMtxStatus(String tapeLibraryName) throws Exception; // TODO Make it generic like LibraryStatus and definitely not labeled with mtx - as impl may choose how it gets the library status..
	
	public List<DataTransferElement> getAllDataTransferElements(String tapeLibraryName) throws Exception;
	
	public List<StorageElement> getAllStorageElements(String tapeLibraryName) throws Exception;
	
	public List<TapeOnLibrary> getAllLoadedTapesInTheLibrary(String tapeLibraryName) throws Exception;
	
	public boolean load(String tapeLibraryName, int storageElementSNo, int dataTransferElementSNo) throws Exception;
	
	public boolean unload(String tapeLibraryName, int dataTransferElementSNo) throws Exception; // The implementation should ensure find an empty slot and pass it for the DTE medium to be unloaded to...

	public boolean unload(String tapeLibraryName, int storageElementSNo, int dataTransferElementSNo) throws Exception;
	
	// Is this needed here? - Sort of a link method connecting drivemanager needing the drivename as well
	public boolean locateAndLoadTapeOnToDrive(String toBeUsedTapeBarcode, String tapeLibraryName, int dataTransferElementSNo, String dataTransferElementName) throws Exception;

	public int locateTape(String toBeUsedTapeBarcode, String tapeLibraryName) throws Exception;
	
	public int locateTape(String toBeUsedTapeBarcode, MtxStatus mtxStatus) throws Exception;
}
