package org.ishafoundation.dwaraapi.controller.master.workflow;

import org.ishafoundation.dwaraapi.db.dao.master.workflow.TaskDao;
import org.ishafoundation.dwaraapi.db.model.master.workflow.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskDao taskDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<Task> taskList){
        taskDao.saveAll(taskList);
        return taskList.size();
    }

    @GetMapping("/getAll")
    public List<Task> getAll(){
        return (List<Task>)taskDao.findAll();
    }
}
