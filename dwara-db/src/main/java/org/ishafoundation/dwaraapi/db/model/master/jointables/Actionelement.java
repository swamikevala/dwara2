package org.ishafoundation.dwaraapi.db.model.master.jointables;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.enumreferences.Action;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name = "Actionelement")
@Table(name = "actionelement", uniqueConstraints={@UniqueConstraint(columnNames = {"action_id","artifactclass_id", "storagetask_id", "processingtask_id","volume_id"})})
public class Actionelement {

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name = "action_id")
	private Action action;
	
	@Column(name="artifactclass_id")
	private int artifactclassId;

	@Column(name = "storagetask_id")
	private int storagetaskId;

	@Column(name = "processingtask_id")
	private int processingtaskId;
	
	// No FK relationship as this could be set to 0
	@Column(name="volume_id")
	private int volumeId;
	
	@Column(name="actionelement_ref_id")
	private Integer actionelementRefId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="output_artifactclass_id")
	private Artifactclass outputArtifactclass;

	@Column(name = "encryption")
	private boolean encryption;

	@Column(name = "display_order")
	private int displayOrder;

	@Column(name = "active")
	private boolean active;


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public int getArtifactclassId() {
		return artifactclassId;
	}

	public void setArtifactclassId(int artifactclassId) {
		this.artifactclassId = artifactclassId;
	}

	public int getStoragetaskId() {
		return storagetaskId;
	}

	public void setStoragetaskId(int storagetaskId) {
		this.storagetaskId = storagetaskId;
	}

	public int getProcessingtaskId() {
		return processingtaskId;
	}

	public void setProcessingtaskId(int processingtaskId) {
		this.processingtaskId = processingtaskId;
	}

	public int getVolumeId() {
		return volumeId;
	}

	public void setVolumeId(int volumeId) {
		this.volumeId = volumeId;
	}

	public boolean isEncryption() {
		return encryption;
	}

	public void setEncryption(boolean encryption) {
		this.encryption = encryption;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Integer getActionelementRefId() {
		return actionelementRefId;
	}

	public void setActionelementRefId(Integer actionelementRefId) {
		this.actionelementRefId = actionelementRefId;
	}

	@JsonIgnore
	public Artifactclass getOutputArtifactclass() {
		return outputArtifactclass;
	}

	@JsonIgnore
	public void setOutputArtifactclass(Artifactclass outputArtifactclass) {
		this.outputArtifactclass = outputArtifactclass;
	}

}