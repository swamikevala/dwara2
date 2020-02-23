package org.ishafoundation.dwaraapi.controller.master.common;

import org.ishafoundation.dwaraapi.db.dao.master.common.RequesttypeDao;
import org.ishafoundation.dwaraapi.db.model.master.common.Requesttype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/requesttype")
public class RequesttypeController {

    @Autowired
    private RequesttypeDao requesttypeDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<Requesttype> requesttypeList){
        requesttypeDao.saveAll(requesttypeList);
        return requesttypeList.size();
    }

    @GetMapping("/getAll")
    public List<Requesttype> getAll(){
        return (List<Requesttype>)requesttypeDao.findAll();
    }
}
