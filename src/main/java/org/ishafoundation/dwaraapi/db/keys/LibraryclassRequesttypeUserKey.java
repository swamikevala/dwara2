package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class LibraryclassRequesttypeUserKey implements Serializable {
	
	private static final long serialVersionUID = 8894504543240809501L;

	@Column(name = "libraryclass_id")
    private int libraryclassId;
	
	@Column(name = "requesttype_id")
    private int requesttypeId;
 
    @Column(name = "user_id")
    private int userId;
 
    public LibraryclassRequesttypeUserKey() {}
    
    public LibraryclassRequesttypeUserKey(
        int libraryclassId,
        int requesttypeId,
        int userId) {
    	this.libraryclassId = libraryclassId;
        this.requesttypeId = requesttypeId;
        this.userId = userId;
    }
 
	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
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
 
        LibraryclassRequesttypeUserKey that = (LibraryclassRequesttypeUserKey) o;
        return Objects.equals(libraryclassId, that.libraryclassId) && 
        		Objects.equals(requesttypeId, that.requesttypeId) &&
        		Objects.equals(userId, that.userId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(libraryclassId, requesttypeId, userId);
    }
}