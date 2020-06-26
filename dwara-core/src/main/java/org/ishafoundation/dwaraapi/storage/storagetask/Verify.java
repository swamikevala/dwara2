package org.ishafoundation.dwaraapi.storage.storagetask;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("verify")
//@Profile({ "!dev & !stage" })
public class Verify extends Write{

    private static final Logger logger = LoggerFactory.getLogger(Verify.class);
    
}
