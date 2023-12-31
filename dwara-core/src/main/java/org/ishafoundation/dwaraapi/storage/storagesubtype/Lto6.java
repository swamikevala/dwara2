package org.ishafoundation.dwaraapi.storage.storagesubtype;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("LTO-6")
public class Lto6 extends AbstractStoragesubtype{

    private static final Logger logger = LoggerFactory.getLogger(Lto6.class);

    public Lto6() {
    	capacity = 2500000000000L;
    	generation = 6;
    	suffixToEndWith = "L6";
    	int[] writeSupportedGenerations = {5,6};
    	this.writeSupportedGenerations = writeSupportedGenerations;
    	int[] readSupportedGenerations = {4,5,6};
    	this.readSupportedGenerations = readSupportedGenerations;
	}
}
