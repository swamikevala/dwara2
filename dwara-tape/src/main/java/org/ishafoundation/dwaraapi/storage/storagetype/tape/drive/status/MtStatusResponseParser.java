package org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/* 
 * 
 * This class is responsible for parsing the response of the below command
 
mt -f /dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst status

When tape writing is just complete

SCSI 2 tape drive:
File1 number=1, block number=0, partition=0.
Tape block size 0 bytes. Density code 0x58 (no translation).
Soft error count since last status=0
General status bits on (81010000):
 EOF ONLINE IM_REP_EN


   						OR

When no tape is inside the drive

SCSI 2 tape drive:
File1 number=-1, block number=-1, partition=0.
Tape block size 0 bytes. Density code 0x0 (default).
Soft error count since last status=0
General status bits on (50000):
 DR_OPEN IM_REP_EN


   						OR

When drive is busy reading or writing
/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst: Device or resource busy

 * 
 */
public class MtStatusResponseParser {
	
	public static MtStatus parseMtStatusResponse(String mtStatusResponse){
		String busyRegEx = ".*Device or resource busy";
		Pattern busyRegExPattern = Pattern.compile(busyRegEx);

		MtStatus mtStatus = new MtStatus();
		Scanner scanner = new Scanner(mtStatusResponse);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			Matcher busyRegExMatcher = busyRegExPattern.matcher(line);
			if(busyRegExMatcher.matches()) {
				mtStatus.setBusy(true);
			}
			else {
				String fileBlockNumRegEx = "File1 number=(-*[0-9]*), block number=(-*[0-9]*), partition=0.";
				String errorCountRegEx = "Soft error count since last status=([0-9]*)";
				String statusRegEx = "^ ([A-Z_ ]*)";

				Pattern fileBlockNumRegExPattern = Pattern.compile(fileBlockNumRegEx);
				Pattern errorCountRegExPattern = Pattern.compile(errorCountRegEx);
				Pattern statusRegExPattern = Pattern.compile(statusRegEx);

				Matcher fileBlockNumRegExMatcher = fileBlockNumRegExPattern.matcher(line);
				Matcher errorCountRegExMatcher = errorCountRegExPattern.matcher(line);
				Matcher statusRegExMatcher = statusRegExPattern.matcher(line);

				if(fileBlockNumRegExMatcher.matches()) {
					mtStatus.setFileNumber(Integer.parseInt(fileBlockNumRegExMatcher.group(1)));
					mtStatus.setBlockNumber(Integer.parseInt(fileBlockNumRegExMatcher.group(2)));
				}
				else if(errorCountRegExMatcher.matches()) {
					mtStatus.setSoftErrorCount(Integer.parseInt(errorCountRegExMatcher.group(1)));
				}
				else if(statusRegExMatcher.matches()) {
					String statusCodeRegEx = "([A-Z_]*)";

					Pattern statusCodeRegExPattern = Pattern.compile(statusCodeRegEx);
					Matcher statusCodeRegExMatcher = statusCodeRegExPattern.matcher(statusRegExMatcher.group(1));
					while(statusCodeRegExMatcher.find()) {
						String extractedMatch = statusCodeRegExMatcher.group();
						if(StringUtils.isBlank(extractedMatch) || extractedMatch.equals("IM_REP_EN"))
							continue;
						TapeDriveStatusCode statusCode = TapeDriveStatusCode.valueOf(extractedMatch);

						switch (statusCode) {
						case ONLINE:
							mtStatus.setReady(true);
							break;			        	
						case DR_OPEN:
							mtStatus.setReady(false);
							break;
						case BOT:
						case EOD:
						case EOF:
						case EOT:								
							mtStatus.setStatusCode(statusCode);
							break;
						case WR_PROT:
							mtStatus.setWriteProtected(true);
							break;
						default:
							break;
						}
					}

				}
			}
		}
		scanner.close();
		return mtStatus;
	}

}
