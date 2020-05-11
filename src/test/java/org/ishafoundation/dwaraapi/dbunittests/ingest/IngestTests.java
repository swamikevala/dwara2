package org.ishafoundation.dwaraapi.dbunittests.ingest;

import java.io.File;
import java.net.URL;

import org.dbunit.Assertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.ishafoundation.dwaraapi.controller.IngestSetup;
import org.ishafoundation.dwaraapi.entrypoint.resource.RequestWithSubrequestDetails;
import org.ishafoundation.dwaraapi.entrypoint.resource.controller.LibraryController;
import org.ishafoundation.dwaraapi.job.JobManager;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
@SpringBootTest
public class IngestTests extends IngestSetup{

	@Autowired 
	LibraryController libraryController;
	
	@Autowired
	private JobManager jobManager;
	
	@Autowired
	private DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection;
	
	@Test
	public void a_JobCreation() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("pgurumurthy", "password"));
		
		ResponseEntity<RequestWithSubrequestDetails> response = libraryController.ingest(userRequest);
		IDatabaseConnection  dbUnitDbConnection = dbUnitDatabaseConnection.getObject();		// Passing the connection so its reused...
		
		validateDBDataForIngestJobCreation(response.getBody(), dbUnitDbConnection);
	}

	private void validateDBDataForIngestJobCreation(RequestWithSubrequestDetails response, IDatabaseConnection  dbUnitDbConnection) throws Exception{
		// Assert Request table
		
		// Assert Subrequest table

		// Assert Library table
		
		// Assert File table
		
		// Assert Job table
		assertJobTable(response, dbUnitDbConnection);
	}
	
	private void assertJobTable(RequestWithSubrequestDetails response, IDatabaseConnection  dbUnitDbConnection) throws Exception{
		int subrequestId = response.getSubrequestList().get(0).getId();
        // Fetch database data after executing your code
//        IDataSet databaseDataSet = dbUnitDbConnection.createDataSet();
//        ITable actualTable = databaseDataSet.getTable("TABLE_NAME");

		// partial database export
        QueryDataSet partialDataSet = new QueryDataSet(dbUnitDbConnection);
        partialDataSet.addTable("Job", "SELECT * FROM job WHERE subrequest_id=" + subrequestId);

        ITable actualTable = partialDataSet.getTable("job");

        
        // Load expected data from an XML dataset
        URL fileUrl = this.getClass().getResource("/dbunit_test_inputs/expected/ingest/jobcreation/job.xml");
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File(fileUrl.getFile()));
        ITable expectedTable = expectedDataSet.getTable("job");
//      // Assert actual database table match expected table
//      Assertion.assertEquals(expectedTable, actualTable);
        
        //We have to exclude some columns from comparison
        ITable filteredTable = DefaultColumnFilter.includedColumnsTable(actualTable, 
        		expectedTable.getTableMetaData().getColumns());
        Assertion.assertEquals(expectedTable, filteredTable); 

	}

	@Test
	public void b_processJobs() throws Exception {
		
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("pgurumurthy", "password"));
		
		// we need to ensure no other jobs are running...
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
