package org.ishafoundation.dwaraapi.service;

import org.ishafoundation.dwaraapi.db.dao.transactional.TRestoreBucketDao;
import org.ishafoundation.dwaraapi.db.model.transactional.RestoreBucketFile;
import org.ishafoundation.dwaraapi.db.model.transactional.TRestoreBucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class RestoreBucketService {
    @Autowired
    TRestoreBucketDao tRestoreBucketDao;

    public TRestoreBucket createBucket(String id , String createdBy){
        TRestoreBucket tRestoreBucket = new TRestoreBucket( id,createdBy , new Date());
        tRestoreBucketDao.save(tRestoreBucket);
        return tRestoreBucket;
    }
    public void deleteBucket(String id){
    tRestoreBucketDao.deleteById(id);
    }

    public TRestoreBucket updateBucket(List<RestoreBucketFile> restoreBucketFiles , String id , boolean create){

        Optional<TRestoreBucket> result = tRestoreBucketDao.findById(id);
        TRestoreBucket tRestoreBucketFromDb = result.get();

        if(create)
            tRestoreBucketFromDb.setDetails(restoreBucketFiles);
        else
            tRestoreBucketFromDb.setDetails(null);
        return tRestoreBucketFromDb;

}
    public TRestoreBucket getFileList(String id , List<String> proxyPaths){
        TRestoreBucket tRestoreBucketFromDb = tRestoreBucketDao.findById(id).get();
        List<RestoreBucketFile> presentFiles =tRestoreBucketFromDb.getDetails();
        List<Integer> presentIds = new ArrayList<>();
        for (RestoreBucketFile file : presentFiles) {
                    presentIds.add(file.getFileID());
        }
        List<File1> proxyFiles= file1Dao.findByPathNameIn(proxyPaths);
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

}
