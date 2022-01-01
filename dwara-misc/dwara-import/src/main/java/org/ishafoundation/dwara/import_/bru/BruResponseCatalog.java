package org.ishafoundation.dwara.import_.bru;

import java.util.List;

public class BruResponseCatalog {
	private String archiveId;
	private List<BruFile> bruFileList;

	public String getArchiveId() {
		return archiveId;
	}
	public void setArchiveId(String archiveId) {
		this.archiveId = archiveId;
	}
	public List<BruFile> getBruFileList() {
		return bruFileList;
	}
	public void setBruFileList(List<BruFile> bruFileList) {
		this.bruFileList = bruFileList;
	}
}
