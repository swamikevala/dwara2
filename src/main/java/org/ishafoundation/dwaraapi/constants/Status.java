package org.ishafoundation.dwaraapi.constants;

/**
 * Processing Status - Ensure status in here and the DB entries are in sync
 */
public enum Status {
    QUEUED(1),
    IN_PROGRESS(2),
    PARTIALLY_COMPLETED(3),
    COMPLETED(4),
    FAILED(5),
    CANCELLED(6),
	ABORTED(7),
	DELETED(8),
	SKIPPED(9),
	COMPLETED_WITH_FAILURE(10),
	MARKED_COMPLETED(11);
	
	private int statusId;
	
	Status(int statusId) {
	    this.statusId = statusId;
	}

	public static String getStatusStringFromId(int statusId){
		String statusAsString = null;
        for (Status es : Status.values()) {
            if (es.statusId == statusId) {
            	statusAsString = es.name();
            }
        }
		return statusAsString;
	}
	
	public int getStatusId() {
		return statusId;
	}
}
