package org.ishafoundation.dwaraapi.storage.storagelevel.block.label;

import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;

public interface LabelManager {

	public boolean isRightVolume(SelectedStorageJob selectedStorageJob, boolean fromVolumelabel) throws Exception;
	
	public boolean writeVolumeLabel(SelectedStorageJob selectedStorageJob) throws Exception;
	
	public boolean writeArtifactLabel(SelectedStorageJob selectedStorageJob) throws Exception;
}
