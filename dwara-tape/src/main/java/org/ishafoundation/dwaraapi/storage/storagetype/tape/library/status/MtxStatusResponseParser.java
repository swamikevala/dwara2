package org.ishafoundation.dwaraapi.storage.storagetype.tape.library.status;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.StorageElement;

/*
 * Class responsible for parsing the below commands console output
 * 
mtx -f /dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400 status


Storage Changer /dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400:3 Drives, 24 Slots ( 4 Import/Export )
  Data Transfer Element 0:Full (Storage Element 12 Loaded):VolumeTag = BRU003
  Data Transfer Element 1:Empty
  Data Transfer Element 2:Empty
        Storage Element 1:Full :VolumeTag=V4A003
        Storage Element 2:Full :VolumeTag=V4B003
        Storage Element 3:Full :VolumeTag=V5A003
        Storage Element 4:Full :VolumeTag=V5B003
        Storage Element 5:Full :VolumeTag=V5C003
        Storage Element 6:Full :VolumeTag=VLA003
        Storage Element 7:Full :VolumeTag=UA001
        Storage Element 8:Full :VolumeTag=UB001
        Storage Element 9:Full :VolumeTag=UC001
        Storage Element 10:Full :VolumeTag=BRU001
        Storage Element 11:Full :VolumeTag=BRU002
        Storage Element 12:Empty
        Storage Element 13:Empty
        Storage Element 14:Empty
        Storage Element 15:Empty
        Storage Element 16:Empty
        Storage Element 17:Empty
        Storage Element 18:Empty
        Storage Element 19:Empty
        Storage Element 20:Empty
        Storage Element 21 IMPORT/EXPORT:Empty
        Storage Element 22 IMPORT/EXPORT:Empty
        Storage Element 23 IMPORT/EXPORT:Empty
        Storage Element 24 IMPORT/EXPORT:Empty
*/
public class MtxStatusResponseParser {
	
	public static MtxStatus parseMtxStatusResponse(String mtxStatusResponse){
		// Data Transfer Element 0:Full (Storage Element 12 Loaded):VolumeTag = BRU003                                                                                                                  
		// Data Transfer Element 1:Empty
		String dteRegEx = "Data Transfer Element ([0-9]*):(.*)";
		String dteEmptyRegEx = "Empty";
		String dteVolTagRegEx = "Full \\(Storage Element ([0-9]*) Loaded\\):VolumeTag = (.*)";
		Pattern dteRegExPattern = Pattern.compile(dteRegEx);
		Pattern dteVolTagRegExPattern = Pattern.compile(dteVolTagRegEx);

		// Storage Element 1:Full :VolumeTag=V4A003
		// Storage Element 12:Empty
		// Storage Element 21 IMPORT/EXPORT:Empty

		String seRegEx = "Storage Element (.[0-9]*)([^:]*):(.*)";
		String seVolTagRegEx = "Full :VolumeTag=(.*)";

		Pattern seRegExPattern = Pattern.compile(seRegEx);
		Pattern seVolTagRegExPattern = Pattern.compile(seVolTagRegEx);

		MtxStatus mtxStatus = new MtxStatus();
		Scanner scanner = new Scanner(mtxStatusResponse);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();
			Matcher dteRegExMatcher = dteRegExPattern.matcher(line);
			if(dteRegExMatcher.matches()) {
				DataTransferElement dte = new DataTransferElement();
				dte.setsNo(Integer.parseInt(dteRegExMatcher.group(1)));
				String dteState = dteRegExMatcher.group(2);
				if(dteState.equals(dteEmptyRegEx)) {
					dte.setEmpty(true);
				}
				else {
					Matcher dteVolTagRegExMatcher = dteVolTagRegExPattern.matcher(dteState);
					if(dteVolTagRegExMatcher.matches()) {
						dte.setStorageElementNo(Integer.parseInt(dteVolTagRegExMatcher.group(1)));
						dte.setVolumeTag(dteVolTagRegExMatcher.group(2));
					}
				}
				mtxStatus.getDteList().add(dte);
			}
			else {
				Matcher seRegExMatcher = seRegExPattern.matcher(line);
				if(seRegExMatcher.matches()) {

					StorageElement se = new StorageElement();
					String seSNo = seRegExMatcher.group(1);
					String impOrExp = seRegExMatcher.group(2);
					String status = seRegExMatcher.group(3);
					//System.out.println(seSNo + " : " + impOrExp + " : " + status);
					se.setsNo(Integer.parseInt(seSNo));
					Matcher seVolTagRegExMatcher = seVolTagRegExPattern.matcher(status);
					if(seVolTagRegExMatcher.matches()) {
						se.setVolumeTag(seVolTagRegExMatcher.group(1));
					}
					mtxStatus.getSeList().add(se);
				}				
			}
		}
		scanner.close();
		mtxStatus.setNoOfDrives(mtxStatus.getDteList().size());
		mtxStatus.setNoOfSlots(mtxStatus.getSeList().size());
		return mtxStatus;
	}

	public static void main(String[] args) throws IOException {
		String mtxStatusResponse = FileUtils.readFileToString(new File("C:\\Users\\prakash\\projects\\videoarchives\\bru\\POC\\BRU002\\test\\mtxResponses\\mtx_status_tape_already_on_drive.txt")); // will make a call to mtx and get the status realtime...
		MtxStatus mtxStatus = MtxStatusResponseParser.parseMtxStatusResponse(mtxStatusResponse);
		System.out.println(mtxStatus.toString());
	}
}
