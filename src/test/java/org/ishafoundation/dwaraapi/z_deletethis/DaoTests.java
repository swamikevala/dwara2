package org.ishafoundation.dwaraapi.z_deletethis;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.TapedriveDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.LibraryTapeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.LibraryTape;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DaoTests{

	@Autowired
	private LibraryTapeDao libraryTapeDao;
	
	@Autowired
	private TapedriveDao tapedriveDao;	
	
	@Autowired
	private SubrequestDao subrequestDao;
	
	@Autowired
	private JobDao jobDao;	
	
	@Test
	public void testLibraryTapeDao() {		
		long a = libraryTapeDao.findUsedSpaceOnTape(12002);
		System.out.println(a);
	}
	
	//@Test
	public void testTapedriveDao() {
//		Tapedrive tapedrive = tapedriveDao.findByTapelibraryNameAndDeviceWwidContaining("/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400", "scsi-1IBM_ULT3580-TD5_1497199456-nst");
//		System.out.println(tapedrive.getDeviceWwid());
		
		Tapedrive tapedrive2 = tapedriveDao.findByDeviceWwidContaining("scsi-1IBM_ULT3580-TD5_1497199456-nst");
		System.out.println(tapedrive2.getDeviceWwid());
	}
	
	//@Test
	public void testSubrequestDao() {
		
		List<Status> statusList = new ArrayList<Status>();
		statusList.add(Status.queued);
		statusList.add(Status.in_progress);

		// If a subrequest action type is mapdrive and status is queued or inprogress skip storage jobs...
		long count = subrequestDao.countByRequestActionAndStatusIn(Action.map_tapedrives, statusList); 
		System.out.println(count);
	}	

	//@Test
	public void testJobDao() {
		
		List<Job> jobList = jobDao.findAllBySubrequestRequestActionAndStatus(Action.format_tape, Status.queued);
		System.out.println(jobList.size());
	}	
}
