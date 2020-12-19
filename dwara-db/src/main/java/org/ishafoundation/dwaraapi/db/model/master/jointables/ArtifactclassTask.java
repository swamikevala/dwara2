package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.jointables.json.ArtifactclassTaskConfig;
import org.ishafoundation.dwaraapi.enumreferences.Action;

@Entity(name = "ArtifactclassTask")
@Table(name="artifactclass_task")
public class ArtifactclassTask {

	@Id
	@Column(name="id")
	private int id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Artifactclass artifactclass;

	@Column(name="processingtask_id")
	private String processingtaskId;

	@Column(name = "storagetask_action_id")
	private Action storagetaskActionId;
	
	@Type(type = "json")
	@Column(name="config", columnDefinition = "json")
	private ArtifactclassTaskConfig config;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Artifactclass getArtifactclass() {
		return artifactclass;
	}

	public void setArtifactclass(Artifactclass artifactclass) {
		this.artifactclass = artifactclass;
	}

	public String getProcessingtaskId() {
		return processingtaskId;
	}

	public void setProcessingtaskId(String processingtaskId) {
		this.processingtaskId = processingtaskId;
	}

	public Action getStoragetaskActionId() {
		return storagetaskActionId;
	}

	public void setStoragetaskActionId(Action storagetaskActionId) {
		this.storagetaskActionId = storagetaskActionId;
	}

	public ArtifactclassTaskConfig getConfig() {
		return config;
	}

	public void setConfig(ArtifactclassTaskConfig config) {
		this.config = config;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ArtifactclassTask that = (ArtifactclassTask) o;
        return Objects.equals(artifactclass, that.artifactclass) &&
               Objects.equals(storagetaskActionId, that.storagetaskActionId) &&
               Objects.equals(processingtaskId, that.processingtaskId) ;
    }

	@Override
    public int hashCode() {
        return Objects.hash(artifactclass, storagetaskActionId, processingtaskId);
    }

}