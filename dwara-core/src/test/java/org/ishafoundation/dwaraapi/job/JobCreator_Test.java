package org.ishafoundation.dwaraapi.job;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.ActionAttributeConverter;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Test {

	protected Request request = null;
	protected String requestInputFilePath = null;
	protected String action = null;
	
	@Autowired
	protected RequestDao requestDao;
	
	@Autowired
	protected JobCreator jobCreator;
	
	@Before
	public void createRequest() throws Exception {
		request = new Request();
		ActionAttributeConverter actionAttributeConverter = new ActionAttributeConverter();
		Action actionn = actionAttributeConverter.convertToEntityAttribute(action);
		request.setActionId(actionn);
		if(actionn == Action.restore)
			request.setDomain(getDomain(null));
		// request.setUser(user);
		request.setRequestedAt(LocalDateTime.now());

		
		RequestDetails details = new RequestDetails();
		if(requestInputFilePath != null) {
			URL fileUrl = this.getClass().getResource(requestInputFilePath);
			String postBodyJson = FileUtils.readFileToString(new File(fileUrl.getFile()));
			
			postBodyJson = fillPlaceHolders(postBodyJson);
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode postBody = mapper.readValue(postBodyJson, JsonNode.class);
			details.setBody(postBody);
		}
		request.setDetails(details);
		requestDao.save(request);
	}
	
	private Domain getDomain(String userRequest) {
		// to get domaindefault we might need a util... or a query...
		Domain domain = null;// from user request
		if (domain == null)
			domain = Domain.one; // defaulting to the domain configured as default...
		return domain;
	}

	protected String fillPlaceHolders(String postBodyJson) {
		// TODO Auto-generated method stub
		return postBodyJson;
	}
	
	protected void createSingleSystemrequestAndJobs() throws Exception{
		Request systemrequest = new Request();
		systemrequest.setRequestRef(request);
		systemrequest.setActionId(request.getActionId());
		systemrequest.setDomain(request.getDomain());
		systemrequest.setRequestedAt(LocalDateTime.now());
	
		RequestDetails details = getSystemrequestDetails();
		systemrequest.setDetails(details);
		systemrequest = requestDao.save(systemrequest);
		
		jobCreator.createJobs(systemrequest, null);
	}
	
	protected RequestDetails getSystemrequestDetails() {
		// TODO Auto-generated method stub
		return null;
	}

}