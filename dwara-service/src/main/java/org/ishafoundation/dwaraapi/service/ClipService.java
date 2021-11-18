package org.ishafoundation.dwaraapi.service;

import org.ishafoundation.dwaraapi.api.req.clip.ClipRequest;
import org.ishafoundation.dwaraapi.api.resp.clip.ClipResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.ClipDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ClipTagDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.MamTagDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.Artifact1Dao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.File1Dao;
import org.ishafoundation.dwaraapi.db.model.transactional.Clip;
import org.ishafoundation.dwaraapi.db.model.transactional.ClipTag;
import org.ishafoundation.dwaraapi.db.model.transactional.MamTag;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClipService extends DwaraService{
    @Autowired
    ClipDao clipDao;
    @Autowired
    MamTagDao mamTagDao;
    @Autowired
    ClipTagDao clipTagDao;
    @Autowired
    File1Dao file1Dao;
    @Autowired
    Artifact1Dao artifact1Dao;

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
                File1 file1 = file1Dao.findById(clip.getFile_id()).get();
                String appendUrlTOProxy = "";
                if(file1.getArtifact1().getArtifactclass().getId().contains("-priv")){
                    appendUrlTOProxy="http://172.18.1.24/mam/private/";
                }
                else
                    appendUrlTOProxy="http://172.18.1.24/mam/public/";
                clipResponse.setProxyPath(appendUrlTOProxy+file1.getPathname());
                clipResponse.setArtifactName(file1.getArtifact1().getName());
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
            Set<File1> file1Set =new TreeSet<>();
            for(String keyword : clipRequest.getKeyWords()){
                List<File1> file1s = file1Dao.findByPathnameContains(keyword);
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
                File1 file1 = file1Dao.findById(clip.getFile_id()).get();
                String appendUrlTOProxy = "";
                if(file1.getArtifact1().getArtifactclass().getId().contains("-priv")){
                    appendUrlTOProxy="http://172.18.1.24/mam/private/";
                }
                else
                    appendUrlTOProxy="http://172.18.1.24/mam/public/";
                clipResponse.setProxyPath(appendUrlTOProxy+file1.getPathname());
                clipResponse.setArtifactName(file1.getArtifact1().getName());
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




