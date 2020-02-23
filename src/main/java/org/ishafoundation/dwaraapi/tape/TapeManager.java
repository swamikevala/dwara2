package org.ishafoundation.dwaraapi.tape;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.storage.Tape;
import org.ishafoundation.dwaraapi.model.Volume;
import org.ishafoundation.dwaraapi.tape.drive.DriveStatusDetails;
import org.ishafoundation.dwaraapi.tape.drive.TapeDriveManager;
import org.ishafoundation.dwaraapi.tape.library.MtxStatus;
import org.ishafoundation.dwaraapi.tape.library.TapeLibraryManager;
import org.ishafoundation.dwaraapi.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.tape.library.components.StorageElement;

// Class responsible for the following
// 1) check if the tape is already on a drive
// 2) else choose a drive 
//		get the drive list - 
//		iterate the drive list and get the free drives
//		choose a drive using some load balancing - how???
// 		unload if any other tape - we should always check the status of the drive before unloading the tape. if the drive is busy we should not unload the tape.., (At the time of writing another thread/process is allowed to unload the tape)
// 		load the tape to be used
// get to the tape's current position/status that the tape head is pointing to - (mt status)
// go to the right/correct/exact block where the tape head need to be positioned
// 		for writing get the fileMark details from the DB tables...
// 		for restore seek the exact block (block details of the requested dwaraFileId come from DB)
// validate the block again(is there any command to do this double checking)

public class TapeManager{

	// we need to get this dynamically
	String tapeLibraryName = "/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400";
	

//	public synchronized DriveStatusDetails loadVolumeAndPositionTapeHeadForWriting(Volume toBeUsedVolume, int noOfArchivesAlreadyInVolume) throws Exception{
//		DriveStatusDetails dsd = null;
//		try {
//			int toBeUsedDataTransferElementNo = loadTapeOnToDrive(toBeUsedVolume);
//			dsd = TapeDriveManager.setTapeHeadPositionForWriting(toBeUsedDataTransferElementNo, noOfArchivesAlreadyInVolume);
//		}
//		catch (Exception e) {
//			System.err.println(e.getMessage()); e.printStackTrace();
//			throw e;
//		}
//		return dsd;
//	}
//	
//	public synchronized DriveStatusDetails loadVolumeAndPositionTapeHeadForReading(Volume toBeUsedVolume, int fileNumber) throws Exception{
//		DriveStatusDetails dsd = null;
//		try {
//			int toBeUsedDataTransferElementNo = loadTapeOnToDrive(toBeUsedVolume);
//			dsd = TapeDriveManager.setTapeHeadPositionForReading(toBeUsedDataTransferElementNo, fileNumber);
//		}
//		catch (Exception e) {
//			System.err.println(e.getMessage()); e.printStackTrace();
//			throw e;
//		}
//		return dsd;
//	}
//	
	private synchronized int loadTapeOnToDrive(Tape toBeUsedTape) throws Exception{
		int toBeUsedDataTransferElementNo = -5;

		try {
			MtxStatus mtxStatus = TapeLibraryManager.getMtxStatus(tapeLibraryName);
	
			DriveStatusDetails dsd = null;
	
			List<DriveStatusDetails> availablDriveDetailsList = new ArrayList<DriveStatusDetails>();
			List<DataTransferElement> dteList = mtxStatus.getDteList();
			System.out.println("dteList " +  dteList);
			for (Iterator<DataTransferElement> iterator = dteList.iterator(); iterator.hasNext();) {
				DataTransferElement nthDataTransferElement = (DataTransferElement) iterator.next();
				int dataTransferElementSNo = nthDataTransferElement.getsNo(); 
				if(toBeUsedTape.getBarcode().equals(nthDataTransferElement.getVolumeTag())) { // checking if the tape is already on any drive
					toBeUsedDataTransferElementNo = dataTransferElementSNo;
					System.out.println(toBeUsedTape.getBarcode() + " is already on drive " + toBeUsedDataTransferElementNo + " - skipping unload/locateVolume/load");
					break;
				} else {
					dsd = TapeDriveManager.getDriveDetails(dataTransferElementSNo);
					dsd.setDte(nthDataTransferElement);
					if(!dsd.getMtStatus().isBusy()) { // only not busy drives are candidates
						availablDriveDetailsList.add(dsd);
					} else {
						
					}
				}
			}
	
			
			if(toBeUsedDataTransferElementNo == -5) { // means volume is not already in one of the drives and needs to be loaded by us
				System.out.println(toBeUsedTape.getBarcode() + " is not in any drive and can be loaded into one of this " + availablDriveDetailsList);
				DriveStatusDetails driveStatusDetails = chooseADrive(availablDriveDetailsList);
				toBeUsedDataTransferElementNo = driveStatusDetails.getDte().getsNo();
				if(driveStatusDetails.getMtStatus().isDriveReady()){ // means drive is not empty and has another tape - so we need to unload the other tape
					System.out.println(toBeUsedDataTransferElementNo + " is not empty and has another tape - so we need to unload the other tape");
					if(!driveStatusDetails.getMtStatus().isBusy()) {
						System.out.println("Unloading ");
						TapeLibraryManager.unload(tapeLibraryName, driveStatusDetails.getDte().getStorageElementNo(), driveStatusDetails.getDte().getsNo());
						System.out.println("Unload successful ");
					}else {
						System.out.println("Something wrong with the logic. Drive is not supposed to be busy, but seems busy");
					}
				}
	
				// locate in which slot the volume is...
				List<StorageElement> seList = mtxStatus.getSeList();
				System.out.println("Now locating in which slot the volume is from the list ..." + seList);
				int storageElementNo = locateTheTapesStorageElement(seList, toBeUsedTape);
	
				// load the volume in the passed slot to the passed dte
				System.out.println("now loading " + toBeUsedTape.getBarcode() + " from " + storageElementNo + " and loading into drive " + toBeUsedDataTransferElementNo);
				TapeLibraryManager.load(tapeLibraryName, storageElementNo, toBeUsedDataTransferElementNo);
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());e.printStackTrace();
			throw e;
		}
		return toBeUsedDataTransferElementNo;
	}
		
	private DriveStatusDetails chooseADrive(List<DriveStatusDetails> emptyDriveDetailsList) {
		DriveStatusDetails driveStatusDetails = null;
		for (Iterator<DriveStatusDetails> iterator = emptyDriveDetailsList.iterator(); iterator.hasNext();) {
			driveStatusDetails = (DriveStatusDetails) iterator.next();
			break; // TODO : for now defaulting it with the first one
		}
		return driveStatusDetails;
	}

	private int locateTheTapesStorageElement(List<StorageElement> seList, Tape toBeUsedTape){
		int storageElementNo = -9;
		System.out.println("seList " + seList);
		System.out.println("toBeUsedVolume.getCode() " + toBeUsedTape.getBarcode());
		for (Iterator<StorageElement> iterator = seList.iterator(); iterator.hasNext();) {
			StorageElement nthStorageElement = (StorageElement) iterator.next();
			System.out.println("nthStorageElement.getVolumeTag() " + nthStorageElement.getVolumeTag());
			if(nthStorageElement.getVolumeTag() != null && nthStorageElement.getVolumeTag().equals(toBeUsedTape.getBarcode())) {
				storageElementNo = nthStorageElement.getsNo();
				System.out.println("storageElementNo " + storageElementNo);
			}
		}

		if(storageElementNo == -9) {
			System.err.println("Tape not inside the library");
			// TODO : Handle this...
		}
		System.out.println(toBeUsedTape.getBarcode() + " is in " +  storageElementNo);
		return storageElementNo;
	}
	
	/**
	 * 
	 * @param args[0] - barCode
	 * @param args[1] - noOfArchivesAlreadyInVolume
	 * @param args[2] - fileNumber
	 * 
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public static void main(String[] args) throws NumberFormatException, Exception {
//		String barCode = args[0];
//		int noOfArchivesAlreadyInVolume = Integer.parseInt(args[1]);
//		int fileNumber = Integer.parseInt(args[2]);
//		
//		Volume toBeUsedVolume = new Volume();
//		toBeUsedVolume.setCode(barCode);
//		
//		TapeManager tm = new TapeManager();
//		DriveStatusDetails dsd1 = tm.loadVolumeAndPositionTapeHeadForWriting(toBeUsedVolume, noOfArchivesAlreadyInVolume);
//		System.out.println(dsd1);
//		
//		DriveStatusDetails dsd2 = tm.loadVolumeAndPositionTapeHeadForReading(toBeUsedVolume, fileNumber);
//		System.out.println(dsd2);
	}
}
