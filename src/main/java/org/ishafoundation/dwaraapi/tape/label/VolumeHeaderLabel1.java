package org.ishafoundation.dwaraapi.tape.label;

import org.apache.commons.lang3.StringUtils;

public class VolumeHeaderLabel1 {
	
	// Label identifier - The characters VOL.
	private String lblID = "VOL"; // Bit Position(BP) = 1-3		  
	
	// Label number - 1/2 etc.,
	private String lblNumber = "1"; // BP = 4
	
	// The Volume Serial Number.
	private String volID = new String(new char[6]); // BP = 5-10
	
	// A space indicates that the volume is authorized.
	private String volAccessibility = " ";// new char[1]; 
	
	// LTO Generation Type e.g., L6/L7/LX(for WORM)
	private String ltoGen = StringUtils.repeat(" ", 2); // BP = 12-13

	// Reserved block 1
	private String reserved1 = StringUtils.repeat(" ", 11); // BP = 14-24
	
	// The Implementation Identifier padded with spaces.
	private String implID = StringUtils.repeat(" ", 13); // BP = 25-37

	private String ownerID = StringUtils.repeat(" ", 14); // BP = 38-51

	// Reserved block 2
	private String reserved2 = StringUtils.repeat(" ", 28); // BP = 52-79
	
	// The label standard level of interchange - ASCII 3/4. Level 4 is no restriction 
	private String lblStandard = "4";//new char[1];

	
	public VolumeHeaderLabel1(String volumeLabel) throws Exception {
		volID = StringUtils.substring(volumeLabel, 4, 10);
		ltoGen = StringUtils.substring(volumeLabel, 11, 13);
		implID = StringUtils.substring(volumeLabel, 24, 37);
		ownerID = StringUtils.substring(volumeLabel, 37, 51);
	}
	
	public VolumeHeaderLabel1(String volID, String ltoGen, String implId, String ownerId) throws Exception {
		if(StringUtils.isBlank(volID))
			throw new Exception("Volume Identifier cannot be null");// return;
		else if(volID.length() != 6)// TODO check this if length need to be 6 or should we pad
			throw new Exception("Volume Identifier isnt length 6");
		else
			this.volID = volID; 
		if(StringUtils.isBlank(ltoGen))
			throw new Exception("LTO Generation cannot be null");
		else
			this.ltoGen = StringUtils.rightPad(ltoGen, this.ltoGen.length());		
		
		if(StringUtils.isNotBlank(implId))
			this.implID = StringUtils.rightPad(implId, this.implID.length());
		if(StringUtils.isNotBlank(ownerId))
			this.ownerID = StringUtils.rightPad(ownerId, this.ownerID.length());
	}

	public String getLabel() throws Exception {
		if(volID.length() != 6)// TODO check this if neeeded
			throw new Exception("Volume Identifier isnt length 6");
		return lblID +  lblNumber +  volID + volAccessibility +  ltoGen + reserved1 +  implID  + ownerID + reserved2 +  lblStandard;
	}
	
	public String getVolID() {
		return volID;
	}

	public String getLtoGen() {
		return ltoGen;
	}

	public String getImplID() {
		return implID;
	}

	public String getOwnerID() {
		return ownerID;
	}
}
