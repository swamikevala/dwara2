package org.ishafoundation.dwaraapi.storage.storagelevel.block.label;

import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;

public interface LabelManager {

	public boolean isRightVolume(StoragetypeJob storagetypeJob) throws Exception;
	
	public boolean writeVolumeLabel(StoragetypeJob storagetypeJob) throws Exception;
}
