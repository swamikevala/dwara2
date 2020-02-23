package org.ishafoundation.dwaraapi.controller.master.ingest;

import org.ishafoundation.dwaraapi.db.dao.master.ingest.PropertyLibraryclassDao;
import org.ishafoundation.dwaraapi.db.model.master.ingest.PropertyLibraryclass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/propertylibraryclass")
public class PropertyLibraryclassController {

    @Autowired
    private PropertyLibraryclassDao propertyLibraryclassDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<PropertyLibraryclass> propertyLibraryclassList){
        propertyLibraryclassDao.saveAll(propertyLibraryclassList);
        return propertyLibraryclassList.size();
    }

    @GetMapping("/getAll")
    public List<PropertyLibraryclass> getAll(){
        return (List<PropertyLibraryclass>)propertyLibraryclassDao.findAll();
    }
}
