package org.ishafoundation.dwaraapi.job;

import java.time.LocalDateTime;
import java.util.Map;

import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.DomainAttributeConverter;
import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificArtifactFactory;
import org.ishafoundation.dwaraapi.db.model.cache.CacheableTablesList;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.json.SubrequestDetails;
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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreatorTest {

	private static final Logger logger = LoggerFactory.getLogger(JobCreatorTest.class);

	@Autowired
	private RequestDao requestDao;

	@Autowired
	private SubrequestDao subrequestDao;

	@SuppressWarnings("rawtypes")
	@Autowired
	private Map<String, ArtifactRepository> artifactDaoMap;

	@SuppressWarnings("rawtypes")
	@Autowired
	private DBMasterTablesCacheManager dBMasterTablesCacheManager;

	@Autowired
	private JobCreator jobCreator;

	@Autowired
	private DomainAttributeConverter domainAttributeConverter;

	//@Test
	public void test_a_Format() {
		try {
			Action action = Action.format;
			Request request = new Request();
			request.setAction(action);
			requestDao.save(request);

			Subrequest subrequest = new Subrequest();
			subrequest.setRequest(request);
			subrequest.setAction(action);

			SubrequestDetails details = new SubrequestDetails();
			details.setVolume_uid("V4A002"); // TODO how do we validate that the volume passed is only physical and not
			// group
			// details.setGroup_volume_uid("V4A");

			// details.setVolume_type(volume_type);
			// details.setVolumetype_id(1);
			details.setForce(false);

			subrequest.setDetails(details);
			subrequestDao.save(subrequest);

			jobCreator.createJobs(request, subrequest, null);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@Test
	public void test_b_Ingest() {
		Action action = Action.ingest;

		Domain domain = getDomain("userrequestgoeshere");
		try {
			Request request = new Request();
			request.setAction(action);
			request.setDomain(domain);
			// request.setUser(user);
			request.setRequestedAt(LocalDateTime.now());
			requestDao.save(request);

			Subrequest subrequest = new Subrequest();
			subrequest.setRequest(request);
			subrequest.setAction(action);

			String artifact_name = "10058_Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9"
					+ System.currentTimeMillis();

			SubrequestDetails details = new SubrequestDetails();
			details.setArtifactclass_id(1);
			details.setSourcepath("some sourcepath");
			details.setArtifact_name(artifact_name);
			// details.setArtifact_id(artifact_id);
			details.setPrev_sequence_code("some prev_sequence_code");

			subrequest.setDetails(details);
			subrequestDao.save(subrequest);
			System.out.println("successfully tested json insert");

			Artifactclass artifactclass = (Artifactclass) dBMasterTablesCacheManager
					.getRecord(CacheableTablesList.artifactclass.name(), "pub-video");

			System.out.println("successfully tested config table caching");

			String domainAsString = domainAttributeConverter.convertToDatabaseColumn(domain);
			String domainSpecificArtifactName = "artifact" + domainAsString;
			Artifact artifact = DomainSpecificArtifactFactory.getInstance(domainSpecificArtifactName);
			artifact.setName(
					"10058_Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9" + System.currentTimeMillis());
			artifact.setArtifactclass(artifactclass);
			artifactDaoMap.get(domainSpecificArtifactName + "Dao").save(artifact);

			// TODO File related changes go here...

			System.out.println("successfully tested domain specific table testing");
			jobCreator.createJobs(request, subrequest, artifact);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	//@Test
	public void test_c_Restore() {
		try {
			Action action = Action.restore;

			Domain domain = getDomain("userrequestgoeshere");
			Location location = getLocation("userrequestgoeshere");

			Request request = new Request();
			request.setAction(action);
			request.setDomain(domain);
			requestDao.save(request);

			Subrequest subrequest = new Subrequest();
			subrequest.setRequest(request);
			subrequest.setAction(action);

			SubrequestDetails details = new SubrequestDetails();
			int file_id = 1;

			details.setFile_id(file_id);
			// details.setPriority(priority);
			details.setLocation_id(location.getId());
			details.setOutput_folder("some output_folder");
			details.setDestinationpath("some dest path");
			details.setVerify(false); // overwriting default archiveformat.verify during restore

			subrequest.setDetails(details);
			subrequestDao.save(subrequest);
			System.out.println("successfully tested json insert");

			jobCreator.createJobs(request, subrequest, null);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	//@Test
	public void test_d_Finalise() {
		try {
			Request request = new Request();
			request.setAction(Action.finalize);
			requestDao.save(request);

			Subrequest subrequest = new Subrequest();
			subrequest.setRequest(request);
			subrequestDao.save(subrequest);

			jobCreator.createJobs(request, subrequest, null);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private Domain getDomain(String userRequest) {
		// to get domaindefault we might need a util... or a query...
		Domain domain = null;// from user request
		if (domain == null)
			domain = Domain.one; // defaulting to the domain configured as default...
		return domain;
	}

	private Location getLocation(String userRequest) {
		// to get domaindefault we might need a util... or a query...
		Location location = null; // userRequest.getlocation(); null;// from user request
		if (location == null) {
			location = (Location) dBMasterTablesCacheManager.getRecord(CacheableTablesList.location.name(), "LR"); // defaulting
			// to
			// the
			// domain
			// configured
			// as
			// default...
		}
		return location;
	}
	//	
	//	@Test
	//	public void testVerify() {
	//    	Request request = new Request();
	//    	request.setAction(Action.verify);
	//    	requestDao.save(request);
	//    	
	//    	Subrequest subrequest = new Subrequest();
	//    	subrequest.setRequest(request);
	//    	subrequestDao.save(subrequest);
	//		
	//    	jobCreator.createJobs(request, subrequest, null);
	//	}
	//	
}
