package org.ishafoundation.dwaraapi.storage.storagelevel;

import java.util.Map;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.IArchiveformatter;
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("file"+DwaraConstants.STORAGELEVEL_SUFFIX)
//@Profile({ "!dev & !stage" })
public class FileStoragelevel implements IStoragelevel {

	private static final Logger logger = LoggerFactory.getLogger(FileStoragelevel.class);
	
	@Autowired
	private Map<String, IArchiveformatter> aafMap;

	// we just need the parameters need to set based on 

	@Override
	public StorageResponse initialize(SelectedStorageJob job) throws Exception{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public StorageResponse write(SelectedStorageJob job) throws Exception{
		logger.debug("File storage means doesnt use any archive, but copies straight.");
		// Archiveformat only needed for block...
		// invoke the copy command here...
		return null;
	}
	
	@Override
	public StorageResponse verify(SelectedStorageJob job) throws Exception{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StorageResponse finalize(SelectedStorageJob job) throws Exception{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public StorageResponse restore(SelectedStorageJob job) throws Exception{
		// TODO Auto-generated method stub
		return null;
	}

}
