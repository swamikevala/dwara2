package org.ishafoundation.dwaraapi.controller.master.workflow;

import org.ishafoundation.dwaraapi.db.dao.master.workflow.TaskTasksetDao;
import org.ishafoundation.dwaraapi.db.model.master.workflow.TaskTaskset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasktaskset")
public class TaskTasksetController {

    @Autowired
    private TaskTasksetDao taskTasksetDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<TaskTaskset> taskTasksetList){
        taskTasksetDao.saveAll(taskTasksetList);
        return taskTasksetList.size();
    }

    @GetMapping("/getAll")
    public List<TaskTaskset> getAll(){
        return (List<TaskTaskset>)taskTasksetDao.findAll();
    }
}
