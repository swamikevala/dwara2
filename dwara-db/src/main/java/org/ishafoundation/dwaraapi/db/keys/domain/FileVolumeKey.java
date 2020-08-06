package org.ishafoundation.dwaraapi.db.keys.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class FileVolumeKey implements Serializable {

	private static final long serialVersionUID = 5366126946379732068L;

	@Column(name = "file_id")
    private int fileId;
 
    @Column(name = "volume_id")
    private String volumeId;
 
    public FileVolumeKey() {}
    
    public FileVolumeKey(
        int fileId,
        String volumeId) {
        this.fileId = fileId;
        this.volumeId = volumeId;
    }
 
    public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public String getVolumeId() {
		return volumeId;
	}

	public void setVolumeId(String volumeId) {
		this.volumeId = volumeId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        FileVolumeKey that = (FileVolumeKey) o;
        return Objects.equals(fileId, that.fileId) &&
               Objects.equals(volumeId, that.volumeId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(fileId, volumeId);
    }
}