package org.ishafoundation.dwaraapi.controller.master.storage;

import org.ishafoundation.dwaraapi.db.dao.master.storage.TapelibraryDao;
import org.ishafoundation.dwaraapi.db.model.master.storage.Tapelibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tapelibrary")
public class TapelibraryController {

    @Autowired
    private TapelibraryDao tapelibraryDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<Tapelibrary> tapelibraryList){
        tapelibraryDao.saveAll(tapelibraryList);
        return tapelibraryList.size();
    }

    @GetMapping("/getAll")
    public List<Tapelibrary> getAll(){
        return (List<Tapelibrary>)tapelibraryDao.findAll();
    }
}
