package org.ishafoundation.dwaraapi.controller.master.workflow;

import org.ishafoundation.dwaraapi.db.dao.master.workflow.CopyDao;
import org.ishafoundation.dwaraapi.db.model.master.workflow.Copy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/copy")
public class CopyController {

    @Autowired
    private CopyDao copyDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<Copy> copyList){
        copyDao.saveAll(copyList);
        return copyList.size();
    }

    @GetMapping("/getAll")
    public List<Copy> getAll(){
        return (List<Copy>)copyDao.findAll();
    }
}
