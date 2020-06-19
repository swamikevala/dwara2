package org.ishafoundation.dwaraapi.storage.storagelevel;

import java.util.Map;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Archiveformat;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.IArchiveformatter;
import org.ishafoundation.dwaraapi.storage.model.DiskJob;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.ishafoundation.dwaraapi.storage.model.TapeJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("file"+DwaraConstants.StoragelevelSuffix)
//@Profile({ "!dev & !stage" })
public class FileStoragelevel implements IStoragelevel {

	@Autowired
	private Map<String, IArchiveformatter> aafMap;

	// we just need the parameters need to set based on 
	
	@Override
	public ArchiveResponse write(StoragetypeJob job) {
		System.out.println(this.getClass().getName() + " file storage ");
		System.out.println(this.getClass().getName() + " file storage means doesnt use any archive");
		Archiveformat archiveformat = job.getStorageJob().getVolume().getArchiveformat();
		// TODO : only needed for block...
		if(archiveformat != null) {
	    	IArchiveformatter archiveFormatter = aafMap.get(archiveformat.getName() + DwaraConstants.ArchiverSuffix);
	    	
	    	
			String artifactSourcePath = job.getStorageJob().getArtifactPrefixPath();
			int blockSizeInKB = 0;
			String deviceName = null;
			String artifactNameToBeWritten = job.getStorageJob().getArtifactName();
			// TODO Whatever is needed for file...
			if(job instanceof TapeJob) {
				TapeJob tj = (TapeJob) job;
				blockSizeInKB = 0; // TODO ??
				deviceName = tj.getTapedriveUid();
			} else if(job instanceof DiskJob) {
				DiskJob dj = (DiskJob) job;
				deviceName = job.getStorageJob().getVolume().getDetails().getMountpoint();
			}	
			try {
				return archiveFormatter.write(artifactSourcePath, blockSizeInKB, deviceName, artifactNameToBeWritten);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			// invoke the copy command here...
		}
		return null;
	}

	@Override
	public ArchiveResponse restore(StoragetypeJob job) {
		// TODO Auto-generated method stub
		return null;
	}

}
