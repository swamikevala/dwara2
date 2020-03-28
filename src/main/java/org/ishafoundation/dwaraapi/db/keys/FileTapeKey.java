package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class FileTapeKey implements Serializable {

	private static final long serialVersionUID = 5366126946379732068L;

	@Column(name = "file_id")
    private int fileId;
 
    @Column(name = "tape_id")
    private int tapeId;
 
    public FileTapeKey() {}
    
    public FileTapeKey(
        int fileId,
        int tapeId) {
        this.fileId = fileId;
        this.tapeId = tapeId;
    }
 
    public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
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
 
        FileTapeKey that = (FileTapeKey) o;
        return Objects.equals(fileId, that.fileId) &&
               Objects.equals(tapeId, that.tapeId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(fileId, tapeId);
    }
}