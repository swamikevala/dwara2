package org.ishafoundation.dwaraapi.controller.master.process;

import org.ishafoundation.dwaraapi.db.dao.master.process.FiletypeDao;
import org.ishafoundation.dwaraapi.db.model.master.process.Filetype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/filetype")
public class FiletypeController {

    @Autowired
    private FiletypeDao filetypeDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<Filetype> filetypeList){
        filetypeDao.saveAll(filetypeList);
        return filetypeList.size();
    }

    @GetMapping("/getAll")
    public List<Filetype> getAll(){
        return (List<Filetype>)filetypeDao.findAll();
    }
}
