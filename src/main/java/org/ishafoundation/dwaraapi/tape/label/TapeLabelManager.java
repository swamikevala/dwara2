package org.ishafoundation.dwaraapi.tape.label;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TapeLabelManager{
	
	Logger logger = LoggerFactory.getLogger(TapeLabelManager.class);

	@Autowired
	private CommandLineExecuter commandLineExecuter;
	
	@Value("${tape.volumelabel1.implid}")
	private String implId;
	
	@Value("${tape.volumelabel1.ownerid}")
	private String ownerId;
	
//	@Autowired
//	private TapeLibraryManager tapeLibraryManager;
//	
//	@Autowired
//	private TapeDriveManager tapeDriveManager;
//	
//	public
//		//get a drive
//		List<DriveStatusDetails> availableDrivesList = tapeLibraryManager.getAvailableDrivesList();
//		DriveStatusDetails dsd = availableDrivesList.get(0);
//		
//		// load the tape
//		try {
//			tapeLibraryManager.locateAndLoadTapeOnToDrive(tapeBarcode, dsd.getTapelibraryName(), dsd.getDriveSNo());
//			tapeLabelManager.writeVolumeHeaderLabelSet(tapeBarcode, dsd.getDriveName());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	
//	}
	public boolean writeVolumeHeaderLabelSet(String tapeBarcode, String storageFormat, String dataTransferElementName) throws Exception{
		boolean isSuccess = false;

		// Writing a label to the tape has multiple options..
		//		Option 1 - issue multiple commands
		//		
		//		dd << EOF of=/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst bs=80
		//		getLabel(tapeBarcode) // TODO: should this storageformat specific? anyway provide a way...
		//		EOF
		
				
		//		Option 2 - using echo		
		//		echo "VOL1V5A001L             LTFS                                                  4" | dd of=/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst bs=80
						
				
		//		Option 3 - using a temp file
		//		dd if=/data/tmp/V5A001-Label.txt of=/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst bs=80		

		String label1 = getLabel1(tapeBarcode);
		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("echo \"" + label1 + "\" | dd of=" + dataTransferElementName + " bs=80");
		if(cler.isComplete()) {
			String label2 = getLabel2(storageFormat);
			cler = commandLineExecuter.executeCommand("echo \"" + label2 + "\" | dd of=" + dataTransferElementName + " bs=80");
			if(cler.isComplete()) 
				isSuccess = true;
		}
		return isSuccess;
		
	}


	private String getLabel1(String tapeBarcode) throws Exception {
		String label = null;
		String volID = StringUtils.substring(tapeBarcode, 0, 6);
		String ltoGen = StringUtils.substring(tapeBarcode, 6, 8);
		
		
		VolumeHeaderLabel1 vol1 = new VolumeHeaderLabel1(volID, ltoGen, implId, ownerId); // TODO - Get the label owner from app.properties...
		label = vol1.getLabel();
		return label;
	}

	private String getLabel2(String storageFormat) throws Exception {
		String label = null;
		VolumeHeaderLabel2 vol2 = new VolumeHeaderLabel2(); // TODO - Get the label owner from app.properties...
		label = vol2.getLabel();
		return label;
	}

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
