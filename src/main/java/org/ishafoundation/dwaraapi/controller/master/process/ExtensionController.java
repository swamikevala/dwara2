package org.ishafoundation.dwaraapi.controller.master.process;

import org.ishafoundation.dwaraapi.db.dao.master.process.ExtensionDao;
import org.ishafoundation.dwaraapi.db.model.master.process.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/extension")
public class ExtensionController {

    @Autowired
    private ExtensionDao extensionDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<Extension> extensionList){
        extensionDao.saveAll(extensionList);
        return extensionList.size();
    }

    @GetMapping("/getAll")
    public List<Extension> getAll(){
        return (List<Extension>)extensionDao.findAll();
    }
}
