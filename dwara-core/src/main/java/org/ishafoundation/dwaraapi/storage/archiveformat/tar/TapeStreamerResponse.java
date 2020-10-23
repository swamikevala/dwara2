package org.ishafoundation.dwaraapi.storage.archiveformat.tar;

import java.util.LinkedHashMap;
import java.util.Map;

public class TapeStreamerResponse {

	private boolean success;
	
	private Map<String, Integer> filePathNameToHeaderBlockCnt = new LinkedHashMap<String, Integer>();

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Map<String, Integer> getFilePathNameToHeaderBlockCnt() {
		return filePathNameToHeaderBlockCnt;
	}

	public void setFilePathNameToHeaderBlockCnt(Map<String, Integer> filePathNameToHeaderBlockCnt) {
		this.filePathNameToHeaderBlockCnt = filePathNameToHeaderBlockCnt;
	}
	
}
