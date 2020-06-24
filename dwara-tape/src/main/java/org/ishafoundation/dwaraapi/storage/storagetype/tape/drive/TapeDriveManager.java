package org.ishafoundation.dwaraapi.storage.storagetype.tape.drive;

import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveStatusDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.MtStatus;

public interface TapeDriveManager{
	
	public MtStatus getMtStatus(String dataTransferElementName) throws Exception;
	
	public DriveStatusDetails getDriveDetails(String dataTransferElementName) throws Exception;
	
	public boolean isTapeBlank(String dataTransferElementName) throws Exception;
	
	public DriveStatusDetails setTapeHeadPositionForWriting(String dataTransferElementName) throws Exception;
	
	public DriveStatusDetails setTapeHeadPositionForReading(String dataTransferElementName, int blockNumberToSeek) throws Exception;
	
	public DriveStatusDetails setTapeHeadPositionForFormatting(String dataTransferElementName) throws Exception;
	
	public DriveStatusDetails setTapeHeadPositionForFinalizing(String dataTransferElementName) throws Exception;
}
