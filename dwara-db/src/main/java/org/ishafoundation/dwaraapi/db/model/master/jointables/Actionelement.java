package org.ishafoundation.dwaraapi.db.model.master.jointables;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.enumreferences.Action;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name = "Actionelement")
@Table(name = "actionelement", uniqueConstraints={@UniqueConstraint(columnNames = {"complex_action_id","artifactclass_id", "storagetask_action_id", "processingtask_id","volume_id"})})
public class Actionelement {

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name = "complex_action_id")
	private Action complexActionId;
	
	@Column(name="artifactclass_id")
	private int artifactclassId;
	
	@Column(name = "storagetask_action_id")
	private Action storagetaskActionId;// TODO : ??? How can this be 0 and will make the uniqueConstraints work???

	@Column(name = "processingtask_id")
	private String processingtaskId;
	
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

	public Action getComplexActionId() {
		return complexActionId;
	}

	public void setComplexActionId(Action complexActionId) {
		this.complexActionId = complexActionId;
	}

	public int getArtifactclassId() {
		return artifactclassId;
	}

	public void setArtifactclassId(int artifactclassId) {
		this.artifactclassId = artifactclassId;
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