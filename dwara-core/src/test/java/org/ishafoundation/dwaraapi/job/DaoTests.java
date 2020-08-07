package org.ishafoundation.dwaraapi.job;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DaoTests{
	
	@Autowired
	private DeviceDao deviceDao;
	
	@Autowired
	private RequestDao requestDao;	
	
	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private DomainUtil domainUtil;
	
//	@Test
//	public void testDevice(){
//		List<Device> tapelibraryDeviceList = deviceDao.findAllByDevicetype(Devicetype.tape_autoloader);
//		List<Device> tapedriveDeviceList = deviceDao.findAllByDevicetype(Devicetype.tape_drive);
//
//		HashMap<String, List<Device>> tapeLibraryName_TapeDriveList_Map = new HashMap<String, List<Device>>();
//		for (Device tapelibrary : tapelibraryDeviceList) {
//			int tapelibraryId = tapelibrary.getId();
//			String tapelibraryName = tapelibrary.getWwnId();
//			for (Device tapedrive : tapedriveDeviceList) {
//				if(tapedrive.getDetails().getAutoloader_id() == tapelibraryId) {
//					List<Device> tapedriveListForNthTapelibrary = tapeLibraryName_TapeDriveList_Map.get(tapelibraryName);
//					if(tapedriveListForNthTapelibrary == null) {
//						tapedriveListForNthTapelibrary = new ArrayList<Device>();
//						tapeLibraryName_TapeDriveList_Map.put(tapelibraryName, tapedriveListForNthTapelibrary);
//					}
//					tapedriveListForNthTapelibrary.add(tapedrive);
//				}
//			}
//			System.out.println("tapelibraryName -- " + tapelibraryName + " drive list" + tapeLibraryName_TapeDriveList_Map.get(tapelibraryName) + "one val" + tapeLibraryName_TapeDriveList_Map.get(tapelibraryName).get(1).getWwnId());
//		}
//	}
	
	public void testFileVolume() {
		Volume vol = getVolume(Domain.one, 60, "3");
		System.out.println(vol.getId());
	}
	
	private Volume getVolume(Domain domain, int fileIdToBeRestored, String locationId) {
    	FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
    	FileVolume fileVolume = domainSpecificFileVolumeRepository.findByIdFileIdAndVolumeLocationId(fileIdToBeRestored, locationId);
		return fileVolume.getVolume();
	}
	
	public void testRequestDao() {
		List<Status> statusList = new ArrayList<Status>();
		statusList.add(Status.queued);
		statusList.add(Status.in_progress);
		
		List<Action> actionList = new ArrayList<Action>();
		actionList.add(Action.map_tapedrives);
		actionList.add(Action.format);
		
		long tdOrFormatJobInFlight = jobDao.countByStoragetaskActionIdInAndStatus(actionList, Status.in_progress);
		System.out.println(tdOrFormatJobInFlight);
	}
	
}
