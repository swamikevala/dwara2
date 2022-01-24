package org.ishafoundation.dwaraapi.resource;

import org.ishafoundation.dwaraapi.api.req.clip.ClipRequest;
import org.ishafoundation.dwaraapi.api.resp.clip.ClipArtifactResponse;
import org.ishafoundation.dwaraapi.api.resp.clip.ClipResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.MamTagDao;
import org.ishafoundation.dwaraapi.db.model.transactional.MamTag;
import org.ishafoundation.dwaraapi.db.model.transactional.TRestoreBucket;
import org.ishafoundation.dwaraapi.service.ClipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
public class ClipController {
    @Autowired
    ClipService clipService;
    @Autowired
    MamTagDao mamTagDao;

    @PostMapping("clip/getSearchedClip")
    public ResponseEntity<List<ClipArtifactResponse>> getSearchedClip(@RequestBody ClipRequest clipRequest){
        List<ClipArtifactResponse> clipResponseList = clipService.searchClips(clipRequest);
        return ResponseEntity.status(HttpStatus.OK).body(clipResponseList);

    }

    @GetMapping("clip/Tags")
    public ResponseEntity<List<String>> getAllTags(){
        List<MamTag> mamTags = (List<MamTag>) mamTagDao.findAll();
        List<String> tags = new ArrayList<>();
        for (MamTag mamTag: mamTags) {
            tags.add(mamTag.getName());

        }
    return ResponseEntity.status(HttpStatus.OK).body(tags);
    }

}
