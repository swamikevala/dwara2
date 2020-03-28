package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ApplicationFileKey implements Serializable {

	private static final long serialVersionUID = -127105880509823063L;

	@Column(name = "application_id")
    private int applicationId;
 
    @Column(name = "file_id")
    private int fileId;
 
    public ApplicationFileKey() {}
    
    public ApplicationFileKey(
        int applicationId,
        int fileId) {
        this.applicationId = applicationId;
        this.fileId = fileId;
    }
 
	public int getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(int applicationId) {
		this.applicationId = applicationId;
	}

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ApplicationFileKey that = (ApplicationFileKey) o;
        return Objects.equals(applicationId, that.applicationId) &&
               Objects.equals(fileId, that.fileId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(applicationId, fileId);
    }
}