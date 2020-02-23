package org.ishafoundation.dwaraapi.controller.master.ingest;

import org.ishafoundation.dwaraapi.db.dao.master.ingest.RequesttypeLibraryclassDao;
import org.ishafoundation.dwaraapi.db.model.master.ingest.RequesttypeLibraryclass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/requesttypelibraryclass")
public class RequesttypeLibraryclassController {

    @Autowired
    private RequesttypeLibraryclassDao requesttypeLibraryclassDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<RequesttypeLibraryclass> requesttypeLibraryclassList){
        requesttypeLibraryclassDao.saveAll(requesttypeLibraryclassList);
        return requesttypeLibraryclassList.size();
    }

    @GetMapping("/getAll")
    public List<RequesttypeLibraryclass> getAll(){
        return (List<RequesttypeLibraryclass>)requesttypeLibraryclassDao.findAll();
    }
}
