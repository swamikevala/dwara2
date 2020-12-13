package org.ishafoundation.dwaraapi.enumreferences;

import java.util.ArrayList;
import java.util.List;

public enum CoreFlowelement {
	core_archive_flow_checksum_gen(-1, "core-archive-flow", null, "checksum-gen", null, null, -1, true, false),
	core_archive_flow_write(-2, "core-archive-flow", "write", null, null, null, -2, true, false),
	core_archive_flow_restore(-3, "core-archive-flow", "restore", null, new Integer[] {-2}, null, -3, true, false),
	core_archive_flow_checksum_verify(-4, "core-archive-flow", null, "checksum-verify", new Integer[] {-1,-3}, null, -4, true, false),
	
	core_restore_verify_flow_restore(-11, "core-restore-verify-flow", "restore", null, null, null, -11, true, false),
	core_restore_verify_flow_checksum_verify(-12, "core-restore-verify-flow", null, "checksum-verify", new Integer[] {-11}, null, -12, true, false);

	private int id;
	private String flowId;
	private String storagetaskActionId;
	private String processingtaskId;
	private Integer[] dependencies;
	private String flowRefId;
	private int displayOrder;
	private boolean active;
	private boolean deprecated;

	CoreFlowelement(int id, String flowId, String storagetaskActionId, String processingtaskId, Integer[] dependencies, String flowRefId, int displayOrder, boolean active, boolean deprecated){
		this.id = id; 
		this.flowId = flowId;
		this.storagetaskActionId = storagetaskActionId;
		this.processingtaskId = processingtaskId;
		this.dependencies = dependencies;
		this.flowRefId = flowRefId;
		this.displayOrder = displayOrder;
		this.active = active;
		this.deprecated = deprecated;
	}

	public int getId() {
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

	public Integer[] getDependencies() {
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
	
	public static List<CoreFlowelement> findAllByFlowId(String flowId){
		List<CoreFlowelement> coreFlowelementList = new ArrayList<CoreFlowelement>();
	    for (CoreFlowelement nthCoreFlowelement : CoreFlowelement.values()) {
	        if (nthCoreFlowelement.getFlowId().equals(flowId)) {
	        	coreFlowelementList.add(nthCoreFlowelement);
	        }
	    }
		return coreFlowelementList;
	}
	
	public static CoreFlowelement findById(int flowelementId){
	    for (CoreFlowelement nthCoreFlowelement : CoreFlowelement.values()) {
	        if (nthCoreFlowelement.getId() == flowelementId) {
	        	return nthCoreFlowelement;
	        }
	    }
		return null;
	}
}
