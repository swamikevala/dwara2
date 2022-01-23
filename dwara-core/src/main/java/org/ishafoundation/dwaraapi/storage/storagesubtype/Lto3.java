package org.ishafoundation.dwaraapi.storage.storagesubtype;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("LTO-3")
public class Lto3 extends AbstractStoragesubtype{

    private static final Logger logger = LoggerFactory.getLogger(Lto3.class);

    public Lto3() {
    	capacity = 400000000000L;
    	generation = 3;
    	suffixToEndWith = "L3";
    	int[] writeSupportedGenerations = {2,3};
    	this.writeSupportedGenerations = writeSupportedGenerations;
    	int[] readSupportedGenerations = {1,2,3};
    	this.readSupportedGenerations = readSupportedGenerations;
	}
}
