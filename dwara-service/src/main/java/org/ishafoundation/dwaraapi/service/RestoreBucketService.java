package org.ishafoundation.dwaraapi.service;

import org.ishafoundation.dwaraapi.db.dao.transactional.TRestoreBucketDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.Artifact1Dao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.File1Dao;
import org.ishafoundation.dwaraapi.db.model.transactional.RestoreBucketFile;
import org.ishafoundation.dwaraapi.db.model.transactional.TRestoreBucket;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact1;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RestoreBucketService {
    private static final Logger logger = LoggerFactory.getLogger(RestoreBucketService.class);
    @Autowired
    TRestoreBucketDao tRestoreBucketDao;
    @Autowired
    File1Dao file1Dao;
    @Autowired
    Artifact1Dao artifact1Dao;

    public TRestoreBucket createBucket(String id , String createdBy){
        TRestoreBucket tRestoreBucket = new TRestoreBucket( id,createdBy , new Date());
        tRestoreBucketDao.save(tRestoreBucket);
        return tRestoreBucket;
    }
    public void deleteBucket(String id){
    tRestoreBucketDao.deleteById(id);
    }

    public TRestoreBucket updateBucket(List<Integer> fileIds  , String id , boolean add){

        Optional<TRestoreBucket> result = tRestoreBucketDao.findById(id);
        TRestoreBucket tRestoreBucketFromDb = result.get();

        if(add) {
            List<RestoreBucketFile> restoreBucketFiles = new ArrayList<>();
            for (int fileid : fileIds){
                List<RestoreBucketFile> restoreBucketFile=createFile(fileid);
                restoreBucketFiles.addAll(restoreBucketFile);

            }
            if(tRestoreBucketFromDb.getDetails()!=null)
            tRestoreBucketFromDb.addDetails(restoreBucketFiles);
            else
            tRestoreBucketFromDb.setDetails(restoreBucketFiles);
        }
        else {
            List<RestoreBucketFile> restoreBucketFiles = tRestoreBucketFromDb.getDetails();
            List<RestoreBucketFile> temp = new ArrayList<>();
            for (RestoreBucketFile file: restoreBucketFiles) {

                if(fileIds.contains(file.getFileID()))
                    temp.add(file);
            }
            restoreBucketFiles.removeAll(temp);

            tRestoreBucketFromDb.setDetails(restoreBucketFiles);
        }
        tRestoreBucketDao.save(tRestoreBucketFromDb);
        return tRestoreBucketFromDb;

}
    // only one file is added
    public TRestoreBucket getFileList(String id , List<String> proxyPaths) throws Exception {

        TRestoreBucket tRestoreBucketFromDb = tRestoreBucketDao.findById(id).get();
        List<RestoreBucketFile> presentFiles =tRestoreBucketFromDb.getDetails();
        List<Integer> presentIds = new ArrayList<>();
        for (RestoreBucketFile restoreBucketFile:presentFiles) {
            presentIds.add(restoreBucketFile.getFileID());
        }

        List<File1> proxyFiles= file1Dao.findByPathnameIn(proxyPaths);

        if(presentFiles==null){
            presentFiles =new ArrayList<>();
        }
        //List<RestoreBucketFile> ogFiles =new ArrayList<>();
        for (File1 file : proxyFiles) {

                File1 ogFile =file.getFile1Ref();
            if(presentIds.contains(ogFile.getId())){
                return null;
            }
            String appendUrlTOProxy = "";
            if(ogFile.getArtifact1().getArtifactclass().getId().contains("-priv")){
                appendUrlTOProxy="http://172.18.1.24/mam/private/";
            }
            else
                appendUrlTOProxy="http://172.18.1.24/mam/public/";

                RestoreBucketFile restoreBucketFile = new RestoreBucketFile();
                restoreBucketFile.setFileID(ogFile.getId());
                restoreBucketFile.setFileSize(String.valueOf(ogFile.getSize()));
                restoreBucketFile.setFilePathName(ogFile.getPathname());
                List< String> previewProxyPaths = new ArrayList<>();

            previewProxyPaths.add(appendUrlTOProxy+proxyPaths.get(proxyFiles.indexOf(file)));
            if(restoreBucketFile.getPreviewProxyPath()!=null)
                restoreBucketFile.addPreviewProxyPath(previewProxyPaths);
            else
            restoreBucketFile.setPreviewProxyPath(previewProxyPaths);
                restoreBucketFile.setArtifactId(ogFile.getArtifact1().getId());
                restoreBucketFile.setArtifactClass(ogFile.getArtifact1().getArtifactclass().getId());
                presentFiles.add(restoreBucketFile);
        }


        tRestoreBucketFromDb.setDetails(presentFiles);
        tRestoreBucketDao.save(tRestoreBucketFromDb);
        return tRestoreBucketFromDb;

    }
    private List<RestoreBucketFile> createFile(int id){
        File1 ogFile= file1Dao.findById(id).get();
        //Artifact artifact = artifact1Dao.findByName(ogFile.getPathname());
        List<RestoreBucketFile> restoreBucketFiles = new ArrayList<>();
        RestoreBucketFile restoreBucketFile = new RestoreBucketFile();
        restoreBucketFile.setFileID(ogFile.getId());
        restoreBucketFile.setFileSize(String.valueOf(ogFile.getSize()));
        restoreBucketFile.setFilePathName(ogFile.getPathname());
        restoreBucketFile.setArtifactId(ogFile.getArtifact1().getId());
        restoreBucketFile.setArtifactClass(ogFile.getArtifact1().getArtifactclass().getId());
        List<String> previewProxyPaths = new ArrayList<>();
        String appendUrlTOProxy = "";
        if(ogFile.getArtifact1().getArtifactclass().getId().contains("-priv")){
            appendUrlTOProxy="http://172.18.1.24/mam/private/";
        }
        else
            appendUrlTOProxy="http://172.18.1.24/mam/public/";
        //Artifact1 artifact = (Artifact1) artifact1Dao.findByName(ogFile.getPathname());
        if(artifact1Dao.existsByName(ogFile.getPathname())){
            Artifact1 artifact = (Artifact1) artifact1Dao.findByName(ogFile.getPathname());

            List<Artifact1> proxyArtifacts =  artifact1Dao.findByartifact1Ref(artifact);
            if(proxyArtifacts.size()>0){
                Artifact1  proxyArtifact = proxyArtifacts.get(0);
            List<File1> proxyVideos = file1Dao.findAllByArtifact1IdAndPathnameEndsWith(proxyArtifact.getId(), ".mp4");

            for (File1 file : proxyVideos
                ) {
                    previewProxyPaths.add(appendUrlTOProxy+file.getPathname());
                }

            }}


    else {
            List<File1> proxyFiles = file1Dao.findAllByFile1RefIdAndPathnameEndsWith(ogFile.getId(),".mp4");
            for (File1 file : proxyFiles) {
                previewProxyPaths.add(appendUrlTOProxy+file.getPathname());
            }

    }
    restoreBucketFile.setPreviewProxyPath(previewProxyPaths);


           restoreBucketFiles.add(restoreBucketFile);
        return restoreBucketFiles;

    }

    public List<TRestoreBucket> getAprrovedNull(){
        return tRestoreBucketDao.findByApprovalStatusNull();
    }



}
