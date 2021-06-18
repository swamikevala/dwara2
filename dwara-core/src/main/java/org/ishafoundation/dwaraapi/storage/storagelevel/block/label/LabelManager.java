package org.ishafoundation.dwaraapi.storage.storagelevel.block.label;

import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;

public interface LabelManager {
	
	public Volumelabel readVolumeLabel(String deviceName, int blocksize) throws Exception;		
	
	public InterArtifactlabel readArtifactLabel(String deviceName, int blocksize) throws Exception;
	
	public boolean writeVolumeLabel(SelectedStorageJob selectedStorageJob) throws Exception;
	
	public void writeArtifactLabelTemporarilyOnDisk(SelectedStorageJob selectedStorageJob) throws Exception;
	
	public boolean writeArtifactLabel(SelectedStorageJob selectedStorageJob) throws Exception;
}
