package org.ishafoundation.dwaraapi.controller.master.test;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.ExtensionDao;
import org.ishafoundation.dwaraapi.db.dao.master.FiletypeDao;
import org.ishafoundation.dwaraapi.db.model.master.Extension;
import org.ishafoundation.dwaraapi.db.model.master.Filetype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("setupExtensionFiletype")
public class SetupExtenstionFiletypeController {
	
	@Autowired
	private ExtensionDao extensionDao;

	@Autowired
	private FiletypeDao filetypeDao;
		
    @PostMapping("/insertExtensionFiletype")
    public boolean insertExtensionFiletype(){
    	Filetype video = filetypeDao.findById(4001).get();
    	/*
    	Filetype video = new Filetype(4001, "Video");
    	video = filetypeDao.save(video);
    	*/
    	Extension mp4 = new Extension(3050, "MTS", "Some MTS description");
//    	Extension mp4 = new Extension();
//    	mp4.setId((long) 1);
//    	mp4.setName("MP4");
//    	mp4.setDescription("Some MP4 Description");
    	mp4.addFiletype(video);
    	mp4 = extensionDao.save(mp4);
    	
    	/*
    	Extension mov = new Extension(3002, "MOV", "Some MOV description");
    	mov.addFiletype(video);
    	mov = extensionDao.save(mov);
*/

//    	Filetype audio = new Filetype(4006, "Audioooo");
//    	audio = filetypeDao.save(audio);
    	
//    	Extension mp3 = new Extension(3012, "MB3", "Some MB3 description");
//    	mp3.addFiletype(audio);
//    	mp3 = extensionDao.save(mp3);    	
		return true;
    }    	
    
    @GetMapping("/retrieveExtensionListForAFiletype")
    public ResponseEntity<List<Extension>> retrieveExtensionListForAFiletype(int filetypeId){
    	// now trying to retrieve all extensions
    	List<Extension> extensionList = extensionDao.findAllByFiletypesFiletypeId(filetypeId);

    	return ResponseEntity.status(HttpStatus.OK).body(extensionList); 
    }
    
    @GetMapping("/retrieveFiletypeOfAnExtension")
    public ResponseEntity<String> retrieveFiletypeOfAnExtension(String extnAsString){
    	// now trying to retrieve all extensions
    	Filetype filetype = filetypeDao.findByExtensionsExtensionName(extnAsString);

    	return ResponseEntity.status(HttpStatus.OK).body(filetype.getName()); 
    } 
    
}
