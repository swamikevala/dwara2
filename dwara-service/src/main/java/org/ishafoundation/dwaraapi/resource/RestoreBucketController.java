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

@RestController
public class RestoreBucketController {
    @Autowired
    RestoreBucketService restoreBucketService;

    @PostMapping("/buckets")
    public ResponseEntity<TRestoreBucket> createBucket(@RequestBody Map<String,String> map){
        TRestoreBucket tRestoreBucket =restoreBucketService.createBucket((String)map.get("id"), map.get("createdBy"));
        return ResponseEntity.status(HttpStatus.OK).body(tRestoreBucket);
    }

    @DeleteMapping("/buckets/{id}")
    public void deleteBucket(@PathVariable String id){
        restoreBucketService.deleteBucket(id);
    }
    @PutMapping("/buckets/{id}")
    public ResponseEntity<TRestoreBucket> updateBucket(@PathVariable String id ,@RequestParam String updateParam ,@RequestBody List<RestoreBucketFile> files){
        boolean create = updateParam.equals("add");
        TRestoreBucket tRestoreBucket= restoreBucketService.updateBucket(files,id,create);
        return ResponseEntity.status(HttpStatus.OK).body(tRestoreBucket);
    }
    
    @PutMapping("/buckets/proxyPaths/{id}")
    public ResponseEntity<TRestoreBucket> updateFiles(@PathVariable String id ,@RequestBody List<String> proxyPaths){
        TRestoreBucket tRestoreBucket= restoreBucketService.getFileList(id,proxyPaths);
        return ResponseEntity.status(HttpStatus.OK).body(tRestoreBucket);
    }
}
