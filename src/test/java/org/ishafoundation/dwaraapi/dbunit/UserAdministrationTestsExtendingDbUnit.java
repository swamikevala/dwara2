package org.ishafoundation.dwaraapi.dbunit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.annotation.PostConstruct;

import org.dbunit.Assertion;
import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.ishafoundation.dwaraapi.entrypoint.resource.controller.admin.UserAdminController;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@TestPropertySource(locations = "classpath:/config/application-stage.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserAdministrationTestsExtendingDbUnit extends DBTestCase {

	@Autowired
	private Environment environment;
	
	@Autowired
	private UserAdminController userAdminController;
	
	@PostConstruct
	void initialiseDbProperties(){
		// TODO 
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, environment.getProperty("spring.datasource.driver-class-name"));
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, environment.getProperty("spring.datasource.url"));
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, environment.getProperty("spring.datasource.username"));
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, environment.getProperty("spring.datasource.password"));
	}
	
	
	@Override
    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(new FileInputStream("full.xml"));
    }


    // TODO parameterise these ...
    String username = "Sadhguru";
    String password = "Shambho";
    String newPassword = "ShivaShambho";
    
    @Test
    public void a_testRegistration() throws Exception {
//        // partial database export
//        QueryDataSet partialDataSet = new QueryDataSet(getConnection());
//        partialDataSet.addTable("user");
//        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("src/test/resources/dbunit_test_inputs/userRegistration_full.xml"));

        // Load expected data from an XML dataset
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/dbunit_test_inputs/userRegistration_full.xml"));
        ITable expectedTable = expectedDataSet.getTable("user");
        
        userAdminController.processRegister(username, password);
        
        // Fetch database data after executing your code
        IDataSet databaseDataSet = getConnection().createDataSet();
        ITable actualTable = databaseDataSet.getTable("user");
        
        Assertion.assertEquals(expectedTable, actualTable);
    }
    
    @Test
    public void b_testSetPassword() throws Exception {
    	
//        // partial entry assertion
//        QueryDataSet partialDataSet = new QueryDataSet(getConnection());
//        partialDataSet.addTable("user", "SELECT * FROM user WHERE name='" + username + "'");
//        partialDataSet.getTable("user");
//        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("src/test/resources/dbunit_test_inputs/userSetPassword_partial.xml"));
        
        // Load expected data from an XML dataset
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/dbunit_test_inputs/userSetPassword_partial.xml"));
        ITable expectedTable = expectedDataSet.getTable("user");

    	userAdminController.setpassword(username, newPassword);

        // Fetch database data after executing your code
        QueryDataSet databaseDataSet = new QueryDataSet(getConnection());
        databaseDataSet.addTable("user", "SELECT * FROM user WHERE name='" + username + "'");
        ITable actualTable = databaseDataSet.getTable("user");
        
        Assertion.assertEquals(expectedTable, actualTable);

    }

}
