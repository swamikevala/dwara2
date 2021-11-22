package org.ishafoundation.dwaraapi.api.req._import;

public class BulkImportRequest {
	
	private String stagingDir; // /data/dwara/import-staging

	public String getStagingDir() {
		return stagingDir;
	}

	public void setStagingDir(String stagingDir) {
		this.stagingDir = stagingDir;
	}
}
