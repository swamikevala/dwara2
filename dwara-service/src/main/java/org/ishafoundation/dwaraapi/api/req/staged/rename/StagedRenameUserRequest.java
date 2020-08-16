package org.ishafoundation.dwaraapi.api.req.staged.rename;

import java.util.List;

public class StagedRenameUserRequest{// extends StagedRenameFile{

	private List<StagedRenameFile> stagedRenameFileList;

	public List<StagedRenameFile> getStagedRenameFileList() {
		return stagedRenameFileList;
	}

	public void setStagedRenameFileList(List<StagedRenameFile> stagedRenameFileList) {
		this.stagedRenameFileList = stagedRenameFileList;
	}
}
