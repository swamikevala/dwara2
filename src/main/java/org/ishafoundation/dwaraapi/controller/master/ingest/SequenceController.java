package org.ishafoundation.dwaraapi.controller.master.ingest;

import org.ishafoundation.dwaraapi.db.dao.master.ingest.SequenceDao;
import org.ishafoundation.dwaraapi.db.model.master.ingest.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sequence")
public class SequenceController {

    @Autowired
    private SequenceDao sequenceDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<Sequence> sequenceList){
        sequenceDao.saveAll(sequenceList);
        return sequenceList.size();
    }

    @GetMapping("/getAll")
    public List<Sequence> getAll(){
        return (List<Sequence>)sequenceDao.findAll();
    }
}
