package org.ishafoundation.dwaraapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.ishafoundation.dwaraapi.db.dao.master.TagDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.Artifact1Dao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Tag;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagService extends DwaraService{
    @Autowired
    private TagDao tagDao;

    public void tagArtifact(String tag, int artifactId) {
        Tag t = getTag(tag);
        if(t == null) {
            t = new Tag(tag);
        }

        Artifact1 r = t.getArtifactById(artifactId);
        if(r == null) {
            r = new Artifact1(artifactId);
            t.addArtifact(r);
        }

        //save
        addTag(t);
    }

    public void deleteTagArtifact(String tag, int artifactId) {
        Tag t = getTag(tag);
        if(t == null) {
            t = new Tag(tag);
        }
        Artifact1 r = t.getArtifactById(artifactId);
        if(r != null) {
            t.deleteArtifact(r);
        }
        //save or add tag
        addTag(t);
    }

    public List<Artifact1> getAllArtifactsByTag(String tag) {
        Tag t = tagDao.findById(tag).get();
        return new ArrayList<Artifact1>(t.getArtifacts());
    }

    public List<String> getAllTagsByArtifactId(int artifactId) {
        List<Tag> tags = new ArrayList<Tag>();
        tagDao.findByArtifacts_Id(artifactId).forEach(tags::add);

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
