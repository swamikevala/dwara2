package org.ishafoundation.dwaraapi.dbunit;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.annotation.PostConstruct;

import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.ishafoundation.dwaraapi.entrypoint.resource.controller.admin.UserAdminController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@TestPropertySource(locations = "classpath:/config/application-stage.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class SampleDBTest extends DBTestCase {

	@Autowired
	private Environment environment;
	
	@PostConstruct
	void initialiseDbProperties(){
		// TODO 
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, environment.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS));
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, environment.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL));
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, environment.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME));
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, environment.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD));
	}
	
	@Override
    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(new FileInputStream("full.xml"));
    }

    protected DatabaseOperation getSetUpOperation() throws Exception {
        return DatabaseOperation.NONE;
    }

    protected DatabaseOperation getTearDownOperation() throws Exception {
        return DatabaseOperation.NONE;
    }

    @Test
    public void testMe() throws Exception {

    	// TODO : integrate dbtest case with spring as the dataset./setup and teardown methods are not getting called.
    	//userAdminController.processRegister("abc", "abc");
    	
//        // Fetch database data after executing your code
//        IDataSet databaseDataSet = getConnection().createDataSet();
//        ITable actualTable = databaseDataSet.getTable("user");
        
        // partial database export
        QueryDataSet partialDataSet = new QueryDataSet(getConnection());
        //partialDataSet.addTable("FOO", "SELECT * FROM TABLE WHERE COL='VALUE'");
        partialDataSet.addTable("user");
        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("output.xml"));
    }
}
