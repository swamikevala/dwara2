package org.ishafoundation.dwaraapi.controller.master.ingest;

import org.ishafoundation.dwaraapi.db.dao.master.ingest.ScanfolderDao;
import org.ishafoundation.dwaraapi.db.model.master.ingest.Scanfolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scanfolder")
public class ScanfolderController {

    @Autowired
    private ScanfolderDao scanfolderDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<Scanfolder> scanfolderList){
        scanfolderDao.saveAll(scanfolderList);
        return scanfolderList.size();
    }

    @GetMapping("/getAll")
    public List<Scanfolder> getAll(){
        return (List<Scanfolder>)scanfolderDao.findAll();
    }
}
