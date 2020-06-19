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

@Component("block"+DwaraConstants.StoragelevelSuffix)
//@Profile({ "!dev & !stage" })
public class BlockStoragelevel implements IStoragelevel {

	@Autowired
	private Map<String, IArchiveformatter> aafMap;

	@Override
	public ArchiveResponse write(StoragetypeJob storagetypeJob) {
		System.out.println(this.getClass().getName() + " block storage ");
		Archiveformat archiveformat = storagetypeJob.getStorageJob().getVolume().getArchiveformat();
    	IArchiveformatter archiveFormatter = aafMap.get(archiveformat.getName() + DwaraConstants.ArchiverSuffix);
    	
    	
		String artifactSourcePath = storagetypeJob.getStorageJob().getArtifactPrefixPath();
		int blockSizeInKB = 0;
		String deviceName = null;
		String artifactNameToBeWritten = storagetypeJob.getStorageJob().getArtifactName();
		if(storagetypeJob instanceof TapeJob) {
			TapeJob tj = (TapeJob) storagetypeJob;
			blockSizeInKB = 0; // TODO ??
			deviceName = tj.getTapedriveUid();
		} else if(storagetypeJob instanceof DiskJob) {
			DiskJob dj = (DiskJob) storagetypeJob;
			deviceName = storagetypeJob.getStorageJob().getVolume().getDetails().getMountpoint();
		}	
		try {
			return archiveFormatter.write(artifactSourcePath, blockSizeInKB, deviceName, artifactNameToBeWritten);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ArchiveResponse restore(StoragetypeJob storagetypeJob) {
		// TODO Auto-generated method stub
		return null;
	}

}
