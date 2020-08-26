package org.ishafoundation.dwaraapi.storage.storagetype.tape;

import java.time.LocalDateTime;
import java.util.HashMap;

import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class VolumeFinalizer {

	private static final Logger logger = LoggerFactory.getLogger(VolumeFinalizer.class);

	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private JobCreator jobCreator;
	
	// TODO : Why domain needed? ANS: Filevolume and Artifactvolume needed for generating the index are "domain-ed"
	public String finalize(String volumeId, User user, Domain domain) throws Exception{
			Request request = new Request();
			request.setType(RequestType.user);
			request.setActionId(Action.finalize);
			request.setRequestedBy(user);
			request.setRequestedAt(LocalDateTime.now());
			request.setDomain(domain);
			RequestDetails details = new RequestDetails();
			ObjectMapper mapper = new ObjectMapper();
			
//			String jsonAsString = "{\"volume_id\":\""+volumeId+"\"}";

			HashMap<String, Object> data = new HashMap<String, Object>();
	    	data.put("volume_id", volumeId);
	    	String jsonAsString = mapper.writeValueAsString(data);
			
			JsonNode postBodyJson = mapper.readValue(jsonAsString, JsonNode.class);
			details.setBody(postBodyJson);
			request.setDetails(details);

			request = requestDao.save(request);
			int requestId = request.getId();
			logger.info("Request - " + requestId);


			Request systemrequest = new Request();
			systemrequest.setRequestRef(request);
			systemrequest.setActionId(request.getActionId());
			systemrequest.setRequestedBy(request.getRequestedBy());
			systemrequest.setRequestedAt(LocalDateTime.now());
			systemrequest.setDomain(request.getDomain());

			RequestDetails systemrequestDetails = new RequestDetails();
			systemrequestDetails.setVolume_id(volumeId);
			systemrequest.setDetails(systemrequestDetails);
			systemrequest = requestDao.save(systemrequest);
			logger.info("System request - " + systemrequest.getId());


			jobCreator.createJobs(systemrequest, null);

		return "Done";
	}
}
