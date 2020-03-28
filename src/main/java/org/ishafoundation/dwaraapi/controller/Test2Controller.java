package org.ishafoundation.dwaraapi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test2")
public class Test2Controller {
//
//	@Autowired
//	private RequestDao requestDao;
//	
//	@Autowired
//	private Subrequest2Dao subrequest2Dao;
//	
//    @PostMapping("/updateDB")
//    public ResponseEntity<String> updateDB(){
//    	Request request = new Request();
//    	request.setRequesttypeId(9008);
//    	request.setLibraryclassId(5001);
//    	request.setRequestedAt(System.currentTimeMillis());
//    	request.setRequestedBy("prak");
//    	System.out.println("DB Request Creation");
//    	request = requestDao.save(request);
//    	int requestId = request.getRequestId();
//    	System.out.println("DB Request Creation - Success " + requestId);
//
//    	
//    	Subrequest2 subrequest2_1 = new Subrequest2();
//    	subrequest2_1.setNewFilename("modifiedFileName");
//    	subrequest2_1.setOldFilename("originalFileName");
//    	subrequest2_1.setOptimizeTapeAccess(true); // TODO Hardcoded for now... is it not possible to have this set to false for ingest???
//    	subrequest2_1.setPrevSequenceCode("123345");
//    	subrequest2_1.setPriority(5);
//    	//subrequest2_1.setRequest(request);
//    	subrequest2_1.setRequest(request);
//    	subrequest2_1.setRerun(false);
//    	// TODO - Hardcoded
//    	int rerunNo = 0;
//    	subrequest2_1.setRerunNo(rerunNo);
//    	subrequest2_1.setSkipTasks(null);
//    	subrequest2_1.setSourcePath("ingestPath");
//    	subrequest2_1.setStatusId(1);
//
//    	System.out.println("DB Subrequest Creation");
//    	subrequest2_1 = subrequest2Dao.save(subrequest2_1);
//    	//System.out.println("DB Subrequest Creation - Success " + subrequest2_1.getRequest().getRequestId());
//    	System.out.println("DB Subrequest Creation - Success " + subrequest2_1.getRequest().getRequestId());
//    	
//    	Subrequest2 subrequest2_2 = new Subrequest2();
//    	subrequest2_2.setNewFilename("modifiedFileName2");
//    	subrequest2_2.setOldFilename("originalFileName2");
//    	subrequest2_2.setOptimizeTapeAccess(true); // TODO Hardcoded for now... is it not possible to have this set to false for ingest???
//    	subrequest2_2.setPrevSequenceCode("22222");
//    	subrequest2_2.setPriority(5);
//    	//subrequest2_2.setRequest(request);
//    	subrequest2_2.setRequest(request);
//    	subrequest2_2.setRerun(false);
//    	subrequest2_2.setRerunNo(rerunNo);
//    	subrequest2_2.setSkipTasks(null);
//    	subrequest2_2.setSourcePath("ingestPath2");
//    	subrequest2_2.setStatusId(1);
//
//    	System.out.println("DB Subrequest Creation");
//    	subrequest2_2 = subrequest2Dao.save(subrequest2_2);
//    	//System.out.println("DB Subrequest Creation - Success " + subrequest2_1.getRequest().getRequestId());
//    	System.out.println("DB Subrequest Creation - Success " + subrequest2_1.getRequest().getRequestId());
//    	
//    	return ResponseEntity.status(HttpStatus.OK).body("Done"); 
//    }
}
