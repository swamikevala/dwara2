package org.ishafoundation.dwaraapi.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.ishafoundation.dwaraapi.api.req.clip.ClipRequest;
import org.ishafoundation.dwaraapi.api.resp.clip.ClipResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ClipDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ClipTagDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.MamTagDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Clip;
import org.ishafoundation.dwaraapi.db.model.transactional.ClipTag;
import org.ishafoundation.dwaraapi.db.model.transactional.File;
import org.ishafoundation.dwaraapi.db.model.transactional.MamTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClipService extends DwaraService{
    @Autowired
    ClipDao clipDao;
    @Autowired
    MamTagDao mamTagDao;
    @Autowired
    ClipTagDao clipTagDao;
    @Autowired
    FileDao file1Dao;
    @Autowired
    ArtifactDao artifact1Dao;

    public List<ClipResponse> searchClips(ClipRequest clipRequest){
        List<ClipResponse> clipResponseList =new ArrayList<>();
        if(!clipRequest.getType().equals("events")){
            List<MamTag> mamTags = mamTagDao.findByNameIn(clipRequest.getKeyWords());
            List<Clip> clips =new ArrayList<>();
            List<Integer> tagIds =new ArrayList<>();
            for(MamTag mamTag:mamTags){
                tagIds.add(mamTag.getId());

            }
            List<ClipTag> clipTags =clipTagDao.findByTagIdIn(tagIds);
            Set<Integer> clipIds = new TreeSet<>();

            for (ClipTag clipTag: clipTags) {
                clipIds.add(clipTag.getClipId());

            }
            clips=clipDao.findAllByIdIn(clipIds);


            for (Clip clip: clips) {
                ClipResponse clipResponse =new ClipResponse();
                clipResponse.setClipId(clip.getId());
                clipResponse.setClipName(clip.getName());
                File file1 = file1Dao.findById(clip.getFile_id()).get();
                String appendUrlTOProxy = "";
                if(file1.getArtifact().getArtifactclass().getId().contains("-priv")){
                    appendUrlTOProxy="http://172.18.1.24/mam/private/";
                }
                else
                    appendUrlTOProxy="http://172.18.1.24/mam/public/";
                clipResponse.setProxyPath(appendUrlTOProxy+file1.getPathname());
                clipResponse.setArtifactName(file1.getArtifact().getName());
                List<ClipTag> clipTagList = clipTagDao.findAllByClipId(clip.getId());
                List<Integer> clipTagIds =new ArrayList<>();
                for(ClipTag  clipTag : clipTagList){
                    clipTagIds.add(clipTag.getTagId());
                }
                List<MamTag> mamTagList = mamTagDao.findByIdIn(clipTagIds);
                List<String> tags =new ArrayList<>();
                for (MamTag mamTag: mamTagList) {
                    tags.add(mamTag.getName());
                }
            clipResponse.setTagList(tags);
             clipResponseList.add(clipResponse);
            }
        return clipResponseList;
        }
        else{
            Set<File> file1Set =new TreeSet<>();
            for(String keyword : clipRequest.getKeyWords()){
                List<File> file1s = file1Dao.findByPathnameContains(keyword);
                file1Set.addAll(file1s);
            }
            Set<Integer> fileIDs =new HashSet<>();
            for(File file : file1Set){
                fileIDs.add(file.getId());
            }
        List<Clip> clipList = clipDao.findAllByIdIn(fileIDs);
            for (Clip clip: clipList) {
                ClipResponse clipResponse =new ClipResponse();
                clipResponse.setClipId(clip.getId());
                clipResponse.setClipName(clip.getName());
                File file1 = file1Dao.findById(clip.getFile_id()).get();
                String appendUrlTOProxy = "";
                if(file1.getArtifact().getArtifactclass().getId().contains("-priv")){
                    appendUrlTOProxy="http://172.18.1.24/mam/private/";
                }
                else
                    appendUrlTOProxy="http://172.18.1.24/mam/public/";
                clipResponse.setProxyPath(appendUrlTOProxy+file1.getPathname());
                clipResponse.setArtifactName(file1.getArtifact().getName());
                List<ClipTag> clipTagList = clipTagDao.findAllByClipId(clip.getId());
                List<Integer> clipTagIds =new ArrayList<>();
                for(ClipTag  clipTag : clipTagList){
                    clipTagIds.add(clipTag.getTagId());
                }
                List<MamTag> mamTagList = mamTagDao.findByIdIn(clipTagIds);
                List<String> tags =new ArrayList<>();
                for (MamTag mamTag: mamTagList) {
                    tags.add(mamTag.getName());
                }
                clipResponse.setTagList(tags);
                clipResponseList.add(clipResponse);
            }
            return clipResponseList;
            }
        }

    }




