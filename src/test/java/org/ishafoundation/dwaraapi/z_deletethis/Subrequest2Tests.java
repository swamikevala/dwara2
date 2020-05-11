package org.ishafoundation.dwaraapi.z_deletethis;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.constants.Action;
import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.Subrequest2Dao;
import org.ishafoundation.dwaraapi.db.model.transactional.ActionColumns;
import org.ishafoundation.dwaraapi.db.model.transactional.FormatColumns;
import org.ishafoundation.dwaraapi.db.model.transactional.IngestColumns;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.RestoreColumns;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest2;
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
	
	@Test
	public void testSubrequest2_1() {
		Subrequest2 subrequest2 = subrequest2Dao.findById(3).get();
		IngestColumns ic = subrequest2.getActionColumns().getIngest();
		
		System.out.println(ic.getLibraryName());
	}
	
	@Test
	@WithMockUser(username = "pgurumurthy", password = "pwd")
	public void testSubrequest2_2() {
		String requestedBy = SecurityContextHolder.getContext().getAuthentication().getName();
    	
		//Action action = Action.ingest;
		//Action action = Action.restore;
		Action action = Action.format;
		
		Request request = new Request();
		request.setAction(action);
		request.setUser(userDao.findByName(requestedBy));
    	request.setRequestedAt(LocalDateTime.now());
    	requestDao.save(request);
    	
		Subrequest2 subrequest2 = new Subrequest2();
		subrequest2.setRequest(request);
		subrequest2.setStatus(Status.queued);
		
		IngestColumns ingest = new IngestColumns();
		ingest.setLibraryId(1);
		ingest.setLibraryName("some lib name");
		ingest.setSourcePath("some sourcePath");
		
		RestoreColumns restore = new RestoreColumns();
		restore.setFileId(1);
		restore.setPriority(0);
		
		FormatColumns format = new FormatColumns();
		format.setBarcode("some barcode");
		format.setStorageType("Some storage type");
		format.setForce(true);
		
		ActionColumns actionColumns = new ActionColumns();
		
		if(action == Action.ingest)
			actionColumns.setIngest(ingest);
		else if(action == Action.restore) 
			actionColumns.setRestore(restore);
		else if(action == Action.format) 
			actionColumns.setFormat(format);
		
		subrequest2.setActionColumns(actionColumns);
		
		subrequest2Dao.save(subrequest2);
		
		List<Status> statusList = new ArrayList<Status>();
		statusList.add(Status.queued);
		statusList.add(Status.in_progress);

		// If a subrequest action type is mapdrive and status is queued or inprogress skip storage jobs...
		long count = subrequest2Dao.countByRequestActionAndStatusIn(action, statusList); 
		System.out.println(count);
	}	
}
