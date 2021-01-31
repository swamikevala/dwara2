package org.ishafoundation.dwaraapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.ishafoundation.dwaraapi.db.dao.master.TagDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Tag;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagService extends DwaraService{
    @Autowired
    private TagDao tagDao;

    public void tagRequest(String tag, int requestId) {
        Tag t = getTag(tag);
        if(t == null) {
            t = new Tag(tag);
        }

        Request r = t.getRequestById(requestId);
        if(r == null) {
            r = new Request(requestId);
            t.addRequest(r);
        }

        //save
        addTag(t);
    }

    public void deleteTagRequest(String tag, int requestId) {
        Tag t = getTag(tag);
        if(t == null) {
            t = new Tag(tag);
        }
        Request r = t.getRequestById(requestId);
        if(r != null) {
            t.deleteRequest(r);
        }
        //save or add tag
        addTag(t);
    }

    public List<Request> getAllRequestsByTag(String tag) {
        Tag t = tagDao.findById(tag).get();
        return new ArrayList<Request>(t.getRequests());
    }

    public List<String> getAllTagsByRequestId(int requestId) {
        List<Tag> tags = new ArrayList<Tag>();
        tagDao.findByRequests_Id(requestId).forEach(tags::add);

        List<String> values = new ArrayList<String>();
        for (Tag tag : tags) {
            values.add(tag.getTag());
        }
        return values;
    }

    public List<String> getAllTagsValue(){
        List<Tag> tags = new ArrayList<Tag>();
        tagDao.findAll().forEach(tags::add);

        List<String> values = new ArrayList<String>();
        for (Tag tag : tags) {
            values.add(tag.getTag());
        }

        return values;
    }

    public List<Tag> getAllTags() {
        List<Tag> tags = new ArrayList<Tag>();
        tagDao.findAll().forEach(tags::add);
        return tags;
    }

    public void addTag(Tag tag) {
        tagDao.save(tag);
    }

    public Tag getTag(String tag) {
        Optional<Tag> t = tagDao.findById(tag);
        if(t != null && t.isPresent())
            return t.get();
        return null;
    }

    public void deleteTag(String tag) {
        Tag t = getTag(tag);
        if(t != null)
            tagDao.delete(t);
    }


}
