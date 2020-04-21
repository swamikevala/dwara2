package org.ishafoundation.dwaraapi.tape.label;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

@Component
public class TapeLabelManager{
//	
//	// TODO Move this out
//	public ResponseEntity<String> writeLabel(String tapeBarcode, int tapelibraryId, int dataTransferElementNo){
//
//		
//		//		Option 1 - issue multiple commands
//		//		
//		//		dd << EOF of=/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst bs=80
//		//		getLabel(tapeBarcode) // TODO: should this storageformat specific? anyway provide a way...
//		//		EOF
//		
//				
//		//		Option 2 - using echo		
//		//		echo "VOL1V5A001L             LTFS                                                  4" | dd of=/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst bs=80
//						
//				
//		//		Option 3 - using a temp file
//		//		dd if=/data/tmp/V5A001-Label.txt of=/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst bs=80		
//
//		try {
//			String dataTransferElementName = getDriveName(tapelibraryId, dataTransferElementNo);
//			// rewind the tape
//			rewind(dataTransferElementName);
//
//			String storageFormat = "BRU"; // TODO get it from tapeset
//			String label1 = getLabel1(tapeBarcode, storageFormat);
//			commandLineExecuter.executeCommand("echo \"" + label1 + "\" | dd of=" + dataTransferElementName + " bs=80");
//			
//			String label2 = getLabel2(tapeBarcode, storageFormat);
//			commandLineExecuter.executeCommand("echo \"" + label2 + "\" | dd of=" + dataTransferElementName + " bs=80");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return ResponseEntity.status(HttpStatus.OK).body("");
//	}
//
//
//	private String getLabel1(String tapeBarcode, String storageFormat) {
//		// TODO Something like below
//		return "VOL1" + tapeBarcode + "L             " + storageFormat + "                                                   4";
//	}
//
//	private String getLabel2(String tapeBarcode, String storageFormat) {
//		// TODO Something like below
//		return "VOL2" + tapeBarcode + "L             " + storageFormat + "                                                   4";
//	}
//
//	public ResponseEntity<String> readLabel(@PathVariable("tapeBarcode") String tapeBarcode){
//		
////		rewind the tape
////		
////		issue the following multiple commands
////		 dd if=/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst bs=80
//// response will be like below
////Shambho EOF
////0+1 records in
////0+1 records out
////12 bytes (12 B) copied, 0.00298022 s, 4.0 kB/s
//
//		
//		return ResponseEntity.status(HttpStatus.OK).body("");
//	}
//
}
