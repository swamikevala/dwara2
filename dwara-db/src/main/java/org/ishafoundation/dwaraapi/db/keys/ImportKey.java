package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ImportKey implements Serializable {

	private static final long serialVersionUID = 397632917957707570L;

	@Column(name = "volume_id")
    private String volumeId;
    
    @Column(name = "artifact_name")
    private String artifactName;
    
    @Column(name = "requeue_id")
    private int requeueId;
 
    public ImportKey() {}
    
    public ImportKey(String volumeId, String artifactName, int requeueId) {
        this.volumeId = volumeId;
        this.artifactName = artifactName;
        this.requeueId = requeueId;
    }

	public String getVolumeId() {
		return volumeId;
	}

	public void setVolumeId(String volumeId) {
		this.volumeId = volumeId;
	}

	public String getArtifactName() {
		return artifactName;
	}

	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}

	public int getRequeueId() {
		return requeueId;
	}

	public void setRequeueId(int requeueId) {
		this.requeueId = requeueId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ImportKey that = (ImportKey) o;
        return Objects.equals(volumeId, that.volumeId) &&
               Objects.equals(artifactName, that.artifactName) &&
               Objects.equals(requeueId, that.requeueId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(volumeId, artifactName, requeueId);
    }
}