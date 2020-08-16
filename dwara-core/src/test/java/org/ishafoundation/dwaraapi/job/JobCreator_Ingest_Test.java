package org.ishafoundation.dwaraapi.job;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Iterator;

import org.dbunit.Assertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificArtifactFactory;
import org.ishafoundation.dwaraapi.db.model.cache.CacheableTablesList;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Ingest_Test extends JobCreator_Test{

	private static final Logger logger = LoggerFactory.getLogger(JobCreator_Ingest_Test.class);

	@SuppressWarnings("rawtypes")
	@Autowired
	private DBMasterTablesCacheManager dBMasterTablesCacheManager;
	
	@Autowired
	private DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection;
	
	@Autowired
	private DomainUtil domainUtil;

	public JobCreator_Ingest_Test() {
		action = Action.ingest.name();
		requestInputFilePath = "/testcases/ingest_request.json";
	}
	
	@Override
	protected String fillPlaceHolders(String postBodyJson) {
		String artifact_name_1 = "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9" + "_"
				+ System.currentTimeMillis();
		String artifact_name_2 = "Shiva-Shambho_Everywhere_18-Nov-1980_Drone" + "_"
				+ System.currentTimeMillis();
		postBodyJson = postBodyJson.replace("<<artifact_name_1>>", artifact_name_1);
		postBodyJson = postBodyJson.replace("<<artifact_name_2>>", artifact_name_2);

		return postBodyJson;
	}

	@Test
//	public void test_b_Ingest() {
//		try {
//			JsonNode body = request.getDetails().getBody();
//			String artifactclassName = body.get("artifactclass").textValue();
//			Artifactclass artifactclass = (Artifactclass) dBMasterTablesCacheManager
//					.getRecord(CacheableTablesList.artifactclass.name(), artifactclassName);
//			Domain domain = artifactclass.getDomain();
//			Iterator<JsonNode> artifacts = body.get("artifact").elements();
//			while (artifacts.hasNext()) {
//				Request systemrequest = new Request();
//				systemrequest.setRequestRef(request);
//				systemrequest.setActionId(request.getActionId());
//				systemrequest.setRequestedAt(LocalDateTime.now());
//				
//				JsonNode artifactJsonNode = (JsonNode) artifacts.next();
//				String artifact_name = artifactJsonNode.get("artifact_name").textValue();
//				
//				RequestDetails details = new RequestDetails();
//				details.setArtifactclass_id(artifactclass.getId());
//				details.setSourcepath("some sourcepath");
//				details.setArtifact_name(artifact_name);
//				details.setPrev_sequence_code("some prev_sequence_code");
//
//				systemrequest.setDetails(details);
//				systemrequest = requestDao.save(systemrequest);
//
//				Artifact artifact = DomainSpecificArtifactFactory.getInstance(domain);
//				artifact.setName(artifact_name);
//				artifact.setArtifactclass(artifactclass);
//				domainUtil.getDomainSpecificArtifactRepository(domain).save(artifact);
//	
//				// TODO File related changes go here...
//				
//				
//	
//				logger.debug("successfully tested domain specific table testing");
//				jobCreator.createJobs(systemrequest, artifact);
//				
//				IDatabaseConnection  dbUnitDbConnection = dbUnitDatabaseConnection.getObject();		// Passing the connection so its reused...
//				
//				validateDBDataForIngestJobCreation(systemrequest, dbUnitDbConnection);
//			}
//
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}
//	}

	private void validateDBDataForIngestJobCreation(Request systemrequest, IDatabaseConnection  dbUnitDbConnection) throws Exception{
		// Assert Request table
	
		// Assert Artifact table
		
		// Assert File table
		
		// Assert Job table
		assertJobTable(systemrequest, dbUnitDbConnection);
	}
	
	private void assertJobTable(Request systemrequest, IDatabaseConnection  dbUnitDbConnection) throws Exception{
		int systemrequestId = systemrequest.getId();
	
		// partial database export
	    QueryDataSet partialDataSet = new QueryDataSet(dbUnitDbConnection);
	    partialDataSet.addTable("Job", "SELECT * FROM job WHERE request_id=" + systemrequestId);
	
	    ITable actualTable = partialDataSet.getTable("job");
	    
	    // Load expected data from an XML dataset
	    URL fileUrl = this.getClass().getResource("/dbunit_test_inputs/expected/ingest/jobcreation/job.xml");
	    IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File(fileUrl.getFile()));
	    ITable expectedTable = expectedDataSet.getTable("job");
	    
	//  // Assert actual database table match expected table
	//  Assertion.assertEquals(expectedTable, actualTable);
	    
	    //We have to exclude some columns from comparison
	    ITable filteredTable = DefaultColumnFilter.includedColumnsTable(actualTable, 
	    		expectedTable.getTableMetaData().getColumns());
	    Assertion.assertEquals(expectedTable, filteredTable); 
	    logger.info("DBUnit assertion success");
	}

}
