package z_dbunit.datasetgen;

import java.io.FileOutputStream;

import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.mysql.MySqlMetadataHandler;
import org.dbunit.ext.oracle.OracleDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

import com.github.springtestdbunit.bean.DatabaseConfigBean;

public class FlatXmlDataSetFromDB_Generator extends DBTestCase {

    public FlatXmlDataSetFromDB_Generator(String name) {
        super(name);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, "com.mysql.cj.jdbc.Driver");
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, "jdbc:mysql://localhost:3306/dwara_v2_test?useSSL=false&useLegacyDatetimeCode=false&serverTimezone=Asia/Kolkata");
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, "dwara");
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, "SadhanaPada9!");
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_SCHEMA, "dwara_v2_test");
    }

    protected DatabaseOperation getSetUpOperation() throws Exception {
        return DatabaseOperation.NONE;
    }

    protected DatabaseOperation getTearDownOperation() throws Exception {
        return DatabaseOperation.NONE;
    }

    @Test
    public void testMe() throws Exception {
    	
        IDatabaseConnection connection = getConnection();

        DatabaseConfig dbConfig = connection.getConfig();

        // added this line to get rid of the warning
        dbConfig.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MySqlDataTypeFactory());
        dbConfig.setProperty(DatabaseConfig.PROPERTY_METADATA_HANDLER, new MySqlMetadataHandler());
        
//        FlatDtdDataSet.write(connection.createDataSet(), new FileOutputStream("test.dtd"));
        
        // partial database export
        QueryDataSet partialDataSet = new QueryDataSet(connection);
        partialDataSet.addTable("job");
        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("src\\test\\resources\\dbunit_test_inputs\\expected\\format\\job.xml"));
        
        partialDataSet = new QueryDataSet(connection);
        partialDataSet.addTable("tape");
        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("src\\test\\resources\\dbunit_test_inputs\\expected\\format\\tape.xml"));
        
        partialDataSet = new QueryDataSet(connection);
        partialDataSet.addTable("tapedrive");
        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("src\\test\\resources\\dbunit_test_inputs\\expected\\format\\tapedrive.xml"));
        
        partialDataSet = new QueryDataSet(connection);
        partialDataSet.addTable("test_data_transfer_element");
        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("src\\test\\resources\\dbunit_test_inputs\\expected\\format\\test_data_transfer_element.xml"));
        
        partialDataSet = new QueryDataSet(connection);
        partialDataSet.addTable("test_storage_element");
        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("src\\test\\resources\\dbunit_test_inputs\\expected\\format\\test_storage_element.xml"));
        
        partialDataSet = new QueryDataSet(connection);
        partialDataSet.addTable("test_mt_status");
        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("src\\test\\resources\\dbunit_test_inputs\\expected\\format\\test_mt_status.xml"));
        
//        QueryDataSet partialDataSet = new QueryDataSet(connection);
//        partialDataSet.addTable("test_data_transfer_element", "SELECT * FROM test_data_transfer_element");
//        partialDataSet.addTable("test_mt_status");
//        partialDataSet.addTable("test_storage_element");
//        partialDataSet.addTable("tapedrive", "SELECT * FROM tapedrive");
//        
//        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("tapedrivemapping_original.xml"));
       
    }

	@Override
	protected IDataSet getDataSet() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}