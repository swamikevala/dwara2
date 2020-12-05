package org.ishafoundation.dwaraapi.job.creation;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;


@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Ingest_VideoPub_Test2 extends JobCreator_Ingest {
	
	private static final Logger logger = LoggerFactory.getLogger(JobCreator_Ingest_VideoPub_Test2.class);
	
	@Autowired
	private DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection;
	
	@Test
	@Sql(scripts = {"/data/sql/truncate_transaction_tables.sql","/data/sql/flowelement_video-pub.sql"})
	public void test_a_ingest() throws Exception {
		String testIngestArtifactName1 =  "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9";
		String artifact_name_1 = extractZip(testIngestArtifactName1,"V21000");

		String artifactclassId =  "video-pub";
		
		List<Job> jobList = synchronousActionForRequest(artifact_name_1, artifactclassId);
		
		
//		String testIngestArtifactName2 = "Shiva-Shambho_Everywhere_18-Nov-1980_Drone";
//		String artifact_name_2 = extractZip(testIngestArtifactName2,"V21001");

//		String testIngestArtifactName3 = "Cauvery-Calling_Day1-Sadhguru-Talking-With-People_Palace-Grounds-Bengaluru_02-Sep-2019_GoProApr6";
//		String artifact_name_3 = extractZip(testIngestArtifactName3);
		
		for (Job job : jobList) {
			String expected = getExpected(job);
			logger.info("---" + expected);
			String actual = "";
			//assertEquals(expected, actual);

		}
		
		Request systemrequest = jobList.get(0).getRequest();
		
		IDatabaseConnection  dbUnitDbConnection = dbUnitDatabaseConnection.getObject();		// Passing the connection so its reused...
		// partial database export
        QueryDataSet partialDataSet = new QueryDataSet(dbUnitDbConnection);
        partialDataSet.addTable("Job", "SELECT * FROM job WHERE request_id=" + systemrequest.getId());

        ITable actualTable = partialDataSet.getTable("job");
        
        // Load expected data from an XML dataset
        URL expectedJobs = this.getClass().getResource("/dbunit_test_inputs/expected/ingest/jobcreation/job.xml");
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File(expectedJobs.getFile()));
        ITable expectedTable = expectedDataSet.getTable("job");
        
        //We have to exclude some columns from comparison
        ITable filteredTable = DefaultColumnFilter.includedColumnsTable(actualTable, 
        		expectedTable.getTableMetaData().getColumns());
        
//	        Assertion.assertEquals(expectedTable, actualTable);
//	        try {
//	        	DatabaseAssertion databaseAssertion = DatabaseAssertionMode.NON_STRICT_UNORDERED.getDatabaseAssertion();
//	        	databaseAssertion.assertEquals(expectedTable, filteredTable); 
//	        	System.out.println("Im here...");
//	        }catch (Exception e) {
//	        	System.out.println("Im here 2...");
//				e.printStackTrace();
//			}
//	        

        validateArchiveFlow(1,"R");

		/**
		 * Now run proxy job so it generates output aritifact
		 */
		List<Job> proxyDependentJobList = callJobManagerAndStatusUpdater(systemrequest, artifactId + 1);
		for (Job proxyDependentJob : proxyDependentJobList) {
			String expected = getExpected(proxyDependentJob);
			//12:"mam-update"
			logger.info("***" + expected);
			String actual = "";
			//assertEquals(expected, actual);
		}
		
		validateArchiveFlow(13,"G");
	}
}
