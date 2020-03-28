package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class LibraryclassTargetvolumeKey implements Serializable {
 
	private static final long serialVersionUID = -620517854806310403L;

	@Column(name = "libraryclass_id")
    private int libraryclassId;
 
    @Column(name = "targetvolume_id")
    private int targetvolumeId;
 
    public LibraryclassTargetvolumeKey() {}
    
    public LibraryclassTargetvolumeKey(
        int libraryclassId,
        int targetvolumeId) {
        this.libraryclassId = libraryclassId;
        this.targetvolumeId = targetvolumeId;
    }
 
	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
	}

	public int getTargetvolumeId() {
		return targetvolumeId;
	}

	public void setTargetvolumeId(int targetvolumeId) {
		this.targetvolumeId = targetvolumeId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        LibraryclassTargetvolumeKey that = (LibraryclassTargetvolumeKey) o;
        return Objects.equals(libraryclassId, that.libraryclassId) &&
               Objects.equals(targetvolumeId, that.targetvolumeId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(libraryclassId, targetvolumeId);
    }
}