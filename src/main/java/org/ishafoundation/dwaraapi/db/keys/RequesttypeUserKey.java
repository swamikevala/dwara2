package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class RequesttypeUserKey implements Serializable {

	private static final long serialVersionUID = 1490630317339527316L;

	@Column(name = "requesttype_id")
    private int requesttypeId;
 
    @Column(name = "user_id")
    private int userId;
 
    public RequesttypeUserKey() {}
    
    public RequesttypeUserKey(
        int requesttypeId,
        int userId) {
        this.requesttypeId = requesttypeId;
        this.userId = userId;
    }
 
    public int getRequesttypeId() {
		return requesttypeId;
	}

	public void setRequesttypeId(int requesttypeId) {
		this.requesttypeId = requesttypeId;
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
 
        RequesttypeUserKey that = (RequesttypeUserKey) o;
        return Objects.equals(requesttypeId, that.requesttypeId) &&
               Objects.equals(userId, that.userId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(requesttypeId, userId);
    }
}