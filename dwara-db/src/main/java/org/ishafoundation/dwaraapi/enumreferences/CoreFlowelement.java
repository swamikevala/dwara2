package org.ishafoundation.dwaraapi.enumreferences;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.jointables.json.Taskconfig;

public enum CoreFlowelement {
	dep_archive_flow_checksum_gen("1", "archive-flow", null, "checksum-gen", null, null, 1, false, true, null),
	dep_archive_flow_write("2", "archive-flow", "write", null, null, null, 2, false, true, null),
	dep_archive_flow_checksum_verify("3", "archive-flow", "verify", null, new String[] {"1","2"}, null, 3, false, true, null),
	
	core_archive_flow_checksum_gen("C1", "archive-flow", null, "checksum-gen", null, null, 1, true, false, null),
	core_archive_flow_write("C2", "archive-flow", "write", null, null, null, 2, true, false, null),
	core_archive_flow_restore("C3", "archive-flow", "restore", null, new String[] {"C2"}, null, 3, true, false, null),
	core_archive_flow_checksum_verify("C4", "archive-flow", null, "checksum-verify", new String[] {"C1","C3"}, null, 4, true, false, null),
	
	core_restore_verify_flow_restore("C11", "restore-verify-flow", "restore", null, null, null, 11, true, false, null),
	core_restore_verify_flow_checksum_verify("C12", "restore-verify-flow", null, "checksum-verify", new String[] {"C11"}, null, 12, true, false, null),

	core_rewrite_flow_good_copy_restore("C21", "rewrite-flow", "restore", null, null, null, 21, true, false, null),
	core_rewrite_flow_good_copy_checksum_verify("C22", "rewrite-flow", null, "checksum-verify", new String[] {"C21"}, null, 22, true, false, null),
	core_rewrite_flow_write("C23", "rewrite-flow", "write", null, new String[] {"C22"}, null, 23, true, false, null),
	core_rewrite_flow_restore("C24", "rewrite-flow", "restore", null, new String[] {"C23"}, null, 24, true, false, null),
	core_rewrite_flow_checksum_verify("C25", "rewrite-flow", null, "checksum-verify", new String[] {"C24"}, null, 25, true, false, null);

	private String id;
	private String flowId;
	private String storagetaskActionId;
	private String processingtaskId;
	private String[] dependencies;
	private String flowRefId;
	private int displayOrder;
	private boolean active;
	private boolean deprecated;
	private Taskconfig taskconfig;

	CoreFlowelement(String id, String flowId, String storagetaskActionId, String processingtaskId, String[] dependencies, String flowRefId, int displayOrder, boolean active, boolean deprecated, Taskconfig taskconfig){
		this.id = id; 
		this.flowId = flowId;
		this.storagetaskActionId = storagetaskActionId;
		this.processingtaskId = processingtaskId;
		this.dependencies = dependencies;
		this.flowRefId = flowRefId;
		this.displayOrder = displayOrder;
		this.active = active;
		this.deprecated = deprecated;
		this.taskconfig = taskconfig;
	}

	public String getId() {
		return id;
	}

	public String getFlowId() {
		return flowId;
	}

	public String getStoragetaskActionId() {
		return storagetaskActionId;
	}

	public String getProcessingtaskId() {
		return processingtaskId;
	}

	public String[] getDependencies() {
		return dependencies;
	}

	public String getFlowRefId() {
		return flowRefId;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isDeprecated() {
		return deprecated;
	}
	
	public Taskconfig getTaskconfig() {
		return taskconfig;
	}

	public static List<CoreFlowelement> findAllByFlowId(String flowId){
		List<CoreFlowelement> coreFlowelementList = new ArrayList<CoreFlowelement>();
	    for (CoreFlowelement nthCoreFlowelement : CoreFlowelement.values()) {
	        if (nthCoreFlowelement.getFlowId().equals(flowId) && nthCoreFlowelement.isActive() && !nthCoreFlowelement.isDeprecated()) {
	        	coreFlowelementList.add(nthCoreFlowelement);
	        }
	    }
		return coreFlowelementList;
	}
	
	public static CoreFlowelement findById(String flowelementId){
	    for (CoreFlowelement nthCoreFlowelement : CoreFlowelement.values()) {
	        if (nthCoreFlowelement.getId().equals(flowelementId)) {
	        	return nthCoreFlowelement;
	        }
	    }
		return null;
	}
}
