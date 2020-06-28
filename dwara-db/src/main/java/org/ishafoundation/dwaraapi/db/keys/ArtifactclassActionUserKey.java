package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ArtifactclassActionUserKey implements Serializable {
	
	private static final long serialVersionUID = 8894504543240809501L;

	@Column(name = "artifactclass_id")
    private int artifactclassId;
	
	@Column(name = "action_id")
    private String actionId;
 
    @Column(name = "user_id")
    private int userId;
 
    public ArtifactclassActionUserKey() {}
    
    public ArtifactclassActionUserKey(
        int artifactclassId,
        String actionName,
        int userId) {
    	this.artifactclassId = artifactclassId;
        this.actionId = actionName;
        this.userId = userId;
    }
 
	public int getArtifactclassId() {
		return artifactclassId;
	}

	public void setArtifactclassId(int artifactclassId) {
		this.artifactclassId = artifactclassId;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ArtifactclassActionUserKey that = (ArtifactclassActionUserKey) o;
        return Objects.equals(artifactclassId, that.artifactclassId) && 
        		Objects.equals(actionId, that.actionId) &&
        		Objects.equals(userId, that.userId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(artifactclassId, actionId, userId);
    }
}