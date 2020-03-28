package org.ishafoundation.dwaraapi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test3")
public class Test3Controller {

//	@Autowired
//	private TapedriveDao2 tapedriveDao;	
//	
//	@Autowired
//	private TapeDao2 tapeDao;	
//
//	@Autowired
//	private JobDao jobDao;	
//	
//	@Autowired
//	private TestDao testDao;	
//	
//	public ResponseEntity<String> updateTestTable(){
//		Test test = new Test();
//		test.setId(1);
//		test.setTape(null);
//		testDao.save(test);
//		
//		Test test2 = new Test();
//		test2.setId(2);
//		test2.setTape(null);
//		testDao.save(test2);
//
//		Tape2 tape = tapeDao.findByTapeId(13009);
//		
//		Test test3 = new Test();
//		test3.setId(3);
//		test3.setTape(tape);
//		testDao.save(test3);
//
//		Test test4 = new Test();
//		test4.setId(3);
//		test4.setTape(tape);
//		testDao.save(test4);
//		
//		return ResponseEntity.status(HttpStatus.OK).body("Done");
//	}
//	
//    @PostMapping("/updateTapedriveTable")
//    public ResponseEntity<String> updateTapedriveTable(){
//    	
//		Tapedrive2 tapedrive = tapedriveDao.findByElementAddress(0);
//		tapedrive.setStatus(TapedriveStatus.BUSY.toString());
//		
//		Tape2 tape = tapeDao.findByTapeId(13009);
////		Tape2 tape = new Tape2();
////		tape.setBarcode("test");
//		
//		Job job = jobDao.findById(1045).get();
////		Job job = new Job();
////		job.setCompletedAt(completedAt);
//		
//		tapedrive.setTape(tape);
//		tapedrive.setJob(job);
//   
//		tapedriveDao.save(tapedrive);
//
//    	return ResponseEntity.status(HttpStatus.OK).body("Done"); 
//    }
}
