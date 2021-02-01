package org.ishafoundation.dwaraapi.storage.storagetype.tape;

import java.time.LocalDateTime;
import java.util.HashMap;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
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
	private UserDao userDao;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private JobCreator jobCreator;
	
	public String finalize(String volumeId, String userName) throws Exception{
			Request request = new Request();
			request.setType(RequestType.user);
			request.setActionId(Action.finalize);
			request.setStatus(Status.queued);
			User user = userDao.findByName(userName);
			request.setRequestedBy(user);
			request.setRequestedAt(LocalDateTime.now());
			RequestDetails details = new RequestDetails();
			ObjectMapper mapper = new ObjectMapper();
			
//			String jsonAsString = "{\"volume_id\":\""+volumeId+"\"}";

			HashMap<String, Object> data = new HashMap<String, Object>();
	    	data.put("volumeId", volumeId);
	    	String jsonAsString = mapper.writeValueAsString(data);
			
			JsonNode postBodyJson = mapper.readValue(jsonAsString, JsonNode.class);
			details.setBody(postBodyJson);
			request.setDetails(details);

			request = requestDao.save(request);
			int requestId = request.getId();
			logger.info(DwaraConstants.USER_REQUEST + requestId);


			Request systemrequest = new Request();
			systemrequest.setType(RequestType.system);
			systemrequest.setRequestRef(request);
			systemrequest.setActionId(request.getActionId());
			systemrequest.setRequestedBy(request.getRequestedBy());
			systemrequest.setRequestedAt(LocalDateTime.now());

			RequestDetails systemrequestDetails = new RequestDetails();
			systemrequestDetails.setVolumeId(volumeId);
			
		   	Domain[] domains = Domain.values();
		   	Domain domain = null;
    		for (Domain nthDomain : domains) {
			    ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(nthDomain);
			    int artifactVolumeCount = domainSpecificArtifactVolumeRepository.countByIdVolumeId(volumeId);
    			if(artifactVolumeCount > 0) {
    				domain = nthDomain;
    				break;
    			}
			}
    		systemrequestDetails.setDomainId(domainUtil.getDomainId(domain));	
			systemrequest.setDetails(systemrequestDetails);
			
			systemrequest = requestDao.save(systemrequest);
			logger.info(DwaraConstants.SYSTEM_REQUEST + systemrequest.getId());


			jobCreator.createJobs(systemrequest, null);

		return "Done";
	}
}
