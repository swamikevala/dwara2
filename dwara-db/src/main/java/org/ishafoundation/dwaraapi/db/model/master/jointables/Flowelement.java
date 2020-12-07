package org.ishafoundation.dwaraapi.db.model.master.jointables;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Flow;
import org.ishafoundation.dwaraapi.enumreferences.Action;

@Entity(name = "Flowelement")
@Table(name = "flowelement")
public class Flowelement {

	@Id
	@Column(name="id")
	private int id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Flow flow;
	
	@Column(name = "storagetask_action_id")
	private Action storagetaskActionId;

	@Column(name = "processingtask_id")
	private String processingtaskId;
	
	@Type(type = "json")
	@Column(name="dependencies", columnDefinition = "json")
	private List<Integer> dependencies;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Flow flowRef;
	
	@Column(name = "display_order")
	private int displayOrder;

	@Column(name = "active")
	private boolean active;
	
	@Column(name="\"deprecated\"")
	private boolean deprecated;
	
	@Transient
	private Flowelement flowelementRef;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Flow getFlow() {
		return flow;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
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

	public List<Integer> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<Integer> dependencies) {
		this.dependencies = dependencies;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public Flow getFlowRef() {
		return flowRef;
	}

	public void setFlowRef(Flow flowRef) {
		this.flowRef = flowRef;
	}

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