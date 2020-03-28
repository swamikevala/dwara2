package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class LibraryTapeKey implements Serializable {

	private static final long serialVersionUID = 8051084463733905503L;

	@Column(name = "library_id")
    private int libraryId;
 
    @Column(name = "tape_id")
    private int tapeId;
 
    public LibraryTapeKey() {}
    
    public LibraryTapeKey(
        int libraryId,
        int tapeId) {
        this.libraryId = libraryId;
        this.tapeId = tapeId;
    }
 
    public int getLibraryId() {
		return libraryId;
	}

	public void setLibraryId(int libraryId) {
		this.libraryId = libraryId;
	}

	public int getTapeId() {
		return tapeId;
	}

	public void setTapeId(int tapeId) {
		this.tapeId = tapeId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        LibraryTapeKey that = (LibraryTapeKey) o;
        return Objects.equals(libraryId, that.libraryId) &&
               Objects.equals(tapeId, that.tapeId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(libraryId, tapeId);
    }
}