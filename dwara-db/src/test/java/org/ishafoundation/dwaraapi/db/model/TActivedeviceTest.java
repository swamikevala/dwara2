package org.ishafoundation.dwaraapi.db.model;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TActivedeviceDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.TActivedevice;
import org.ishafoundation.dwaraapi.enumreferences.DeviceStatus;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class TActivedeviceTest {

	@Autowired
	private DeviceDao deviceDao;
	
	@Autowired
	private TActivedeviceDao tActivedeviceDao;
	
	
	
	//@Test
	public void test_a_InsertTActivedevice() {
		Device device = deviceDao.findById(2).get();
		
		TActivedevice tActivedevice = new TActivedevice();
		tActivedevice.setDevice(device);
		tActivedevice.setDeviceStatus(DeviceStatus.AVAILABLE);
		tActivedeviceDao.save(tActivedevice);
	}
	
	@Test
	public void test_b_getTActivedevice() {
		List<TActivedevice> activeDevice = tActivedeviceDao.findAllByDeviceDevicetypeAndDeviceStatus(Devicetype.tape_drive, DeviceStatus.AVAILABLE);
		for (TActivedevice tActivedevice : activeDevice) {
			System.out.println(tActivedevice.getDevice().getDetails().getAutoloader_address());
		}
	}
}
