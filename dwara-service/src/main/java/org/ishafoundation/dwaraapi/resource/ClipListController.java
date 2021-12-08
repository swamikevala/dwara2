package org.ishafoundation.dwaraapi.resource;

import org.ishafoundation.dwaraapi.api.req.clip.ClipListRequest;
import org.ishafoundation.dwaraapi.api.resp.clip.ClipListResponse;
import org.ishafoundation.dwaraapi.service.ClipListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
public class ClipListController {

    @Autowired
    ClipListService clipListService;

    @GetMapping("clipList/getList")
    public ResponseEntity<List<ClipListResponse>> getClipList(){
        List<ClipListResponse> clipListResponses = clipListService.getAllClipList();
        return ResponseEntity.status(HttpStatus.OK).body(clipListResponses);
    }
    @PostMapping("clipList/createList")
    public ResponseEntity<String> createClipList(@RequestBody ClipListRequest clipListRequest){
        clipListService.createList(clipListRequest);
        return ResponseEntity.status(HttpStatus.OK).body("DONE");
    }
    @PutMapping("clipList/addClip")
    public ResponseEntity<String> addClipList(@RequestParam int clipListId, @RequestParam int clipId){
        clipListService.addClips(clipListId,clipId);
        return ResponseEntity.status(HttpStatus.OK).body("DONE");
    }
    @PutMapping("clipList/deleteClip")
    public ResponseEntity<String> deleteclipList(@RequestParam int clipListId, @RequestParam int clipId){
        clipListService.deleteClips(clipListId,clipId);
        return ResponseEntity.status(HttpStatus.OK).body("DONE");
    }
    @DeleteMapping("clipList/deleteClipList")
    public ResponseEntity<String> deleteList(@RequestParam int clipListId){
        clipListService.deleteList(clipListId);
        return ResponseEntity.status(HttpStatus.OK).body("Done");
    }
}
