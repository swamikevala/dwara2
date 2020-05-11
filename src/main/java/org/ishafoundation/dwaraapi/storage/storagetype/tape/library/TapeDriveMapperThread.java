package org.ishafoundation.dwaraapi.storage.storagetype.tape.library;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.model.master.Tapelibrary;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeDrivePreparer;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.DataTransferElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


// separate Thread so that we can wait until all drives are available ie., there arent any inprogress tape jobs to continue...
@Component
@Scope("prototype")
public class TapeDriveMapperThread implements Runnable{
	
	Logger logger = LoggerFactory.getLogger(TapeDriveMapperThread.class);

	@Autowired
	private SubrequestDao subrequestDao;
	
	@Autowired
	private TapeDriveMapper tapeDriveMapper;

	@Autowired
	private TapeDrivePreparer tapeDrivePreparer;


	private Subrequest subrequest;
	
	public void setSubrequest(Subrequest subrequest) {
		this.subrequest = subrequest;
	}

	
	@Override
	public void run() {
		HashMap<Tapelibrary, List<DataTransferElement>> prepared_tapelibrary_dteList_map = tapeDrivePreparer.prepareAllTapeDrivesForBlockingJobs();
		Set<Tapelibrary> tapelibrarySet = prepared_tapelibrary_dteList_map.keySet();
		boolean isSuccess = true;
		for (Iterator<Tapelibrary> iterator = tapelibrarySet.iterator(); iterator.hasNext();) {
			Tapelibrary tapelibrary = (Tapelibrary) iterator.next();
			
			String tapelibraryName = tapelibrary.getName();
			List<DataTransferElement> allDrives = prepared_tapelibrary_dteList_map.get(tapelibrary);
			try {
				tapeDriveMapper.mapDrives(tapelibraryName, allDrives);
			} catch (Exception e) {
				isSuccess = false;
			}
		}

		
		if(subrequest != null) { // Just to make tests works
			Status status = Status.failed;
			if(isSuccess)
				status = Status.completed;
		
			// update subrequest status
			String logMsg = "DB Subrequest - " + subrequest.getId() + " - Update - status to " + status;
			logger.debug(logMsg);
			subrequest.setStatus(status);
			subrequest = subrequestDao.save(subrequest);
	        logger.debug(logMsg + " - Success");
		}
	}
}
