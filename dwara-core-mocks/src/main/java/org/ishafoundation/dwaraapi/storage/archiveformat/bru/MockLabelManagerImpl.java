package org.ishafoundation.dwaraapi.storage.archiveformat.bru;

import org.ishafoundation.dwaraapi.storage.storagelevel.block.label.LabelManagerImpl;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "dev | stage" })
public class MockLabelManagerImpl extends LabelManagerImpl{
	

}
