package org.ishafoundation.dwaraapi.storage.storagelevel;

import java.util.Map;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Archiveformat;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.IArchiveformatter;
import org.ishafoundation.dwaraapi.storage.model.DiskJob;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.ishafoundation.dwaraapi.storage.model.TapeJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("file"+DwaraConstants.StoragelevelSuffix)
//@Profile({ "!dev & !stage" })
public class FileStoragelevel implements IStoragelevel {

	private static final Logger logger = LoggerFactory.getLogger(FileStoragelevel.class);
	
	@Autowired
	private Map<String, IArchiveformatter> aafMap;

	// we just need the parameters need to set based on 

	@Override
	public ArchiveResponse format(StoragetypeJob job) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArchiveResponse write(StoragetypeJob job) {
		logger.debug("File storage means doesnt use any archive, but copies straight.");
		// Archiveformat only needed for block...
		// invoke the copy command here...
		return null;
	}
	
	@Override
	public ArchiveResponse verify(StoragetypeJob job) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArchiveResponse finalize(StoragetypeJob job) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArchiveResponse restore(StoragetypeJob job) {
		// TODO Auto-generated method stub
		return null;
	}

}
