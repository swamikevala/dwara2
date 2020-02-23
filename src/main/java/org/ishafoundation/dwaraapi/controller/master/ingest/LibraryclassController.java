package org.ishafoundation.dwaraapi.controller.master.ingest;

import org.ishafoundation.dwaraapi.db.dao.master.ingest.LibraryclassDao;
import org.ishafoundation.dwaraapi.db.model.master.ingest.Libraryclass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/libraryclass")
public class LibraryclassController {

    @Autowired
    private LibraryclassDao libraryclassDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<Libraryclass> libraryclassList){
        libraryclassDao.saveAll(libraryclassList);
        return libraryclassList.size();
    }

    @GetMapping("/getAll")
    public List<Libraryclass> getAll(){
        return (List<Libraryclass>)libraryclassDao.findAll();
    }
}
