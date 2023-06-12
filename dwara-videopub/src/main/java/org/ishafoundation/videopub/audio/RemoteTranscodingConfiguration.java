package org.ishafoundation.videopub.audio;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="remote-transcoding")
public class RemoteTranscodingConfiguration {
	
	private String host;
	
	private String sshSystemUser; // SSH credentials

	private String sshRootLocation; // root path where the file will be in remote system


	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getSshSystemUser() {
		return sshSystemUser;
	}

	public void setSshSystemUser(String sshSystemUser) {
		this.sshSystemUser = sshSystemUser;
	}

	public String getSshRootLocation() {
		return sshRootLocation;
	}

	public void setSshRootLocation(String sshRootLocation) {
		this.sshRootLocation = sshRootLocation;
	}
}
