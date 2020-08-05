package org.ishafoundation.dwaraapi.storage.storagelevel.block.label;

import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;

public interface LabelManager {

	public boolean isRightVolume(SelectedStorageJob storagetypeJob) throws Exception;
	
	public boolean writeVolumeLabel(SelectedStorageJob storagetypeJob) throws Exception;
}
