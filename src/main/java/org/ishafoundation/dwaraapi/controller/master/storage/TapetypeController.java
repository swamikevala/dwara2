package org.ishafoundation.dwaraapi.controller.master.storage;

import org.ishafoundation.dwaraapi.db.dao.master.storage.TapetypeDao;
import org.ishafoundation.dwaraapi.db.model.master.storage.Tapetype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tapetype")
public class TapetypeController {

    @Autowired
    private TapetypeDao tapetypeDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<Tapetype> tapetypeList){
        tapetypeDao.saveAll(tapetypeList);
        return tapetypeList.size();
    }

    @GetMapping("/getAll")
    public List<Tapetype> getAll(){
        return (List<Tapetype>)tapetypeDao.findAll();
    }
}
