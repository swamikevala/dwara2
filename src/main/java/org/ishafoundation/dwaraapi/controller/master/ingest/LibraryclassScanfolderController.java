package org.ishafoundation.dwaraapi.controller.master.ingest;

import org.ishafoundation.dwaraapi.db.dao.master.ingest.LibraryclassScanfolderDao;
import org.ishafoundation.dwaraapi.db.model.master.ingest.LibraryclassScanfolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/libraryclassscanfolder")
public class LibraryclassScanfolderController {

    @Autowired
    private LibraryclassScanfolderDao libraryclassScanfolderDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<LibraryclassScanfolder> libraryclassScanfolderList){
        libraryclassScanfolderDao.saveAll(libraryclassScanfolderList);
        return libraryclassScanfolderList.size();
    }

    @GetMapping("/getAll")
    public List<LibraryclassScanfolder> getAll(){
        return (List<LibraryclassScanfolder>)libraryclassScanfolderDao.findAll();
    }
}
