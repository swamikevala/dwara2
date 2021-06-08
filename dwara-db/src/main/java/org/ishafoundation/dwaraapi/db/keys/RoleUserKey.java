package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class RoleUserKey implements Serializable {

	private static final long serialVersionUID = 1490630317339527316L;

	@Column(name = "role_id")
    private int roleId;
 
    @Column(name = "user_id")
    private int userId;
 
    public RoleUserKey() {}
    
    public RoleUserKey(
        int roleId,
        int userId) {
        this.roleId = roleId;
        this.userId = userId;
    }

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
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
 
        RoleUserKey that = (RoleUserKey) o;
        return Objects.equals(roleId, that.roleId) &&
               Objects.equals(userId, that.userId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(roleId, userId);
    }
}