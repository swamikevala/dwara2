package org.ishafoundation.dwaraapi.storage.storagetask;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("import_")
//@Profile({ "!dev & !stage" })
public class Import extends AbstractStoragetaskAction{

    private static final Logger logger = LoggerFactory.getLogger(Import.class);

}
