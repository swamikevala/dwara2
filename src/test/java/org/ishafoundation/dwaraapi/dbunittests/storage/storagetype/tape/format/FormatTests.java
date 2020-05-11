package org.ishafoundation.dwaraapi.dbunittests.storage.storagetype.tape.format;

import org.ishafoundation.dwaraapi.api.req.Format;
import org.ishafoundation.dwaraapi.entrypoint.resource.controller.TapeController;
import org.ishafoundation.dwaraapi.job.JobManager;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseSetups;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
@SpringBootTest
public class FormatTests {

	@Autowired 
	TapeController tapeController;
	
	@Autowired
	private JobManager jobManager;
	
	@Test
	// Spring dbunit test doesnt support this @WithMockUser(username = "pgurumurthy", password = "pwd")
	@DatabaseSetups({
    	@DatabaseSetup(type=DatabaseOperation.DELETE, value="classpath:/dbunit_test_inputs/original/format/tape_to_be_deleted.xml"),
    	@DatabaseSetup(type=DatabaseOperation.DELETE, value="classpath:/dbunit_test_inputs/setup/format/alldrivesavailable_unloaded/test_data_transfer_element.xml"),
    	@DatabaseSetup(type=DatabaseOperation.DELETE, value="classpath:/dbunit_test_inputs/setup/format/alldrivesavailable_unloaded/test_mt_status.xml"),
    	@DatabaseSetup(type=DatabaseOperation.CLEAN_INSERT, value="classpath:/dbunit_test_inputs/setup/format/alldrivesavailable_unloaded/test_storage_element.xml"),
    	@DatabaseSetup(type=DatabaseOperation.CLEAN_INSERT, value="classpath:/dbunit_test_inputs/setup/format/alldrivesavailable_unloaded/tapedrive.xml"),
    	@DatabaseSetup(type=DatabaseOperation.INSERT, value="classpath:/dbunit_test_inputs/setup/format/alldrivesavailable_unloaded/test_mt_status.xml"),
    	@DatabaseSetup(type=DatabaseOperation.INSERT, value="classpath:/dbunit_test_inputs/setup/format/alldrivesavailable_unloaded/test_data_transfer_element.xml")
    })
	public void a_JobCreation() throws Exception {
		
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("pgurumurthy", "password"));
		
		Format requestBody = new Format();
		requestBody.setBarcode("V5A999L7");
		requestBody.setType("LTO7");
		requestBody.setForce(false);
		
		tapeController.format(requestBody);
	}

	@Test
	// Spring dbunit test doesnt support this @WithMockUser(username = "pgurumurthy", password = "pwd")
	@ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED, value="classpath:/dbunit_test_inputs/expected/format/tape.xml", table="tape", query="select * from tape where barcode=\"V5A999L7\"")
	public void b_processJobs() throws Exception {
		
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("pgurumurthy", "password"));
		
		jobManager.processJobs();
		
		try {
			Thread.sleep(20000); // sleeping for 20 secs before we do the expected DB verification
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println();
	}
}
