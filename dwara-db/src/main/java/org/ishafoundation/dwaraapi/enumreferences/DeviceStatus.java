package org.ishafoundation.dwaraapi.enumreferences;

/**
 * Processing Status - Ensure status in here and the DB entries are in sync
 */
public enum DeviceStatus {
  BUSY,
  AVAILABLE,
  MAINTANENCE;
	
	
//    BUSY(1),
//    AVAILABLE(2),
//    MAINTANENCE(3);
//	
//	private int deviceStatusId;
//	
//	DeviceStatus(int deviceStatusId) {
//	    this.deviceStatusId = deviceStatusId;
//	}
//
//	public static String getDeviceStatusStringFromId(int deviceStatusId){
//		String statusAsString = null;
//        for (DeviceStatus es : DeviceStatus.values()) {
//            if (es.deviceStatusId == deviceStatusId) {
//            	statusAsString = es.name();
//            }
//        }
//		return statusAsString;
//	}
//	
//	public int getDeviceStatusId() {
//		return deviceStatusId;
//	}
}
