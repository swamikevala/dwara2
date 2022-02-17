package org.ishafoundation.dwaraapi.storage.storagetype.disk.job;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.storage.storagetype.AbstractStoragetypeJobProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

//@Component("tapeJobProcessor")
//TODO : how to enforce the component name
@Component("disk" + DwaraConstants.STORAGETYPE_JOBPROCESSOR_SUFFIX)
//@Profile({ "!dev & !stage" })
public class DiskJobProcessor extends AbstractStoragetypeJobProcessor {

	private static final Logger logger = LoggerFactory.getLogger(DiskJobProcessor.class);
	
//	@Autowired
//	private CommandLineExecuter commandLineExecuter;

/*	
    public StorageResponse copy(SelectedStorageJob selectedStorageJob) throws Throwable{
    	
    	DiskJob diskJob = (DiskJob) selectedStorageJob;
		StorageJob storageJob = diskJob.getStorageJob();
		
    	int jobId = storageJob.getJob().getId();
    	logger.info("Copying job " + jobId);

    	StorageResponse storageResponse = null;
//    	beforeWrite(selectedStorageJob);
    
    	Path srcFilepath = Paths.get(storageJob.getArtifactPrefixPath(), storageJob.getArtifactName());
    	Path destDiskpath = Paths.get(diskJob.getMountPoint(), storageJob.getVolume().getId());
    	if(!destDiskpath.toFile().exists())
    		throw new DwaraException("Disk " + destDiskpath + " not found");
    	
    	
//    	Files.copy(srcFilepath, destPath);
    	
    	FileUtils.copyDirectory(srcFilepath.toFile(), Paths.get(destDiskpath.toString(), storageJob.getArtifactName()).toFile());
    	
//    	String command = "cp -r \"" + srcFilepath.toString() + "\" " + "\"" +  destPath.toString() +"\"";
//    	CommandLineExecutionResponse cler = null;
//    	
//
//		cler = commandLineExecuter.executeCommand(command);
//		if(cler.isComplete())
//			logger.debug("Job " + jobId + " completed succesfully - " + cler.getStdOutResponse());


//    	afterWrite(selectedStorageJob, storageResponse);
		storageResponse = new StorageResponse();
    	return storageResponse; 
    }
*/
}
