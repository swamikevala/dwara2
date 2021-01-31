package org.ishafoundation.dwaraapi.db.model.master.jointables;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.ishafoundation.dwaraapi.db.model.master.jointables.json.Taskconfig;
import org.ishafoundation.dwaraapi.enumreferences.Action;

@Entity(name = "Flowelement")
@Table(name = "flowelement")
public class Flowelement {

	@Id
	@Column(name="id")
	private String id;

//	@ManyToOne(fetch = FetchType.LAZY)
//	private Flow flow;
	@Column(name="flow_id")
	private String flowId;
	
	@Column(name = "storagetask_action_id")
	private Action storagetaskActionId;

	@Column(name = "processingtask_id")
	private String processingtaskId;
	
	@Type(type = "json")
	@Column(name="dependencies", columnDefinition = "json")
	private List<String> dependencies;
	
//	@ManyToOne(fetch = FetchType.LAZY)
//	private Flow flowRef;
	@Column(name="flow_ref_id")
	private String flowRefId;
	
	@Column(name = "display_order")
	private int displayOrder;

	@Column(name = "active")
	private boolean active;
	
	@Column(name="\"deprecated\"")
	private boolean deprecated;
	
	@Type(type = "json")
	@Column(name="task_config", columnDefinition = "json")
	private Taskconfig taskconfig;

	@Transient
	private Flowelement flowelementRef;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
//
//	public Flow getFlow() {
//		return flow;
//	}
//
//	public void setFlow(Flow flow) {
//		this.flow = flow;
//	}

	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	public String getFlowRefId() {
		return flowRefId;
	}

	public void setFlowRefId(String flowRefId) {
		this.flowRefId = flowRefId;
	}

	public Action getStoragetaskActionId() {
		return storagetaskActionId;
	}

	public void setStoragetaskActionId(Action storagetaskActionId) {
		this.storagetaskActionId = storagetaskActionId;
	}

	public String getProcessingtaskId() {
		if(StringUtils.isBlank(processingtaskId))
			processingtaskId = null;// We do this so that uniqueness constraint works...
		return processingtaskId;
	}

	public void setProcessingtaskId(String processingtaskId) {
		if(processingtaskId == null)
			processingtaskId = "";// We do this so that uniqueness constraint works...
		this.processingtaskId = processingtaskId;
	}
//
//	public String getFlowRefId() {
//		return flowRefId;
//	}
//
//	public void setFlowRefId(String flowRefId) {
//		this.flowRefId = flowRefId;
//	}

	public List<String> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<String> dependencies) {
		this.dependencies = dependencies;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}
//
//	public Flow getFlowRef() {
//		return flowRef;
//	}
//
//	public void setFlowRef(Flow flowRef) {
//		this.flowRef = flowRef;
//	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isDeprecated() {
		return deprecated;
	}

	public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
	}
	
	public Taskconfig getTaskconfig() {
		return taskconfig;
	}

	public void setTaskconfig(Taskconfig taskconfig) {
		this.taskconfig = taskconfig;
	}
	
	public Flowelement getFlowelementRef() {
		return flowelementRef;
	}

	public void setFlowelementRef(Flowelement flowelementRef) {
		this.flowelementRef = flowelementRef;
	}
	
	@Override
	public String toString() {
		return "Id : " + id;
	}
}