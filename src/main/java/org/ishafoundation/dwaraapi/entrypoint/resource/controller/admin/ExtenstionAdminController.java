package org.ishafoundation.dwaraapi.entrypoint.resource.controller.admin;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.ExtensionDao;
import org.ishafoundation.dwaraapi.db.dao.master.TaskfiletypeDao;
import org.ishafoundation.dwaraapi.db.model.master.Extension;
import org.ishafoundation.dwaraapi.db.model.master.Taskfiletype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("extensionAdministration")
public class ExtenstionAdminController {
	
	@Autowired
	private ExtensionDao extensionDao;

	@Autowired
	private TaskfiletypeDao filetypeDao;
		
    @PostMapping("/addExtension")
    public boolean addExtension(@RequestParam String taskfiletypeName, @RequestParam String extension, @RequestParam String description){
    	Taskfiletype taskfiletype = filetypeDao.findByName(taskfiletypeName);
    	int extnId = extensionDao.findTopByOrderByIdDesc() != null ? extensionDao.findTopByOrderByIdDesc().getId() : 0;
    	if(extnId == 0)
    		return false;
    	
    	Extension extn = new Extension(extnId + 1, extension, description);
    	extn.addTaskfiletype(taskfiletype);
    	extn = extensionDao.save(extn);
		return true;
    }    	
    
    @GetMapping("/retrieveExtensionListForAFiletype")
    public ResponseEntity<List<Extension>> retrieveExtensionListForAFiletype(int taskfiletypeId){
    	// now trying to retrieve all extensions
    	List<Extension> extensionList = extensionDao.findAllByTaskfiletypesTaskfiletypeId(taskfiletypeId);
    	return ResponseEntity.status(HttpStatus.OK).body(extensionList); 
    }
    
    @GetMapping("/retrieveFiletypeOfAnExtension")
    public ResponseEntity<String> retrieveFiletypeOfAnExtension(String extnAsString){
    	// now trying to retrieve all extensions
    	Taskfiletype filetype = filetypeDao.findByExtensionsExtensionName(extnAsString);

    	return ResponseEntity.status(HttpStatus.OK).body(filetype.getName()); 
    } 
    
}
