package org.ishafoundation.dwaraapi.constants;

/**
 * Processing Status - Ensure status in here and the DB entries are in sync
 */
public enum TapedriveStatus {
    BUSY(1),
    AVAILABLE(2),
    MAINTANENCE(3);
	
	private int tapedriveStatusId;
	
	TapedriveStatus(int tapedriveStatusId) {
	    this.tapedriveStatusId = tapedriveStatusId;
	}

	public static String getTapedriveStatusStringFromId(int tapedriveStatusId){
		String statusAsString = null;
        for (TapedriveStatus es : TapedriveStatus.values()) {
            if (es.tapedriveStatusId == tapedriveStatusId) {
            	statusAsString = es.name();
            }
        }
		return statusAsString;
	}
	
	public int getTapedriveStatusId() {
		return tapedriveStatusId;
	}
}
