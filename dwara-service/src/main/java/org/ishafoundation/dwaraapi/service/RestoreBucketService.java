package org.ishafoundation.dwaraapi.service;

import org.ishafoundation.dwaraapi.db.dao.transactional.TRestoreBucketDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.Artifact1Dao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.File1Dao;
import org.ishafoundation.dwaraapi.db.model.transactional.RestoreBucketFile;
import org.ishafoundation.dwaraapi.db.model.transactional.TRestoreBucket;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class RestoreBucketService {
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

    public TRestoreBucket updateBucket(List<Integer> fileIds  , String id , boolean create){

        Optional<TRestoreBucket> result = tRestoreBucketDao.findById(id);
        TRestoreBucket tRestoreBucketFromDb = result.get();

        if(create) {
            List<RestoreBucketFile> restoreBucketFiles = new ArrayList<>();
            for (int fileid : fileIds){
                List<RestoreBucketFile> restoreBucketFile=createFile(fileid);
                restoreBucketFiles.addAll(restoreBucketFile);

            }
            tRestoreBucketFromDb.setDetails(restoreBucketFiles);
        }
        else {
            List<RestoreBucketFile> restoreBucketFiles = tRestoreBucketFromDb.getDetails();
            for (RestoreBucketFile file: restoreBucketFiles
                 ) {
                if(fileIds.contains(file.getFileID()))
                    restoreBucketFiles.remove(file);
            }
            tRestoreBucketFromDb.setDetails(restoreBucketFiles);
        }
            return tRestoreBucketFromDb;

}
    public TRestoreBucket getFileList(String id , List<String> proxyPaths){
        TRestoreBucket tRestoreBucketFromDb = tRestoreBucketDao.findById(id).get();
        List<RestoreBucketFile> presentFiles =tRestoreBucketFromDb.getDetails();
        List<Integer> presentIds = new ArrayList<>();
        for (RestoreBucketFile file : presentFiles) {
                    presentIds.add(file.getFileID());
        }
        List<File1> proxyFiles= file1Dao.findByPathnameIn(proxyPaths);
        //List<RestoreBucketFile> ogFiles =new ArrayList<>();
        for (File1 file : proxyFiles) {
                if(!presentIds.contains(file.getId())){
                File ogFile =file.getFile1Ref();

                RestoreBucketFile restoreBucketFile = new RestoreBucketFile();
                restoreBucketFile.setFileID(ogFile.getId());
                restoreBucketFile.setFileSize(String.valueOf(ogFile.getSize()));
                restoreBucketFile.setFilePathName(ogFile.getPathname());
                List<String> previewProxyPaths = new ArrayList<>();
            previewProxyPaths.add(proxyPaths.get(proxyFiles.indexOf(file)));
                restoreBucketFile.setPreviewProxyPath(previewProxyPaths);
                restoreBucketFile.setArtifactId(file.getArtifact1().getId());
                restoreBucketFile.setArtifactClass(file.getArtifact1().getArtifactclass().getId());
                presentFiles.add(restoreBucketFile);
        }}
        tRestoreBucketFromDb.setDetails(presentFiles);
        tRestoreBucketDao.save(tRestoreBucketFromDb);
        return tRestoreBucketFromDb;

    }
    private List<RestoreBucketFile> createFile(int id){
        File1 ogFile= file1Dao.findById(id).get();
        //Artifact artifact = artifact1Dao.findByName(ogFile.getPathname());
        List<RestoreBucketFile> restoreBucketFiles = new ArrayList<>();
        if(artifact1Dao.existsByPathname(ogFile.getPathname())){
            Artifact artifact = artifact1Dao.findByName(ogFile.getPathname());
            Artifact proxyArtifact = artifact1Dao.findByartifact1Ref(ogFile.getId());
            List<File1> proxyVideos = file1Dao.findAllByArtifact1IdAndPathnameEndsWith(proxyArtifact.getId(), ".mp4");
            for (File1 file1 : proxyVideos) {
                RestoreBucketFile restoreBucketFile = new RestoreBucketFile();
                restoreBucketFile.setFileID(file1.getId());
                restoreBucketFile.setFileSize(String.valueOf(file1.getSize()));
                restoreBucketFile.setFilePathName(file1.getPathname());
                List<String> previewProxyPaths = new ArrayList<>();
                List<File1> proxyFiles = file1Dao.findAllByFile1RefIdAndPathnameEndsWith(file1.getId(),".mp4");
                for (File1 file : proxyFiles
                ) {
                    previewProxyPaths.add(file.getPathname());
                }
                restoreBucketFile.setPreviewProxyPath(previewProxyPaths);
                restoreBucketFile.setArtifactId(file1.getArtifact1().getId());
                restoreBucketFile.setArtifactClass(file1.getArtifact1().getArtifactclass().getId());
            restoreBucketFiles.add(restoreBucketFile);
            }

        }
    else {
            File1 file1 = file1Dao.findAllByFile1RefId(id).get(0);
            RestoreBucketFile restoreBucketFile = new RestoreBucketFile();
            restoreBucketFile.setFileID(file1.getId());
            restoreBucketFile.setFileSize(String.valueOf(file1.getSize()));
            restoreBucketFile.setFilePathName(file1.getPathname());
            List<String> previewProxyPaths = new ArrayList<>();
            List<File1> proxyFiles = file1Dao.findAllByFile1RefId(file1.getId());
            for (File1 file : proxyFiles
            ) {
                previewProxyPaths.add(file.getPathname());
            }
            restoreBucketFile.setPreviewProxyPath(previewProxyPaths);
            restoreBucketFile.setArtifactId(file1.getArtifact1().getId());
            restoreBucketFile.setArtifactClass(file1.getArtifact1().getArtifactclass().getId());
            restoreBucketFiles.add(restoreBucketFile);
    }
        return restoreBucketFiles;

    }

}
