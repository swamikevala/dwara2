package org.ishafoundation.dwaraapi.helpers;

import org.apache.commons.lang3.StringUtils;

public class ThreadNameHelper {

	public void setThreadName(int systemRequestId, int jobId) {
		String origThreadName = Thread.currentThread().getName();
		String threadName = "sr-" + systemRequestId + "-job-" + jobId;
		String appThreadName = origThreadName + "~!~" + threadName;
		Thread.currentThread().setName(appThreadName);
	}
	
	public void setThreadName(int systemRequestId, int jobId, int fileId) {
		String origThreadName = Thread.currentThread().getName();
		String threadName = "sr-" + systemRequestId + "-job-" + jobId + "-file-" + fileId;
		String appThreadName = origThreadName + "~!~" + threadName;
		Thread.currentThread().setName(appThreadName);
	}
	
	public void resetThreadName() {
		String curThreadName = Thread.currentThread().getName();
		Thread.currentThread().setName(StringUtils.substringBefore(curThreadName, "~!~"));
	}


}
