package org.ishafoundation.dwaraapi;

import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.service.VolumeService;
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
public class JobCreator_Finalize_Test {
	

	private static final Logger logger = LoggerFactory.getLogger(JobCreator_Finalize_Test.class);

	@Autowired
	VolumeService volumeService;
		
	@Test
	public void test_Finalize() {
		try {
			volumeService.finalize("V5A001", Domain.one); // TODO : Why domain needed? Filevolume and Artifactvolume are domain-ed
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
