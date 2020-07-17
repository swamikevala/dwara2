package org.ishafoundation.dwaraapi.service;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ishafoundation.dwaraapi.api.req.ingest.RequestParams;
import org.ishafoundation.dwaraapi.api.req.ingest.UserRequest;
import org.ishafoundation.dwaraapi.api.req.ingest.mapper.RequestToEntityObjectMapper;
import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.model.cache.CacheableTablesList;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ArtifactService {

	private static final Logger logger = LoggerFactory.getLogger(ArtifactService.class);

	@Autowired
	protected RequestDao requestDao;
	
	@Autowired
	protected JobCreator jobCreator;

	@Autowired
	private RequestToEntityObjectMapper requestToEntityObjectMapper; 
	
	@Autowired
	private DomainUtil domainUtil;

	@SuppressWarnings("rawtypes")
	@Autowired
	private DBMasterTablesCacheManager dBMasterTablesCacheManager;

    public ResponseEntity<String> ingest(UserRequest userRequest){	
    	try {
	    	
		    	Request request = new Request();
				request.setActionId(Action.ingest);
		    	// request.setUser(userDao.findByName(requestedBy));
				// request.setUser(user);
				request.setRequestedAt(LocalDateTime.now());
				
				RequestDetails details = new RequestDetails();
				JsonNode postBodyJson = getRequestDetails(userRequest); 
				details.setBody(postBodyJson);
				request.setDetails(details);
				
		    	request = requestDao.save(request);
		    	int requestId = request.getId();
		    	logger.info("Request - " + requestId);
	
				// TODO - ??? - Artifactclass mismatch - api request passes string, while request.details has id...
		    	String artifactclassName = userRequest.getArtifactclass();
				Artifactclass artifactclass = (Artifactclass) dBMasterTablesCacheManager.getRecord(CacheableTablesList.artifactclass.name(), artifactclassName);
				Domain domain = artifactclass.getDomain();
		    	List<RequestParams> requestParamsList = userRequest.getArtifact();
		    	
		    	for (RequestParams requestParams : requestParamsList) {
					Request systemrequest = new Request();
					systemrequest.setRequestRef(request);
					systemrequest.setActionId(request.getActionId());
					systemrequest.setUser(request.getUser());
					systemrequest.setRequestedAt(LocalDateTime.now());
					

		    		RequestDetails systemrequestDetails = requestToEntityObjectMapper.getRequestDetails(requestParams);
		    		systemrequestDetails.setArtifactclass_id(artifactclass.getId()); // transitioning from global level on the request to artifact level...
					systemrequest.setDetails(systemrequestDetails);
					
					systemrequest = requestDao.save(systemrequest);
					logger.info("System request - " + systemrequest.getId());
					
//					Artifact derivedArtifact = DomainSpecificArtifactFactory.getInstance(domain);
//					derivedArtifact.setName(requestParams.getArtifact_name()+"derived");
//					derivedArtifact.setArtifactclass(artifactclass);
//					derivedArtifact = (Artifact) domainUtil.getDomainSpecificArtifactRepository(domain).save(derivedArtifact);
					
					Artifact artifact = domainUtil.getDomainSpecificArtifactInstance(domain);
					artifact.setName(requestParams.getArtifact_name());
					artifact.setArtifactclass(artifactclass);
					artifact = (Artifact) domainUtil.getDomainSpecificArtifactRepository(domain).save(artifact);
					
//					Method artifactRefSetter = artifact.getClass().getMethod("set" + artifact.getClass().getSimpleName() + "Ref", Artifact.class);
//					artifactRefSetter.invoke(artifact, derivedArtifact);
					
					logger.info(artifact.getClass().getSimpleName() + " - " + artifact.getId());
					
					// TODO - Hardcoding
					String readyToIngestPath =  "C:\\data\\ingested";
					java.io.File libraryFileInStagingDir = new java.io.File(readyToIngestPath + java.io.File.separator + requestParams.getArtifact_name());
			    	String junkFilesStagedDirName = ".dwara-ignored";//configuration.getJunkFilesStagedDirName(); 
//			    	if(libraryFileInStagingDir.isDirectory())
//			    		junkFilesMover.moveJunkFilesFromMediaLibrary(libraryFileInStagingDir.getAbsolutePath());
			    	
			    	Collection<java.io.File> libraryFileAndDirsList = getFileList(libraryFileInStagingDir, junkFilesStagedDirName);
			    	int fileCount = libraryFileAndDirsList.size();
			        long size = FileUtils.sizeOf(libraryFileInStagingDir);
					
			        createFiles(readyToIngestPath, domain, artifact, libraryFileAndDirsList);
					
					jobCreator.createJobs(systemrequest, artifact);
				}

			
		}catch (Exception e) {
			e.printStackTrace();
		}

    	return ResponseEntity.status(HttpStatus.ACCEPTED).body("Done");
    }
	
	// TODO - Unnecessary conversion happening...
	private JsonNode getRequestDetails(UserRequest userRequest) {
		JsonNode postBodyJson = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			String postBody = mapper.writeValueAsString(userRequest);
			postBodyJson = mapper.readValue(postBody, JsonNode.class);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return postBodyJson;
	}
	
	private void createFiles(String pathPrefix, Domain domain, Artifact artifact, Collection<java.io.File> libraryFileAndDirsList) throws Exception {
	
	    List<File> toBeAddedFileTableEntries = new ArrayList<File>(); 
	    for (Iterator<java.io.File> iterator = libraryFileAndDirsList.iterator(); iterator.hasNext();) {
			
			java.io.File file = (java.io.File) iterator.next();
			String filePath = file.getAbsolutePath();
			filePath = filePath.replace(pathPrefix + java.io.File.separator, ""); //filePath = filePath.replace(stagingSrcDirRoot + File1.separator, "");
			
			File nthFileRowToBeInserted = domainUtil.getDomainSpecificFileInstance(domain);
			nthFileRowToBeInserted.setPathname(filePath);
			
			//nthFileRowToBeInserted.setArtifact(artifact);
			
//			if(file.isFile())
//				nthFileRowToBeInserted.setChecksum(Md5Util.getChecksum(file, Checksumtype.sha256));// TODO : ??? - From where do we get the checksumtype???
			
			Method fileArtifactSetter = nthFileRowToBeInserted.getClass().getMethod("set" + artifact.getClass().getSimpleName(), artifact.getClass());
			fileArtifactSetter.invoke(nthFileRowToBeInserted, artifact);

			nthFileRowToBeInserted.setSize(FileUtils.sizeOf(file));
			toBeAddedFileTableEntries.add(nthFileRowToBeInserted);			
		}
	    
	    if(toBeAddedFileTableEntries.size() > 0) {
	    	FileRepository<File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
	    	domainSpecificFileRepository.saveAll(toBeAddedFileTableEntries);
	    	logger.info("File records created successfully");
	    }
	}
	
    private Collection<java.io.File> getFileList(java.io.File libraryFileInStagingDir, String junkFilesStagedDirName) {
        IOFileFilter dirFilter = null;
        Collection<java.io.File> libraryFileAndDirsList = null;
	    if(libraryFileInStagingDir.isDirectory()) {
			dirFilter = FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(junkFilesStagedDirName, null));
	    	libraryFileAndDirsList = FileUtils.listFilesAndDirs(libraryFileInStagingDir, TrueFileFilter.INSTANCE, dirFilter);
	    }else {
	    	libraryFileAndDirsList = new ArrayList<java.io.File>();
	    	libraryFileAndDirsList.add(libraryFileInStagingDir);
	    }
	    return libraryFileAndDirsList;
	}
}

