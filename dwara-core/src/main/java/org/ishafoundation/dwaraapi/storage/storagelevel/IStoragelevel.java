package org.ishafoundation.dwaraapi.storage.storagelevel;

import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;

public interface IStoragelevel {
	
	public ArchiveResponse format(StoragetypeJob job);
	
	public ArchiveResponse write(StoragetypeJob job);
	
	public ArchiveResponse verify(StoragetypeJob job);
	
	public ArchiveResponse finalize(StoragetypeJob job);

	public ArchiveResponse restore(StoragetypeJob job);
}
