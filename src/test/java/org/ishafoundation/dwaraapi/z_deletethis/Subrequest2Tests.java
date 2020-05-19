package org.ishafoundation.dwaraapi.z_deletethis;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.Subrequest2Dao;
import org.ishafoundation.dwaraapi.db.model.transactional.ActionColumns;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest2;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Subrequest2Tests{

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private Subrequest2Dao subrequest2Dao;
	
	//@Test
	public void testSubrequest2_1() {
		Subrequest2 subrequest2 = subrequest2Dao.findById(5).get();
		ActionColumns ic = subrequest2.getActionColumns();
		
		System.out.println(ic.getLibraryName());
	}
	
	@Test
	@WithMockUser(username = "pgurumurthy", password = "pwd")
	public void testSubrequest2_2() {
		String requestedBy = SecurityContextHolder.getContext().getAuthentication().getName();
    	
		Action action = null;
		
		for (int i = 0; i < 3; i++) {
			if(i == 0)
				action = Action.ingest;
			else if(i == 1)
				action = Action.restore;
			else if(i == 2)
				action = Action.format_tape;
			
			Request request = new Request();
			request.setAction(action);
			request.setUser(userDao.findByName(requestedBy));
	    	request.setRequestedAt(LocalDateTime.now());
	    	requestDao.save(request);
	    	
			Subrequest2 subrequest2 = new Subrequest2();
			subrequest2.setRequest(request);
			subrequest2.setStatus(Status.queued);
			
			ActionColumns actionColumns = new ActionColumns();
			
			if(action == Action.ingest) {
				actionColumns.setSourcePath("some sourcePath");
//				actionColumns.setSkipTasks(null);
//				actionColumns.setRerun(false);
//				actionColumns.setRerunNo(0);
				actionColumns.setLibraryId(1);
				actionColumns.setLibraryName("some lib name");
//				actionColumns.setPrevSequenceCode(null);
			}
			else if(action == Action.restore) { 
				actionColumns.setFileId(1);
				actionColumns.setPriority(0);
			}
			else if(action == Action.format_tape) { 
				actionColumns.setBarcode("some barcode");
				actionColumns.setStorageType("Some storage type");
				actionColumns.setForce(true);
			}
			
			subrequest2.setActionColumns(actionColumns);
			
			subrequest2Dao.save(subrequest2);
		}
		List<Status> statusList = new ArrayList<Status>();
		statusList.add(Status.queued);
		statusList.add(Status.in_progress);

		// If a subrequest action type is mapdrive and status is queued or inprogress skip storage jobs...
		long count = subrequest2Dao.countByRequestActionAndStatusIn(action, statusList); 
		System.out.println(count);
	}	
}
