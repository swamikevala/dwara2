package org.ishafoundation.dwaraapi.job;

import java.time.LocalDateTime;

import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Test {

	protected Request request = null;
	protected Action action = null;
	
	@Autowired
	protected RequestDao requestDao;
	
	@Autowired
	protected JobCreator jobCreator;
	
	@BeforeClass
	public Request createRequest() throws Exception {
		Request request = new Request();
		request.setAction(action);
		request.setDomain(getDomain(null));
		// request.setUser(user);
		request.setRequestedAt(LocalDateTime.now());

		
		RequestDetails details = getRequestDetails();
		request.setDetails(details);
		return requestDao.save(request);
	}
	
	protected RequestDetails getRequestDetails() {
		return null;}
	
	private Domain getDomain(String userRequest) {
		// to get domaindefault we might need a util... or a query...
		Domain domain = null;// from user request
		if (domain == null)
			domain = Domain.one; // defaulting to the domain configured as default...
		return domain;
	}

}