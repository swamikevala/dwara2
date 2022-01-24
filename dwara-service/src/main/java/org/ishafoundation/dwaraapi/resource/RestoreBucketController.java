package org.ishafoundation.dwaraapi.resource;

import org.ishafoundation.dwaraapi.api.req.restore.RestoreUserRequest;
import org.ishafoundation.dwaraapi.api.resp.restore.RestoreBucketResponse;
import org.ishafoundation.dwaraapi.api.resp.restore.RestoreResponse;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TRestoreBucketDao;
import org.ishafoundation.dwaraapi.db.model.transactional.RestoreBucketFile;
import org.ishafoundation.dwaraapi.db.model.transactional.TRestoreBucket;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.EmailerService;
import org.ishafoundation.dwaraapi.service.FileService;
import org.ishafoundation.dwaraapi.service.RestoreBucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.util.*;

@CrossOrigin
@RestController
public class RestoreBucketController {

    @Autowired
    RestoreBucketService restoreBucketService;
    @Autowired
    TRestoreBucketDao tRestoreBucketDao;
    @Autowired
    FileService fileService;
    @Autowired
    UserDao userDao;



    @GetMapping("/buckets")
    public ResponseEntity<List<RestoreBucketResponse>> getAllBuckets(){
        List<TRestoreBucket> tRestoreBucketsFromDb = (List<TRestoreBucket>)tRestoreBucketDao.findAll();
        List<RestoreBucketResponse> restoreBucketResponses =new ArrayList<>();
        for (TRestoreBucket tRestoreBucket:tRestoreBucketsFromDb) {
            RestoreBucketResponse restoreBucketResponse = new RestoreBucketResponse(tRestoreBucket);
            restoreBucketResponse.setRequestedBeforeTimeNumber( System.currentTimeMillis()-restoreBucketResponse.getCreatedAt().toEpochSecond(ZoneOffset.of("+05:30")));
            restoreBucketResponse.setRequestedBeforeTime(restoreBucketService.getElapsedTime(restoreBucketResponse.getCreatedAt()));
            if(restoreBucketResponse.getCreatedBy()!=null)
                restoreBucketResponse.setCreatorName(userDao.findById(restoreBucketResponse.getCreatedBy()).get().getName());
            restoreBucketResponses.add(restoreBucketResponse);

        }
        Collections.sort(restoreBucketResponses);
        return ResponseEntity.status(HttpStatus.OK).body(restoreBucketResponses);
    }

    @PostMapping("/buckets")
    public ResponseEntity<TRestoreBucket> createBucket(@RequestBody Map<String,String> map){
        TRestoreBucket tRestoreBucket =restoreBucketService.createBucket((String)map.get("id"));
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
    public ResponseEntity<TRestoreBucket> updateFiles(@PathVariable String id ,@RequestBody List<String> proxyPaths) throws Exception {
        TRestoreBucket tRestoreBucket= restoreBucketService.getFileList(id,proxyPaths);
        return ResponseEntity.status(HttpStatus.OK).body(tRestoreBucket);
    }

    @PostMapping("/restore/bucket/{id}")
    public ResponseEntity<RestoreResponse> restoreBucket(@PathVariable String id, @RequestBody RestoreUserRequest restoreUserRequest){
        RestoreResponse restoreResponse = null;
        try {
            restoreResponse = fileService.restore(restoreUserRequest, Action.restore_process, restoreUserRequest.getFlow());
            if (!Objects.isNull(restoreResponse) && restoreResponse.getFiles().size() == restoreUserRequest.getFileIds().size()) {
                restoreBucketService.deleteBucket(id);
            }
        }catch (Exception e) {
            String errorMsg = "Unable to restore - " + e.getMessage();

            if(e instanceof DwaraException)
                throw (DwaraException) e;
            else
                throw new DwaraException(errorMsg, null);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(restoreResponse);
    }


    @GetMapping("/buckets/open")
    public ResponseEntity<List<RestoreBucketResponse>> getOpenBuckets(){
        List<TRestoreBucket> tRestoreBuckets = restoreBucketService.getAprrovedNull();
        List<RestoreBucketResponse> restoreBucketResponses =new ArrayList<>();
        for (TRestoreBucket tRestoreBucket:tRestoreBuckets) {
            RestoreBucketResponse restoreBucketResponse = new RestoreBucketResponse(tRestoreBucket);
            restoreBucketResponse.setRequestedBeforeTimeNumber( System.currentTimeMillis()/1000-restoreBucketResponse.getCreatedAt().toEpochSecond(ZoneOffset.of("+05:30")));
            restoreBucketResponse.setRequestedBeforeTime(restoreBucketService.getElapsedTime(restoreBucketResponse.getCreatedAt()));
            if(restoreBucketResponse.getCreatedBy()!=null)
                restoreBucketResponse.setCreatorName(userDao.findById(restoreBucketResponse.getCreatedBy()).get().getName());
            restoreBucketResponses.add(restoreBucketResponse);

        }
        Collections.sort(restoreBucketResponses);
        return ResponseEntity.status(HttpStatus.OK).body(restoreBucketResponses);
    }

    @CrossOrigin
    @PutMapping("/bucket/approval")
    public ResponseEntity<TRestoreBucket> getApproval(@RequestBody TRestoreBucket tRestoreBucket){
        TRestoreBucket tRestoreBucketFromDb = tRestoreBucketDao.findById(tRestoreBucket.getId()).get();
        tRestoreBucketFromDb.setApproverEmail(tRestoreBucket.getApproverEmail());
        tRestoreBucketFromDb.setDestinationPath(tRestoreBucket.getDestinationPath());
        tRestoreBucketFromDb.setApprover(tRestoreBucket.getApprover());
        tRestoreBucketFromDb.setPriority(tRestoreBucket.getPriority());
        tRestoreBucketFromDb.setApprovalStatus(tRestoreBucket.getApprovalStatus());
        tRestoreBucketFromDb.setRequestedBy(restoreBucketService.getUserObjFromContext().getId());
        tRestoreBucketDao.save(tRestoreBucketFromDb);
        restoreBucketService.sendMail(tRestoreBucketFromDb);

        return ResponseEntity.status(HttpStatus.OK).body(new TRestoreBucket());
    }

}
