package org.ishafoundation.dwaraapi.service;

import org.ishafoundation.dwaraapi.api.req.clip.ClipListRequest;
import org.ishafoundation.dwaraapi.api.resp.clip.ClipArtifactResponse;
import org.ishafoundation.dwaraapi.api.resp.clip.ClipListResponse;
import org.ishafoundation.dwaraapi.api.resp.clip.ClipResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.*;
import org.ishafoundation.dwaraapi.db.model.transactional.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ClipListService  extends  DwaraService{
    @Autowired
    ClipListDao clipListDao;
    @Autowired
    ClipClipListDao clipClipListDao;
    @Autowired
    ClipDao clipDao;
    @Autowired
    FileDao file1Dao;
    @Autowired
    ClipTagDao clipTagDao;
    @Autowired
    MamTagDao mamTagDao;
    @Value("${catdv.host}")
    private String catdvHost;

    public List<ClipListResponse> getAllClipList(){
        List<ClipList> clipLists = (List<ClipList>) clipListDao.findAll();
        List<ClipListResponse> clipListResponseList = new ArrayList<>();
        for (ClipList clipList:clipLists) {
            ClipListResponse clipListResponse = new ClipListResponse();
            List<ClipArtifactResponse> clipArtifactResponses = new ArrayList<>();
            clipListResponse.setName(clipList.getName());
            clipListResponse.setCreatedBy(clipList.getCreatedby());
            clipListResponse.setCreatedOn(clipList.getCreatedOn().toString());
            List<ClipClipList> clipClipLists= clipClipListDao.findAllByCliplistId(clipList.getId());
            Set<Integer> clipIds = new HashSet<>();
            for (ClipClipList clipClip: clipClipLists) {
                clipIds.add(clipClip.getClipId());

            }
            List<Clip> clips=clipDao.findAllByIdIn(clipIds);
            System.out.println(clipIds);
            Map<String , List<ClipResponse>> artifactClips = new HashMap<>();
            for (Clip clip: clips) {
                ClipResponse clipResponse =new ClipResponse();
                clipResponse.setClipId(clip.getId());
                clipResponse.setClipName(clip.getName());
                System.out.println(clip.getFile_id());
                if(clip.getFile_id()!=null && clip.getFile_id()!=0){
                    File file1 = file1Dao.findById(clip.getFile_id()).get();
                    String appendUrlTOProxy = "";
                    if(file1.getArtifact().getArtifactclass().getId().contains("-priv")){
                        appendUrlTOProxy="http://" + catdvHost + "/mam/private/";
                    }
                    else
                        appendUrlTOProxy="http://" + catdvHost + "/mam/public/";
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
                }}
            for (String artifact: artifactClips.keySet()) {
                ClipArtifactResponse clipArtifactResponse = new ClipArtifactResponse();
                clipArtifactResponse.setName(artifact);
                clipArtifactResponse.setClipResponseList(artifactClips.get(artifact));
                clipArtifactResponses.add(clipArtifactResponse);
            }
            clipListResponse.setClipArtifactResponseResponseList(clipArtifactResponses);
            clipListResponseList.add(clipListResponse);
        }
            return clipListResponseList;
    }

    public void  createList(ClipListRequest clipListRequest){
        ClipList clipList = new ClipList();
        clipList.setName(clipListRequest.getName());
        clipList.setCreatedby(getUserObjFromContext().getId());
        clipList.setCreatedOn(LocalDateTime.now());
        clipListDao.save(clipList);
       ClipList clipListFromDb=clipListDao.findById(clipListRequest.getId()).get();

        for ( int clipId:clipListRequest.getClipIds()) {
            ClipClipList clipClipList = new ClipClipList();
            clipClipList.setCliplistId(clipListFromDb.getId());
            clipClipList.setClipId(clipId);
            clipClipListDao.save(clipClipList);
        }

    }
    public void deleteList(int clipListId){
      /*ClipList clipList = clipListDao.findById(clipListId).get();
      clipListDao.delete(clipList);*/
       clipListDao.deleteById(clipListId);
       clipClipListDao.deleteByCliplistId(clipListId);
    }

    public void addClips(int clipListId , int clipId){
        ClipClipList clipClipList = new ClipClipList();

        clipClipList.setClipId(clipId);
        clipClipList.setCliplistId(clipListId);
        clipClipListDao.save(clipClipList);

    }

    public void deleteClips(int clipListId , int clipId){
        ClipClipList clipClipList = clipClipListDao.findByCliplistIdAndClipId(clipListId,clipId);
        clipClipListDao.delete(clipClipList);
    }
}


