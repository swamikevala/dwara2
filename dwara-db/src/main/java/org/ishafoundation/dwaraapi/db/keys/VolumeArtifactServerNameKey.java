package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class VolumeArtifactServerNameKey implements Serializable {
	
	private static final long serialVersionUID = -2239115749779103274L;

	@Column(name = "volume_id")
	private String volumeId;

	@Column(name="artifact_name")
	private String artifactName;
	
	@Column(name="server_name")
	private String serverName;
 
    public VolumeArtifactServerNameKey() {}
    
    public VolumeArtifactServerNameKey(
		String volumeId,
		String artifactName,
		String serverName) {
    	this.volumeId = volumeId;
    	this.artifactName = artifactName;
    	this.serverName = serverName;
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

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        VolumeArtifactServerNameKey that = (VolumeArtifactServerNameKey) o;
        return Objects.equals(volumeId, that.volumeId) && 
        		Objects.equals(artifactName, that.artifactName) &&
        		Objects.equals(serverName, that.serverName);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(volumeId, artifactName, serverName);
    }
}