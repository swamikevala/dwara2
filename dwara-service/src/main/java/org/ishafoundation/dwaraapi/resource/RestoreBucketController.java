package org.ishafoundation.dwaraapi.resource;

import org.ishafoundation.dwaraapi.db.dao.transactional.TRestoreBucketDao;
import org.ishafoundation.dwaraapi.db.model.transactional.RestoreBucketFile;
import org.ishafoundation.dwaraapi.db.model.transactional.TRestoreBucket;
import org.ishafoundation.dwaraapi.service.RestoreBucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
public class RestoreBucketController {
    @Autowired
    RestoreBucketService restoreBucketService;
    @Autowired
    TRestoreBucketDao tRestoreBucketDao;
    @GetMapping("/buckets")
    public ResponseEntity<List<TRestoreBucket>> getAllBuckets(){
        return ResponseEntity.status(HttpStatus.OK).body((List<TRestoreBucket>) tRestoreBucketDao.findAll());
    }

    @PostMapping("/buckets")
    public ResponseEntity<TRestoreBucket> createBucket(@RequestBody Map<String,String> map){
        TRestoreBucket tRestoreBucket =restoreBucketService.createBucket((String)map.get("id"), map.get("createdBy"));
        return ResponseEntity.status(HttpStatus.OK).body(tRestoreBucket);
    }

    @DeleteMapping("/buckets/{id}")
    public ResponseEntity deleteBucket(@PathVariable String id){
        restoreBucketService.deleteBucket(id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/buckets/{id}")
    public ResponseEntity<TRestoreBucket> updateBucket(@PathVariable String id ,@RequestParam String updateParam ,@RequestBody List<Integer> fileIds){
        boolean create =true;
        if(updateParam.equals("delete"))
            create =false;
        else if (updateParam.equals("add"))
            create=true;
        else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        TRestoreBucket tRestoreBucket= restoreBucketService.updateBucket(fileIds,id,create);
        return ResponseEntity.status(HttpStatus.OK).body(tRestoreBucket);
    }
    
    @PutMapping("/buckets/proxyPaths/{id}")
    public ResponseEntity<TRestoreBucket> updateFiles(@PathVariable String id ,@RequestBody List<String> proxyPaths){
        TRestoreBucket tRestoreBucket= restoreBucketService.getFileList(id,proxyPaths);
        return ResponseEntity.status(HttpStatus.OK).body(tRestoreBucket);
    }
}
