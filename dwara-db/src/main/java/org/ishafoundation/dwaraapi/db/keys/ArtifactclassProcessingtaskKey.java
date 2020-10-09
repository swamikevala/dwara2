package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ArtifactclassProcessingtaskKey implements Serializable {

	private static final long serialVersionUID = -5665264641884923349L;

	@Column(name = "artifactclass_id")
    private String artifactclassId;
 
    @Column(name = "processingtask_id")
    private String processingtaskId;
 
    public ArtifactclassProcessingtaskKey() {}
    
    public ArtifactclassProcessingtaskKey(
        String artifactclassId,
        String processingtaskId) {
        this.artifactclassId = artifactclassId;
        this.processingtaskId = processingtaskId;
    }
 
	public String getArtifactclassId() {
		return artifactclassId;
	}

	public void setArtifactclassId(String artifactclassId) {
		this.artifactclassId = artifactclassId;
	}

	public String getProcessingtaskId() {
		return processingtaskId;
	}

	public void setProcessingtaskId(String processingtaskId) {
		this.processingtaskId = processingtaskId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ArtifactclassProcessingtaskKey that = (ArtifactclassProcessingtaskKey) o;
        return Objects.equals(artifactclassId, that.artifactclassId) &&
               Objects.equals(processingtaskId, that.processingtaskId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(artifactclassId, processingtaskId);
    }
}
