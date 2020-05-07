package org.ishafoundation.dwaraapi.tape.drive;

import org.ishafoundation.dwaraapi.tape.drive.status.DriveStatusDetails;

public interface TapeDriveManager{
	
//	public DriveStatusDetails getDriveDetails(int tapelibraryId, int dataTransferElementNo);
//	
//	public DriveStatusDetails setTapeHeadPositionForWriting(int tapelibraryId, int dataTransferElementNo);
//	
//	public DriveStatusDetails setTapeHeadPositionForReading(int tapelibraryId, int dataTransferElementNo, int blockNumberToSeek); 
	
	public DriveStatusDetails getDriveDetails(String tapelibraryName, int dataTransferElementNo);
	
	public DriveStatusDetails setTapeHeadPositionForWriting(String tapelibraryName, int dataTransferElementNo);
	
	public DriveStatusDetails setTapeHeadPositionForReading(String tapelibraryName, int dataTransferElementNo, int blockNumberToSeek);
	
	public boolean isTapeBlank(String tapelibraryName, int dataTransferElementNo);
	
	public DriveStatusDetails setTapeHeadPositionForFormatting(String tapelibraryName, int dataTransferElementNo);
}
