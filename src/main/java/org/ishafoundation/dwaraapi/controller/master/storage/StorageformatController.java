package org.ishafoundation.dwaraapi.controller.master.storage;

import org.ishafoundation.dwaraapi.db.dao.master.storage.StorageformatDao;
import org.ishafoundation.dwaraapi.db.model.master.storage.Storageformat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/storageformat")
public class StorageformatController {

    @Autowired
    private StorageformatDao storageformatDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<Storageformat> storageformatList){
        storageformatDao.saveAll(storageformatList);
        return storageformatList.size();
    }

    @GetMapping("/getAll")
    public List<Storageformat> getAll(){
        return (List<Storageformat>)storageformatDao.findAll();
    }
}
