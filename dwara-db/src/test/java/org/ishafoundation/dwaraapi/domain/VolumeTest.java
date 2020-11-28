package org.ishafoundation.dwaraapi.domain;

import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
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
public class VolumeTest {

	@Autowired
	private VolumeDao volumeDao;
	
	@Test
	public void test_a_UpdateVolumeDetails() {
//		Volume volume = volumeDao.findById(2).get();
//		
//		VolumeDetails volumeDetails = new VolumeDetails();
//		volumeDetails.setBarcoded(true);
//		volumeDetails.setBlocksize(1024);
////		volumeDetails.setGeneration(7);
////		volumeDetails.setMountpoint(mountpoint);
////		volumeDetails.setProvider_id(provider_id);
//		volume.setDetails(volumeDetails);
//		volumeDao.save(volume);
	}
	

}
