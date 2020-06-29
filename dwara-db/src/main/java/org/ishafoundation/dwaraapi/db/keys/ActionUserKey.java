package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ActionUserKey implements Serializable {

	private static final long serialVersionUID = 1490630317339527316L;

	@Column(name = "action_id")
    private String actionId;
 
    @Column(name = "user_id")
    private int userId;
 
    public ActionUserKey() {}
    
    public ActionUserKey(
        String actionId,
        int userId) {
        this.actionId = actionId;
        this.userId = userId;
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
 
        ActionUserKey that = (ActionUserKey) o;
        return Objects.equals(actionId, that.actionId) &&
               Objects.equals(userId, that.userId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(actionId, userId);
    }
}