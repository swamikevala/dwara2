package org.ishafoundation.dwaraapi.storage.storagetype.tape.drive;

import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.MtStatus;

public interface TapeDriveManager{
	
	public MtStatus getMtStatus(String dataTransferElementName) throws Exception;
	
	public DriveDetails getDriveDetails(String dataTransferElementName) throws Exception;
	
	public boolean isTapeBlank(String dataTransferElementName) throws Exception;
	
	public DriveDetails setTapeHeadPositionForInitializing(String dataTransferElementName) throws Exception;
	
	public DriveDetails setTapeHeadPositionForReadingLabel(String dataTransferElementName) throws Exception;
	
	public DriveDetails setTapeHeadPositionForReadingInterArtifactXml(String dataTransferElementName) throws Exception;
	
	public DriveDetails setTapeHeadPositionForWriting(String dataTransferElementName, int blockNumberToBePositioned) throws Exception;
	
	public DriveDetails setTapeHeadPositionForReading(String dataTransferElementName, int blockNumberToSeek) throws Exception;

	public DriveDetails setTapeHeadPositionForFinalizing(String dataTransferElementName, int blockNumberToBePositioned) throws Exception;
}
