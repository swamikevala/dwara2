package org.ishafoundation.dwaraapi.tape.library;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.tape.library.components.StorageElement;


/**
 * Object representing the below
 * 
 * mtx -f /dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400 status
 * 
 * 
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
 *
 */
public class MtxStatus {
	private String storageChangerName;
	private int noOfDrives;
	private int noOfSlots;
	private int noOfIESlots;
	private List<DataTransferElement> dteList = new ArrayList<DataTransferElement>();
	private List<StorageElement> seList = new ArrayList<StorageElement>();
	
	public String getStorageChangerName() {
		return storageChangerName;
	}
	public void setStorageChangerName(String storageChangerName) {
		this.storageChangerName = storageChangerName;
	}
	public int getNoOfDrives() {
		return noOfDrives;
	}
	public void setNoOfDrives(int noOfDrives) {
		this.noOfDrives = noOfDrives;
	}
	public int getNoOfSlots() {
		return noOfSlots;
	}
	public void setNoOfSlots(int noOfSlots) {
		this.noOfSlots = noOfSlots;
	}
	public int getNoOfIESlots() {
		return noOfIESlots;
	}
	public void setNoOfIESlots(int noOfIESlots) {
		this.noOfIESlots = noOfIESlots;
	}
	public List<DataTransferElement> getDteList() {
		return dteList;
	}
	public void setDteList(List<DataTransferElement> dteList) {
		this.dteList = dteList;
	}
	public DataTransferElement getDte(int dataTransferElementSNo) {
		List<DataTransferElement> dteList = getDteList();
		for (Iterator<DataTransferElement> iterator = dteList.iterator(); iterator.hasNext();) {
			DataTransferElement nthDataTransferElement = (DataTransferElement) iterator.next();
			int nthDataTransferElementSNo = nthDataTransferElement.getsNo(); 
			if(nthDataTransferElementSNo == dataTransferElementSNo) {
				return nthDataTransferElement;
			}
		}
		return null;
	}
	public List<StorageElement> getSeList() {
		return seList;
	}
	public void setSeList(List<StorageElement> seList) {
		this.seList = seList;
	}
	public List<String> getAllLoadedTapesInTheLibrary(){
		List<String> tapeList = new ArrayList<String>();
		
		List<DataTransferElement> dteList = getDteList();
		for (DataTransferElement nthDataTransferElement : dteList) {
			String vt = nthDataTransferElement.getVolumeTag();
			if(vt != null)
				tapeList.add(vt);
		}
		
		List<StorageElement> seList = getSeList();
		for (StorageElement nthStorageElement : seList) {
			String vt = nthStorageElement.getVolumeTag();
			if(vt != null)
				tapeList.add(vt);
		}
		
		return tapeList;
	}
	
	@Override
	public String toString() {
		return "storageChangerName : " + storageChangerName + " noOfDrives : " + noOfDrives + " noOfSlots : " + noOfSlots + " noOfIESlots : " + noOfIESlots + " dteList : " + dteList.toString() + " seList : " + seList.toString();
	}
}
