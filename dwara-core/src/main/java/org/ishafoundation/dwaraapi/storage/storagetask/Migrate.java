package org.ishafoundation.dwaraapi.storage.storagetask;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("migrate")
//@Profile({ "!dev & !stage" })
public class Migrate extends Rewrite{

    private static final Logger logger = LoggerFactory.getLogger(Migrate.class);

}
