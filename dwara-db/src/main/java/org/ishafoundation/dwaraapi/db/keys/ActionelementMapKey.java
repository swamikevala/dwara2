package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ActionelementMapKey implements Serializable {

	private static final long serialVersionUID = -5624937109282165854L;

	@Column(name = "id")
    private int actionelementId;

    @Column(name = "id_ref")
    private int actionelementRefId;

    
    public ActionelementMapKey() {}
    
    public ActionelementMapKey(
        int actionelementId,
        int actionelementRefId) {

        this.actionelementId = actionelementId;
        this.actionelementRefId = actionelementRefId;
    }
 
 	public int getActionelementId() {
		return actionelementId;
	}

	public void setActionelementId(int actionelementId) {
		this.actionelementId = actionelementId;
	}
	
	public int getActionelementRefId() {
		return actionelementRefId;
	}

	public void setActionelementRefId(int actionelementRefId) {
		this.actionelementRefId = actionelementRefId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ActionelementMapKey that = (ActionelementMapKey) o;
        return Objects.equals(actionelementId, that.actionelementId) && 
        		Objects.equals(actionelementRefId, that.actionelementRefId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(actionelementId, actionelementRefId);
    }
}