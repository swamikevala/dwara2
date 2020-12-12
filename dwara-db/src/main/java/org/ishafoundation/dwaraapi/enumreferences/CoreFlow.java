package org.ishafoundation.dwaraapi.enumreferences;

public enum CoreFlow {
	core_archive_flow("core-archive-flow"),
	core_restore_checksumverify_flow("core-restore-verify-flow");
	
	private String flowName;
	
	CoreFlow(String flowName){
		this.flowName = flowName;
	}
	
	public String getFlowName() {
		return flowName;
	}
}
