package org.ishafoundation.dwaraapi.controller.master.storage;

import org.ishafoundation.dwaraapi.db.dao.master.storage.TapeDao;
import org.ishafoundation.dwaraapi.db.model.master.storage.Tape;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tape")
public class TapeController {

    @Autowired
    private TapeDao tapeDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<Tape> tapeList){
        tapeDao.saveAll(tapeList);
        return tapeList.size();
    }

    @GetMapping("/getAll")
    public List<Tape> getAll(){
        return (List<Tape>)tapeDao.findAll();
    }
}
