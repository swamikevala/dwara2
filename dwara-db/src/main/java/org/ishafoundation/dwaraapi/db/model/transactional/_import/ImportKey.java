package org.ishafoundation.dwaraapi.db.model.transactional._import;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ImportKey implements Serializable {

	private static final long serialVersionUID = 4380339555510450285L;

	@Column(name = "volume_id")
    private String volumeId;

    @Column(name = "run_id")
    private int runId;
    
    public ImportKey() {}
    
    public ImportKey(String volumeId, int runId) {
        this.volumeId = volumeId;
        this.runId = runId;
    }
    
	public String getVolumeId() {
		return volumeId;
	}

	public void setVolumeId(String volumeId) {
		this.volumeId = volumeId;
	}

	public int getRunId() {
		return runId;
	}

	public void setRunId(int runId) {
		this.runId = runId;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ImportKey that = (ImportKey) o;
        return Objects.equals(volumeId, that.volumeId) &&
               Objects.equals(runId, that.runId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(volumeId, runId);
    }

}
