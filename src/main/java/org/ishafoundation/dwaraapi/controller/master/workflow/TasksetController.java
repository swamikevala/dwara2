package org.ishafoundation.dwaraapi.controller.master.workflow;

import org.ishafoundation.dwaraapi.db.dao.master.workflow.TasksetDao;
import org.ishafoundation.dwaraapi.db.model.master.workflow.Taskset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/taskset")
public class TasksetController {

    @Autowired
    private TasksetDao tasksetDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<Taskset> tasksetList){
        tasksetDao.saveAll(tasksetList);
        return tasksetList.size();
    }

    @GetMapping("/getAll")
    public List<Taskset> getAll(){
        return (List<Taskset>)tasksetDao.findAll();
    }
}
