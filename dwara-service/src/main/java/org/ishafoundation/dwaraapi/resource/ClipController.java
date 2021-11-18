package org.ishafoundation.dwaraapi.resource;

import org.ishafoundation.dwaraapi.api.req.clip.ClipRequest;
import org.ishafoundation.dwaraapi.api.resp.clip.ClipResponse;
import org.ishafoundation.dwaraapi.db.model.transactional.TRestoreBucket;
import org.ishafoundation.dwaraapi.service.ClipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
public class ClipController {
    @Autowired
    ClipService clipService;

    @PostMapping("clip/getSearchedClip")
    public ResponseEntity<List<ClipResponse>> getSearchedClip(@RequestBody ClipRequest clipRequest){
        List<ClipResponse> clipResponseList = clipService.searchClips(clipRequest);
        return ResponseEntity.status(HttpStatus.OK).body(clipResponseList);

    }
}
