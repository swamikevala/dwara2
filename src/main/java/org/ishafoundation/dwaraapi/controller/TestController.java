package org.ishafoundation.dwaraapi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {
	
//	@Autowired
//	private SubrequestDao2 subrequestDao2;
//	
//    @GetMapping("/sample")
//    public ResponseEntity<String> triggerSample(){
//    	Set<Integer> statusIdSet = new HashSet<Integer>();
//    	statusIdSet.add(1);
//    	statusIdSet.add(4);
//    	List<Subrequest2> subrequestList = subrequestDao2.findAllByRequesttypeAndStatusIds(9008, statusIdSet);
//    	for (Subrequest2 nthSubrequest : subrequestList) {
//    		System.out.println("nth result set request " + nthSubrequest.getRequest().getRequestId());
//			System.out.println("nth result set " + nthSubrequest.getSubrequestId());
//		}
//    	
//    	return ResponseEntity.status(HttpStatus.OK).body("Done"); 
//    }
}
