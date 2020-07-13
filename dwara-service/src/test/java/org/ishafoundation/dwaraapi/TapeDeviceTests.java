package org.ishafoundation.dwaraapi;

import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.TapeDriveManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.MtStatus;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeLibraryManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.status.MtxStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TapeDeviceTests{
	
	@Autowired
	private TapeLibraryManager tapeLibraryManager;

	@Autowired
	private TapeDriveManager tapeDriveManager;
	
	@Test
	public void testLibraryMtxStatus() {
		String tapeLibraryName = "/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400";
		int dataTransferElementSNo = 0;
		int storageElementSNo = 4;
		String dataTransferElementName = "/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst";
		try {
			tapeLibraryManager.load(tapeLibraryName, storageElementSNo, dataTransferElementSNo);
			System.out.println("load success");

			MtxStatus mtxStatus = tapeLibraryManager.getMtxStatus(tapeLibraryName);
			System.out.println(mtxStatus);
			
			MtStatus mtStatus = tapeDriveManager.getMtStatus(dataTransferElementName);
			System.out.println(mtStatus);
			
			tapeLibraryManager.unload(tapeLibraryName, storageElementSNo, dataTransferElementSNo);
			System.out.println("unload success");
			
			mtxStatus = tapeLibraryManager.getMtxStatus(tapeLibraryName);
			System.out.println(mtxStatus);
			
			mtStatus = tapeDriveManager.getMtStatus(dataTransferElementName);
			System.out.println(mtStatus);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
}
