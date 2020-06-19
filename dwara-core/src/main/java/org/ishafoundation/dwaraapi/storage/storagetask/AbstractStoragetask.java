package org.ishafoundation.dwaraapi.storage.storagetask;



import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

public abstract class AbstractStoragetask{

	private static final Logger logger = LoggerFactory.getLogger(AbstractStoragetask.class);
	
//	@Autowired
//	private JobUtils jobUtils;	

	public ArchiveResponse process(StoragetypeJob storagejob) throws Throwable {
		Job job = null;
		ArchiveResponse archiveResponse = null;
		try {
//			job = storagejob.getJob();
//			jobUtils.updateJobInProgress(job);
			
			archiveResponse = execute(storagejob);
			
//			jobUtils.updateJobCompleted(job);
		}catch (Exception e) {
//			jobUtils.updateJobFailed(job);
			e.printStackTrace();
			throw e;
		}
		return archiveResponse;

	}

	public abstract ArchiveResponse execute(StoragetypeJob storagejob) throws Throwable;




}
