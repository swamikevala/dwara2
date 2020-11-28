package org.ishafoundation.dwaraapi.api.req.restore;

public class FileDetails {
	
	private Integer fileId;
	
	private String timecodeStart;
	
	private String timecodeEnd;

	public Integer getFileId() {
		return fileId;
	}

	public void setFileId(Integer fileId) {
		this.fileId = fileId;
	}

	public String getTimecodeStart() {
		return timecodeStart;
	}

	public void setTimecodeStart(String timecodeStart) {
		this.timecodeStart = timecodeStart;
	}

	public String getTimecodeEnd() {
		return timecodeEnd;
	}

	public void setTimecodeEnd(String timecodeEnd) {
		this.timecodeEnd = timecodeEnd;
	}
	
}
