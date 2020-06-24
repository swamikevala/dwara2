package org.ishafoundation.dwaraapi.storage.storagetask;


import java.util.Map;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.ishafoundation.dwaraapi.storage.storagetype.AbstractStoragetypeJobProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("map_tapedrives")
//@Profile({ "!dev & !stage" })
public class MapTapedrives extends AbstractStoragetaskAction{

    private static final Logger logger = LoggerFactory.getLogger(MapTapedrives.class);

}
