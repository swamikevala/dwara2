package org.ishafoundation.dwaraapi.dbunit;

import java.io.File;
import java.io.FileInputStream;

import javax.annotation.PostConstruct;

import org.dbunit.Assertion;
import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.ishafoundation.dwaraapi.entrypoint.resource.controller.admin.UserAdminController;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@TestPropertySource(locations = "classpath:/config/application-stage.properties")
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
	DbUnitTestExecutionListener.class })
@SpringBootTest
public class UserAdministrationTestsUsingSpringDbunit {

	@Autowired
	private Environment environment;
	
	@Autowired
	private UserAdminController userAdminController;
	
//    @Autowired
//    private IDatabaseTester databaseTester;
	
//	@PostConstruct
//	void initialiseDbProperties(){
//		// TODO 
//        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, environment.getProperty("spring.datasource.driver-class-name"));
//        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, environment.getProperty("spring.datasource.url"));
//        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, environment.getProperty("spring.datasource.username"));
//        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, environment.getProperty("spring.datasource.password"));
//	}
	
	
//	@Override
//    protected IDataSet getDataSet() throws Exception {
//        return new FlatXmlDataSetBuilder().build(new FileInputStream("full.xml"));
//    }


    // TODO parameterise these ...
    String username = "Sadhguru";
    String password = "Shambho";
    String newPassword = "ShivaShambho";
    
//    @Before
//    protected void setUp() throws Exception {
//
//        // Get the XML and set it on the databaseTester
//        // Optional: get the DTD and set it on the databaseTester
//
//        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
//        databaseTester.setTearDownOperation(DatabaseOperation.NONE);
//        databaseTester.onSetup();
//    }
    
//    @Test
//    @DatabaseSetup("src/test/resources/dbunit_test_inputs/full.xml")
//    @ExpectedDatabase("src/test/resources/dbunit_test_inputs/userRegistration_full.xml")
//    public void a_testRegistration() throws Exception {
//      userAdminController.processRegister(username, password);
//    }
    
    @Test
    @DatabaseSetup("classpath:/dbunit_test_inputs/full.xml")
    @ExpectedDatabase("classpath:/dbunit_test_inputs/userSetPassword_partial.xml")
    public void b_testSetPassword() throws Exception {
    	userAdminController.setpassword(username, newPassword);
    }

    
//    public void a_testRegistration() throws Exception {
////        // partial database export
////        QueryDataSet partialDataSet = new QueryDataSet(getConnection());
////        partialDataSet.addTable("user");
////        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("src/test/resources/dbunit_test_inputs/userRegistration_full.xml"));
//
//        // Load expected data from an XML dataset
//        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/dbunit_test_inputs/userRegistration_full.xml"));
//        ITable expectedTable = expectedDataSet.getTable("user");
//        
//        userAdminController.processRegister(username, password);
//        
//        // Fetch database data after executing your code
//        IDataSet databaseDataSet = getConnection().createDataSet();
//        ITable actualTable = databaseDataSet.getTable("user");
//        
//        Assertion.assertEquals(expectedTable, actualTable);
//    }
    
//    @Test
//    public void b_testSetPassword() throws Exception {
//    	
////        // partial entry assertion
////        QueryDataSet partialDataSet = new QueryDataSet(getConnection());
////        partialDataSet.addTable("user", "SELECT * FROM user WHERE name='" + username + "'");
////        partialDataSet.getTable("user");
////        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("src/test/resources/dbunit_test_inputs/userSetPassword_partial.xml"));
//        
//        // Load expected data from an XML dataset
//        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/dbunit_test_inputs/userSetPassword_partial.xml"));
//        ITable expectedTable = expectedDataSet.getTable("user");
//
//    	userAdminController.setpassword(username, newPassword);
//
//        // Fetch database data after executing your code
//        QueryDataSet databaseDataSet = new QueryDataSet(getConnection());
//        databaseDataSet.addTable("user", "SELECT * FROM user WHERE name='" + username + "'");
//        ITable actualTable = databaseDataSet.getTable("user");
//        
//        Assertion.assertEquals(expectedTable, actualTable);
//
//    }

}
