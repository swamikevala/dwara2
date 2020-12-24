package org.ishafoundation.dwaraapi.service;

import java.util.HashMap;
import java.util.Optional;

import org.ishafoundation.dwaraapi.api.resp.artifact.DeleteArtifactResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.resource.mapper.MiscObjectMapper;
import org.ishafoundation.dwaraapi.service.common.ArtifactDeleter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArtifactService extends DwaraService{

	private static final Logger logger = LoggerFactory.getLogger(ArtifactService.class);
	
	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private MiscObjectMapper miscObjectMapper; 
	
	@Autowired
	private ArtifactDeleter artifactDeleter; 
	
	
    public String renameArtifact(int artifactId) throws DwaraException{
		return null;
	}

    // TODO - will we get artifactId or artifactName as input from UI?
    public DeleteArtifactResponse deleteArtifact(int artifactId) throws Exception{
    	ArtifactRepository<Artifact> artifactRepository = null;
		Artifact artifact = null; // get the artifact details from DB
		Domain domain = null; 
	   	Domain[] domains = Domain.values();
		for (Domain nthDomain : domains) {
			artifactRepository = domainUtil.getDomainSpecificArtifactRepository(nthDomain);
			Optional<Artifact> artifactEntity = artifactRepository.findById(artifactId);
			if(artifactEntity.isPresent()) {
				artifact = artifactEntity.get();
				domain = nthDomain;
				break;
			}
		}
		
		artifactDeleter.validateArtifactclass(artifact.getArtifactclass().getId());
			
		Request request = artifact.getWriteRequest();//artifact.getqLatestRequest();
		
		artifactDeleter.validateRequest(request);
		
		artifactDeleter.validateJobsAndUpdateStatus(request);
		
		// Step 1 - Create User Request
		HashMap<String, Object> data = new HashMap<String, Object>();
    	data.put("artifactId", artifactId);
    	Request userRequest = createUserRequest(Action.delete, Status.in_progress, data);

    	artifactDeleter.cleanUp(userRequest, request,  domain, artifactRepository);
    	
    	userRequest.setRequestRef(request);
        userRequest.setStatus(Status.completed);
        requestDao.save(userRequest);
        logger.info(userRequest.getId() + " - Completed");
        
        DeleteArtifactResponse dr = new DeleteArtifactResponse();
        org.ishafoundation.dwaraapi.api.resp.artifact.Artifact artifactForResponse = miscObjectMapper.getArtifactForDeleteArtifactResponse(artifact);
        dr.setArtifact(artifactForResponse);
        dr.setUserRequestId(userRequest.getId());
        dr.setAction(Action.delete.name());
    	dr.setRequestedAt(getDateForUI(userRequest.getRequestedAt()));
    	dr.setRequestedBy(userRequest.getRequestedBy().getName());
        
        return dr;
    }
}

