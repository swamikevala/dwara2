package org.ishafoundation.dwaraapi.controller.master.storage;

import org.ishafoundation.dwaraapi.db.dao.master.storage.TapesetDao;
import org.ishafoundation.dwaraapi.db.model.master.storage.Tapeset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tapeset")
public class TapesetController {

    @Autowired
    private TapesetDao tapesetDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<Tapeset> tapesetList){
        tapesetDao.saveAll(tapesetList);
        return tapesetList.size();
    }

    @GetMapping("/getAll")
    public List<Tapeset> getAll(){
        return (List<Tapeset>)tapesetDao.findAll();
    }
}
