package org.ishafoundation.dwaraapi.storage.storagesubtype;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("LTO-4")
public class Lto4 extends AbstractStoragesubtype{

    private static final Logger logger = LoggerFactory.getLogger(Lto4.class);

    public Lto4() {
    	capacity = 800000000000L;
    	generation = 4;
    	suffixToEndWith = "L4";
    	int[] writeSupportedGenerations = {3,4};
    	this.writeSupportedGenerations = writeSupportedGenerations;
    	int[] readSupportedGenerations = {2,3,4};
    	this.readSupportedGenerations = readSupportedGenerations;
	}
}
