package org.ishafoundation.dwaraapi.enumreferences;

public enum CoreFlow {
	core_archive_flow("archive-flow"),
	core_restore_checksumverify_flow("restore-verify-flow"),
	core_rewrite_flow("rewrite-flow");
	
	private String flowName;
	
	CoreFlow(String flowName){
		this.flowName = flowName;
	}
	
	public String getFlowName() {
		return flowName;
	}
}
