package org.ishafoundation.dwaraapi.controller.master.workflow;

import org.ishafoundation.dwaraapi.db.dao.master.workflow.ProcessDao;
import org.ishafoundation.dwaraapi.db.model.master.workflow.Process;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/process")
public class ProcessController {

    @Autowired
    private ProcessDao processDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<Process> processList){
        processDao.saveAll(processList);
        return processList.size();
    }

    @GetMapping("/getAll")
    public List<Process> getAll(){
        return (List<Process>)processDao.findAll();
    }
}
