package org.ishafoundation.dwaraapi.storage.archiveformat.bru;

import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.label.LabelManagerImpl;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "dev | stage" })
public class MockLabelManagerImpl extends LabelManagerImpl{
	
	@Override
	public boolean isRightVolume(SelectedStorageJob selectedStorageJob) throws Exception {
		return true;
	}

}
