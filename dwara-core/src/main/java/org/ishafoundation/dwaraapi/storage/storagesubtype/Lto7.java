package org.ishafoundation.dwaraapi.storage.storagesubtype;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("LTO-7")
public class Lto7 extends AbstractStoragesubtype{

    private static final Logger logger = LoggerFactory.getLogger(Lto7.class);

    public Lto7() {
    	capacity = 6000000000000L;
    	generation = 7;
    	suffixToEndWith = "L7";
    	int[] writeSupportedGenerations = {6,7};
    	this.writeSupportedGenerations = writeSupportedGenerations;
    	int[] readSupportedGenerations = {5,6,7};
    	this.readSupportedGenerations = readSupportedGenerations;
	}
}
