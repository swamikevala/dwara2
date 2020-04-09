package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class LibraryclassActionUserKey implements Serializable {
	
	private static final long serialVersionUID = 8894504543240809501L;

	@Column(name = "libraryclass_id")
    private int libraryclassId;
	
	@Column(name = "action_id")
    private int actionId;
 
    @Column(name = "user_id")
    private int userId;
 
    public LibraryclassActionUserKey() {}
    
    public LibraryclassActionUserKey(
        int libraryclassId,
        int actionId,
        int userId) {
    	this.libraryclassId = libraryclassId;
        this.actionId = actionId;
        this.userId = userId;
    }
 
	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
	}

    public int getActionId() {
		return actionId;
	}

	public void setActionId(int actionId) {
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
 
        LibraryclassActionUserKey that = (LibraryclassActionUserKey) o;
        return Objects.equals(libraryclassId, that.libraryclassId) && 
        		Objects.equals(actionId, that.actionId) &&
        		Objects.equals(userId, that.userId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(libraryclassId, actionId, userId);
    }
}