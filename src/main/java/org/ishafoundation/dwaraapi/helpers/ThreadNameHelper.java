package org.ishafoundation.dwaraapi.helpers;

import org.apache.commons.lang3.StringUtils;

public class ThreadNameHelper {

	public void setThreadName(String threadName) {
		String origThreadName = Thread.currentThread().getName();
		//String appThreadName = StringUtils.substringBefore(origThreadName, "~!~") + "~!~mlID-" + mediaLibraryId + "-" + processName;
		String appThreadName = origThreadName + "~!~" + threadName;
		Thread.currentThread().setName(appThreadName);
	}
	
	public void setThreadName(int mediaLibraryId, String processName) {
		String origThreadName = Thread.currentThread().getName();
		//String appThreadName = StringUtils.substringBefore(origThreadName, "~!~") + "~!~mlID-" + mediaLibraryId + "-" + processName;
		String appThreadName = origThreadName + "~!~mlID-" + mediaLibraryId + "-" + processName;
		Thread.currentThread().setName(appThreadName);
	}
	
	public void resetThreadName() {
		String curThreadName = Thread.currentThread().getName();
		Thread.currentThread().setName(StringUtils.substringBefore(curThreadName, "~!~"));
	}
}
