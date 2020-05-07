package org.ishafoundation.dwaraapi.dbunit.datasetgen;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;

import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.xml.FlatDtdDataSet;

public class DatabaseExportSample
	{
	    public static void main(String[] args) throws Exception
	    {
	        // database connection
	        Class driverClass = Class.forName("com.mysql.cj.jdbc.Driver");
	        Connection jdbcConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dwara_v2_test?useSSL=false&useLegacyDatetimeCode=false&serverTimezone=Asia/Kolkata", "dwara", "SadhanaPada9!");
	        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection,"dwara_v2_test");
	        
	        // write DTD file
	        FlatDtdDataSet.write(connection.createDataSet(), new FileOutputStream("test.dtd"));
	    }
	}

