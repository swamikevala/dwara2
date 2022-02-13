package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.req.tag.TagArtifact;
import org.ishafoundation.dwaraapi.api.resp.request.RequestResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Tag;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.RequestService;
import org.ishafoundation.dwaraapi.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@CrossOrigin
@RestController
public class TagController {
    @Autowired
    TagService tagService;

    @Autowired
    RequestDao requestDao;

    @Autowired
    ArtifactDao artifact1Dao;

    @Autowired
    RequestService requestService;

    private static final Logger logger = LoggerFactory.getLogger(TagController.class);

    @PostMapping(value="/tags/artifacts", produces = "application/json")
    public ResponseEntity tagMultipleRequest(@RequestBody TagArtifact tagArtifact) {
        try {
            for (String tag : tagArtifact.getTags()) {
                for (int artifactId : tagArtifact.getArtifactIds()) {
                    tagService.tagArtifact(tag, artifactId);
                }
            }
        } catch (Exception e) {
			String errorMsg = "Unable to tag artifact - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(value="/tags/{tag}/artifact/{artifactId}", produces = "application/json") 
    public ResponseEntity tagArtifact(@PathVariable String tag, @PathVariable int artifactId) {
        try {
            tagService.tagArtifact(tag, artifactId);
        } catch (Exception e) {
			String errorMsg = "Unable to tag artifact - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping(value="/tags/{tag}/artifact/{artifactId}", produces = "application/json")
    public ResponseEntity deleteTagArtifact(@PathVariable String tag, @PathVariable int artifactId) {
        try {
            tagService.deleteTagArtifact(tag, artifactId);
        } catch (Exception e) {
			String errorMsg = "Unable to delete tag request - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping(value="/tags/artifacts", produces = "application/json")
    public ResponseEntity<List<RequestResponse>> getRequestsByTag(@RequestParam("tags") String tags) {
        List<RequestResponse> l = new ArrayList<RequestResponse>();
        try {
            String[] arrTags = tags.split(",");
            List<Artifact> listArtifact = new ArrayList<Artifact>();
            for (String tag : arrTags) {
                Tag t = tagService.getTag(tag);
                if(t != null && t.getArtifacts() != null) {
                    for (Artifact artifact : t.getArtifacts()) {
                        if(!listArtifact.contains(artifact))
                            listArtifact.add(artifact);
                    }
                }
            }
            for (Artifact artifact: listArtifact) {
                Request request = artifact.getQueryLatestRequest();
                RequestResponse requestResponse = requestService.frameRequestResponse(request, request.getType());
                l.add(requestResponse);
            }
        } catch (Exception e) {
			String errorMsg = "Unable to get requests by tag - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
        return ResponseEntity.status(HttpStatus.OK).body(l);
    }

    @GetMapping(value = "/tags/artifact/{artifactId}", produces = "application/json")
    public ResponseEntity<List<String>> getTagsByRequestId(@PathVariable int artifactId) {
        List<String> listTags = new ArrayList<String>();
        try {
            Artifact r = artifact1Dao.findById(artifactId).get();
            if(r != null) {
                List<Tag> l = new ArrayList<Tag>(r.getTags());
                for (Tag tag : l) {
                    listTags.add(tag.getTag());
                }
            }
        } catch (Exception e) {
			String errorMsg = "Unable to get tags by artifact id - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
        return ResponseEntity.status(HttpStatus.OK).body(listTags);
    }

    @GetMapping(value="/tags", produces = "application/json")
    public ResponseEntity<List<String>> getAllTags(){
		
		List<String> tags = null;
		try {
            tags = tagService.getAllTagsValue();
		} catch (Exception e) {
			String errorMsg = "Unable to get all tags - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		
		return ResponseEntity.status(HttpStatus.OK).body(tags);
    }
    
    @PostMapping(value="/tags", produces = "application/json")
    public ResponseEntity addTag(@RequestBody String tag) {
        try {
            Tag t = new Tag(tag);
			tagService.addTag(t);
		} catch (Exception e) {
			String errorMsg = "Unable to add a tag - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		
		return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping(value="/tags/{tag}", produces = "application/json")
    public ResponseEntity deleteTag(@PathVariable String tag) {
        try {
            tagService.deleteTag(tag);    
        } catch (Exception e) {
			String errorMsg = "Unable to delete a tag '" + tag + "'- " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		
		return new ResponseEntity(HttpStatus.OK);
        
    }
}
