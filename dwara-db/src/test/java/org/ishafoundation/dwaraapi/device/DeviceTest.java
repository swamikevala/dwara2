package org.ishafoundation.dwaraapi.device;

import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.master.configuration.json.DeviceDetails;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.junit.FixMethodOrder;
import org.junit.Test;
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
public class DeviceTest {
	
	private static final Logger logger = LoggerFactory.getLogger(DeviceTest.class);

	@Autowired
	private DeviceDao deviceDao;
	
	//@Test
	public void test_a_CreateDevices() {
		Device device = new Device();
		device.setId(1);
		device.setDevicetype(Devicetype.tape_autoloader);
		device.setUid("/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400");
		DeviceDetails deviceDetails = new DeviceDetails();
		int[] generations_supported = {6,7};
		deviceDetails.setGenerations_supported(generations_supported);
		deviceDetails.setSlots(24);
		deviceDetails.setMax_drives(3);
		device.setDetails(deviceDetails);
		deviceDao.save(device);
		
		deviceDetails = new DeviceDetails();
		deviceDetails.setType("LTO");
		deviceDetails.setGeneration(7);
		int[] readable_generations = {6,7};
		deviceDetails.setReadable_generations(readable_generations);
		int[] writeable_generations = {7};
		deviceDetails.setWriteable_generations(writeable_generations);		
		deviceDetails.setAutoloader_id(1);
		deviceDetails.setStandalone(false);

		device = new Device();
		device.setId(2);
		device.setDevicetype(Devicetype.tape_drive);
		device.setUid("/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst");
		deviceDetails.setAutoloader_address(0);
		device.setDetails(deviceDetails);
		deviceDao.save(device);
		
		device = new Device();
		device.setId(3);
		device.setDevicetype(Devicetype.tape_drive);
		device.setUid("/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1684087499-nst");
		deviceDetails.setAutoloader_address(1);
		device.setDetails(deviceDetails);
		deviceDao.save(device);

		device = new Device();
		device.setId(4);
		device.setDevicetype(Devicetype.tape_drive);
		device.setUid("/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1970448833-nst");
		deviceDetails.setAutoloader_address(2);
		device.setDetails(deviceDetails);
		deviceDao.save(device);
	}
	
	@Test
	public void test_b_getADevice() {
		Device device = deviceDao.findById(2).get();
		logger.debug("Add - " + device.getDetails().getAutoloader_address());
	}
}
