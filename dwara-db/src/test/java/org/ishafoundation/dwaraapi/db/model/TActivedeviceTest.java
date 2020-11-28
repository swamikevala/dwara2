package org.ishafoundation.dwaraapi.db.model;

import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TActivedeviceDao;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class TActivedeviceTest {
	
	private static final Logger logger = LoggerFactory.getLogger(TActivedeviceTest.class);

	@Autowired
	private DeviceDao deviceDao;
	
	@Autowired
	private TActivedeviceDao tActivedeviceDao;
	
	
	
//	//@Test
//	public void test_a_InsertTActivedevice() {
//		Device device = deviceDao.findById(2).get();
//		
//		TActivedevice tActivedevice = new TActivedevice();
//		tActivedevice.setDevice(device);
//		tActivedevice.setDeviceStatus(DeviceStatus.AVAILABLE);
//		tActivedeviceDao.save(tActivedevice);
//	}
//	
//	@Test
//	public void test_b_getTActivedevice() {
//		List<TActivedevice> activeDevice = tActivedeviceDao.findAllByDeviceDevicetypeAndDeviceStatus(Devicetype.tape_drive, DeviceStatus.AVAILABLE);
//		for (TActivedevice tActivedevice : activeDevice) {
//			logger.debug(tActivedevice.getDevice().getDetails().getAutoloader_address()+"");
//		}
//	}
}
