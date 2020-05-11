package org.ishafoundation.dwaraapi.storage.storagetype.tape.label;

import org.apache.commons.lang3.StringUtils;

public class VolumeHeaderLabel2 {
	
	// Label identifier - The characters VOL.
	private String lblID = "VOL";		  
	
	// Label number - 1/2 etc.,
	private String lblNumber = "2";
	
	// Implementation specific content goes here..
	private String impl = new String(new char[76]);

	// Incoming
	public VolumeHeaderLabel2() throws Exception {
		impl = StringUtils.rightPad("***  SOME IMPLEMENTATION SPECIFIC STUFF ***...", this.impl.length()) ; 
	}
	
	// Outgoing
	public VolumeHeaderLabel2(String volumeLabel) throws Exception {
		impl = StringUtils.substring(volumeLabel, 4, 79);
	}
	
	public String getLabel() throws Exception {
		return lblID +  lblNumber +  impl;
	}

	public String getImpl() {
		return impl;
	}
}
