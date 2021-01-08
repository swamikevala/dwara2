package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.resp.request.RequestResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Tag;
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
import org.springframework.web.bind.annotation.RestController;


@CrossOrigin
@RestController
public class TagController {
    @Autowired
    TagService tagService;

    @Autowired
    RequestDao requestDao;

    @Autowired
    RequestService requestService;

    private static final Logger logger = LoggerFactory.getLogger(TagController.class);

    @PostMapping(value="/tags/{tag}/request/{requestId}") 
    public ResponseEntity<String> tagRequest(@PathVariable String tag, @PathVariable int requestId) {
        try {
            Tag t = tagService.getTag(tag);
            if(t == null) {
                t = new Tag(tag);
            }
            Request r = t.getRequestById(requestId);
            if(r == null) {
                r = new Request(requestId);
                t.addRequest(r);
            }
            //save or add tag
            tagService.addTag(t);
        } catch (Exception e) {
			String errorMsg = "Unable to tag request - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    @DeleteMapping(value="/tags/{tag}/request/{requestId}")
    public ResponseEntity<String> deleteTagRequest(@PathVariable String tag, @PathVariable int requestId) {
        try {
            Tag t = tagService.getTag(tag);
            if(t == null) {
                t = new Tag(tag);
            }
            Request r = t.getRequestById(requestId);
            if(r != null) {
                t.deleteRequest(r);
            }
            //save or add tag
            tagService.addTag(t);
        } catch (Exception e) {
			String errorMsg = "Unable to delete tag request - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    @GetMapping(value="/tags/{tag}")
    public ResponseEntity<List<RequestResponse>> getRequestsByTag(@PathVariable String tag) {
        List<RequestResponse> l;
        try {
            Tag t = tagService.getTag(tag);
            if(t != null && t.getRequests() != null) {
                l = new ArrayList<RequestResponse>();

                for (Request request : t.getRequests()) {
                    RequestResponse requestResponse = requestService.frameRequestResponse(request, request.getType());
                    l.add(requestResponse);
                }
            }
            else {
                l =  new ArrayList<RequestResponse>();
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

    @GetMapping(value = "/tags/request/{requestId}")
    public ResponseEntity<List<String>> getTagsByRequestId(@PathVariable int requestId) {
        List<String> listTags = new ArrayList<String>();
        try {
            Request r = requestDao.findById(requestId).get();
            if(r != null) {
                List<Tag> l = new ArrayList<Tag>(r.getTags());
                for (Tag tag : l) {
                    listTags.add(tag.getTag());
                }
            }
        } catch (Exception e) {
			String errorMsg = "Unable to get tags by request id - " + e.getMessage();
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
    
    @PostMapping(value="/tags")
    public ResponseEntity<String> addTag(@RequestBody String tag) {
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
		
		return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    @DeleteMapping(value="/tags/{tag}")
    public ResponseEntity<String> deleteTag(@PathVariable String tag) {
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
		
		return ResponseEntity.status(HttpStatus.OK).body("OK");
        
    }
}
