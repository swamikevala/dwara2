package org.ishafoundation.dwaraapi.service;

import java.util.*;

import org.ishafoundation.dwaraapi.api.req.clip.ClipRequest;
import org.ishafoundation.dwaraapi.api.resp.clip.ClipArtifactResponse;
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

    public List<ClipArtifactResponse> getEvents(ClipRequest clipRequest){
        List<ClipArtifactResponse> clipArtifactResponses = new ArrayList<>();
        //Set<File> file1Set =new TreeSet<>();
        Set<File> file1s = new HashSet<>();
        for(String keyword : clipRequest.getKeyWords()){
           List<File> files  = file1Dao.findByPathnameContains(keyword);
            file1s.addAll(files);
        }
        if(clipRequest.getOperator().equals("AND")){
            for (File file: file1s) {
                int length=0;
                for (String word: clipRequest.getKeyWords()
                     ) {
                    if(file.getPathname().contains(word))
                        length+=1;


                }
                if(!(length==clipRequest.getKeyWords().size()))
                    file1s.remove(file);
            }

        }


        Map<String , List<File>> artifactFiles = new HashMap<>();
        for(File file : file1s){
            if(artifactFiles.containsKey(file.getArtifact().getName())) {
                List<File> files = artifactFiles.get(file.getArtifact().getName());
                files.add(file);
                artifactFiles.replace(file.getArtifact().getName(),files);
            }

            else {
                List<File> files = new ArrayList<>();
                files.add(file);
                artifactFiles.put(file.getArtifact().getName(),files);
            }

        }

        for (String artifact: artifactFiles.keySet()) {
            ClipArtifactResponse clipArtifactResponse = new ClipArtifactResponse();
            clipArtifactResponse.setName(artifact);
            List<File> files = artifactFiles.get(artifact);
            Set<Integer> fileIDs = new HashSet<>();
            for (File file : files) {
                fileIDs.add(file.getId());
            }
            List<ClipResponse> clipResponseList = new ArrayList<>();
            List<Clip> clipList = clipDao.findAllByIdIn(fileIDs);
            for (Clip clip : clipList) {
                ClipResponse clipResponse = new ClipResponse();
                clipResponse.setClipId(clip.getId());
                clipResponse.setClipName(clip.getName());
                File file1 = file1Dao.findById(clip.getFile_id()).get();
                String appendUrlTOProxy = "";
                if (file1.getArtifact().getArtifactclass().getId().contains("-priv")) {
                    appendUrlTOProxy = "http://172.18.1.24/mam/private/";
                } else
                    appendUrlTOProxy = "http://172.18.1.24/mam/public/";
                clipResponse.setProxyPath(appendUrlTOProxy + file1.getPathname());
                //clipResponse.setArtifactName(file1.getArtifact().getName());
                List<ClipTag> clipTagList = clipTagDao.findAllByClipId(clip.getId());
                List<Integer> clipTagIds = new ArrayList<>();
                for (ClipTag clipTag : clipTagList) {
                    clipTagIds.add(clipTag.getTagId());
                }
                List<MamTag> mamTagList = mamTagDao.findByIdIn(clipTagIds);
                List<String> tags = new ArrayList<>();
                for (MamTag mamTag : mamTagList) {
                    tags.add(mamTag.getName());
                }
                clipResponse.setTagList(tags);
                clipResponseList.add(clipResponse);

            }
            clipArtifactResponse.setClipResponseList(clipResponseList);
            clipArtifactResponses.add(clipArtifactResponse);
        }

       return  clipArtifactResponses;

    }

    public List<ClipArtifactResponse> getTags(ClipRequest clipRequest){
        List<ClipArtifactResponse> clipArtifactResponses = new ArrayList<>();
        List<MamTag> mamTags = mamTagDao.findByNameIn(clipRequest.getKeyWords());
        List<Clip> clips =new ArrayList<>();
        List<Integer> tagIds =new ArrayList<>();
        for(MamTag mamTag:mamTags){
            tagIds.add(mamTag.getId());

        }
        List<ClipTag> clipTags =clipTagDao.findByTagIdIn(tagIds);
        Set<Integer> clipIds = new TreeSet<>();

        for (ClipTag clipTag: clipTags) {
            if (clipRequest.getOperator().equals("AND")) {
                    if(clipTagDao.findAllByClipId(clipTag.getClipId()).containsAll(tagIds))
                        clipIds.add(clipTag.getTagId());
            }
            else
            clipIds.add(clipTag.getClipId());
        }


        clips=clipDao.findAllByIdIn(clipIds);

        Map<String , List<ClipResponse>> artifactClips = new HashMap<>();
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
            // clipResponse.setArtifactName(file1.getArtifact().getName())
            // ;
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
            if(artifactClips.containsKey(file1.getArtifact().getName())) {
                List<ClipResponse> clipResponseList = artifactClips.get(file1.getArtifact().getName());
                clipResponseList.add(clipResponse);
                artifactClips.replace(file1.getArtifact().getName(),clipResponseList);
            }

            else {
                List<ClipResponse> clipResponseList = new ArrayList<>();
                clipResponseList.add(clipResponse);
                artifactClips.put(file1.getArtifact().getName(),clipResponseList);
            }
            //clipResponseList.add(clipResponse);
        }
        for (String artifact: artifactClips.keySet()) {
            ClipArtifactResponse clipArtifactResponse = new ClipArtifactResponse();
            clipArtifactResponse.setName(artifact);
            clipArtifactResponse.setClipResponseList(artifactClips.get(artifact));
            clipArtifactResponses.add(clipArtifactResponse);
        }

        return clipArtifactResponses;

    }

    public List<ClipArtifactResponse> searchClips(ClipRequest clipRequest){
        List<ClipArtifactResponse> clipArtifactResponses = new ArrayList<>();
        if(clipRequest.getType().equals("Events"))
            return getEvents(clipRequest);
        else if( clipRequest.getType().equals("Tags"))
            return getEvents(clipRequest);
        clipArtifactResponses.addAll(getEvents(clipRequest));
        clipArtifactResponses.addAll(getTags(clipRequest));
        return clipArtifactResponses;

    }}




