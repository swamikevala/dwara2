package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class LibraryclassTapesetKey implements Serializable {
 
	private static final long serialVersionUID = -6723236319592657942L;

	@Column(name = "libraryclass_id")
    private int libraryclassId;
 
    @Column(name = "tapeset_id")
    private int tapesetId;
 
    public LibraryclassTapesetKey() {}
    
    public LibraryclassTapesetKey(
        int libraryclassId,
        int tapesetId) {
        this.libraryclassId = libraryclassId;
        this.tapesetId = tapesetId;
    }
 
	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
	}

	public int getTapesetId() {
		return tapesetId;
	}

	public void setTapesetId(int tapesetId) {
		this.tapesetId = tapesetId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        LibraryclassTapesetKey that = (LibraryclassTapesetKey) o;
        return Objects.equals(libraryclassId, that.libraryclassId) &&
               Objects.equals(tapesetId, that.tapesetId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(libraryclassId, tapesetId);
    }
}