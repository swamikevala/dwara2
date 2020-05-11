package org.ishafoundation.dwaraapi.dbunittests.storage.storagetype.tape.library;

import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeDriveMapperThread;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
	DbUnitTestExecutionListener.class })
@SpringBootTest
public class TapeDriveMapperTests {

//	@Autowired
//	private TapeDriveMapper tapeDriveMapper;
//	
//    @Test
//    @DatabaseSetup(type=DatabaseOperation.REFRESH, value="classpath:/dbunit_test_inputs/setup/tapedrivemapping/test_data_transfer_element_test1.xml")
//    @ExpectedDatabase(table="tapedrive", assertionMode=DatabaseAssertionMode.NON_STRICT, value="classpath:/dbunit_test_inputs/expected/tapedrivemapping/tapedrive_test1.xml")
//    public void test1() throws Exception {
//    	tapeDriveMapper.mapDrives();
//    }
//    
//    @Test
//    @DatabaseSetup(type=DatabaseOperation.REFRESH, value="classpath:/dbunit_test_inputs/setup/tapedrivemapping/test_data_transfer_element_test2.xml")
//    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value="classpath:/dbunit_test_inputs/expected/tapedrivemapping/tapedrive_test2.xml")
//    public void test2() throws Exception {
//    	tapeDriveMapper.mapDrives();
//    }
//    
//    @Test
//    @DatabaseSetup(type=DatabaseOperation.REFRESH, value="classpath:/dbunit_test_inputs/setup/tapedrivemapping/test_data_transfer_element_test3.xml")
//    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value="classpath:/dbunit_test_inputs/expected/tapedrivemapping/tapedrive_test3.xml")
//    public void test3() throws Exception {
//    	tapeDriveMapper.mapDrives();
//    }
//    
//    @Test
//    @DatabaseSetup(type=DatabaseOperation.REFRESH, value="classpath:/dbunit_test_inputs/setup/tapedrivemapping/test_data_transfer_element_test4.xml")
//    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value="classpath:/dbunit_test_inputs/expected/tapedrivemapping/tapedrive_test4.xml")
//    //@DatabaseTearDown(type=DatabaseOperation.TRUNCATE_TABLE, value="classpath:/dbunit_test_inputs/original/tapedrivemapping/tapedrivemapping_original.xml")
//    public void test4() throws Exception {
//    	tapeDriveMapper.mapDrives();
//    }
//    
//    // DatabaseTearDown doesnt work because of constraints. So calling the original state file to reset the DB to known state...
//    @Test
//    @DatabaseSetup(type=DatabaseOperation.REFRESH, value="classpath:/dbunit_test_inputs/setup/tapedrivemapping/test_data_transfer_element_original.xml")
//    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value="classpath:/dbunit_test_inputs/expected/tapedrivemapping/tapedrive_original.xml")
//    public void test5() throws Exception {
//    	tapeDriveMapper.mapDrives();
//    }    
    
    
    

	@Autowired
	private ApplicationContext applicationContext;
	
	
    @Test
    @DatabaseSetup(type=DatabaseOperation.REFRESH, value="classpath:/dbunit_test_inputs/setup/tapedrivemapping/test_data_transfer_element_test1.xml")
    @ExpectedDatabase(table="tapedrive", assertionMode=DatabaseAssertionMode.NON_STRICT, value="classpath:/dbunit_test_inputs/expected/tapedrivemapping/tapedrive_test1.xml")
    public void test1() throws Exception {
    	callTest();
    }
    
    @Test
    @DatabaseSetup(type=DatabaseOperation.REFRESH, value="classpath:/dbunit_test_inputs/setup/tapedrivemapping/test_data_transfer_element_test2.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value="classpath:/dbunit_test_inputs/expected/tapedrivemapping/tapedrive_test2.xml")
    public void test2() throws Exception {
    	callTest();
    }
    
    @Test
    @DatabaseSetup(type=DatabaseOperation.REFRESH, value="classpath:/dbunit_test_inputs/setup/tapedrivemapping/test_data_transfer_element_test3.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value="classpath:/dbunit_test_inputs/expected/tapedrivemapping/tapedrive_test3.xml")
    public void test3() throws Exception {
    	callTest();
    }
    
    @Test
    @DatabaseSetup(type=DatabaseOperation.REFRESH, value="classpath:/dbunit_test_inputs/setup/tapedrivemapping/test_data_transfer_element_test4.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value="classpath:/dbunit_test_inputs/expected/tapedrivemapping/tapedrive_test4.xml")
    //@DatabaseTearDown(type=DatabaseOperation.TRUNCATE_TABLE, value="classpath:/dbunit_test_inputs/original/tapedrivemapping/tapedrivemapping_original.xml")
    public void test4() throws Exception {
    	callTest();
    }
    
    // DatabaseTearDown doesnt work because of constraints. So calling the original state file to reset the DB to known state...
    @Test
    @DatabaseSetup(type=DatabaseOperation.REFRESH, value="classpath:/dbunit_test_inputs/setup/tapedrivemapping/test_data_transfer_element_original.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value="classpath:/dbunit_test_inputs/expected/tapedrivemapping/tapedrive_original.xml")
    public void test5() throws Exception {
    	callTest();
    }
    
    private void callTest() {
    	TapeDriveMapperThread tdmt = applicationContext.getBean(TapeDriveMapperThread.class);
    	//Executors.newSingleThreadExecutor().execute(tdmt);
    	tdmt.run();// ya invoking the run method straight for tests... so that a separate thread is not created prompting junit main thread to close soon... 
    }
}


