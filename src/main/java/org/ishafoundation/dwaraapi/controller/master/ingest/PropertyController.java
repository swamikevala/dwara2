package org.ishafoundation.dwaraapi.controller.master.ingest;

import org.ishafoundation.dwaraapi.db.dao.master.ingest.PropertyDao;
import org.ishafoundation.dwaraapi.db.model.master.ingest.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/property")
public class PropertyController {

    @Autowired
    private PropertyDao propertyDao;


    @PostMapping("/addAll")
    public int addAll(@RequestBody List<Property> propertyList){
        propertyDao.saveAll(propertyList);
        return propertyList.size();
    }

    @GetMapping("/getAll")
    public List<Property> getAll(){
        return (List<Property>)propertyDao.findAll();
    }
}
