package org.ishafoundation.dwaraapi.storage.storagetype.tape.drive;

import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.MtStatus;

public interface TapeDriveManager{
	
	public MtStatus getMtStatus(String dataTransferElementName) throws Exception;
	
	public DriveDetails getDriveDetails(String dataTransferElementName) throws Exception;
	
	public boolean isTapeBlank(String dataTransferElementName) throws Exception;
	
	public DriveDetails setTapeHeadPositionForWriting(String dataTransferElementName, int fileNumberToBePositioned) throws Exception;
	
	public DriveDetails setTapeHeadPositionForReading(String dataTransferElementName, int blockNumberToSeek) throws Exception;
	
	public DriveDetails setTapeHeadPositionForFormatting(String dataTransferElementName) throws Exception;
	
	public DriveDetails setTapeHeadPositionForReadingLabel(String dataTransferElementName) throws Exception;

	public DriveDetails setTapeHeadPositionForFinalizing(String dataTransferElementName) throws Exception;
}
