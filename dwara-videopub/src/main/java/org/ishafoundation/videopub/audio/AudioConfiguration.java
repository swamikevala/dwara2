package org.ishafoundation.videopub.audio;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="nonDwaraAudioProxy")
public class AudioConfiguration {
	
	private Boolean check; // should the existence of nonDwara proxy be checked and generated or can be generated straight
	
	private Boolean remote; // Does the nonDwara proxies in remote system or in local

	// if remote = false then the below needs to be present
	private String localRootLocation; // root path where the file will be locally
	
	// if remote = true then the following needs to be present 
	private String host;
	
	private String sshSystemUser; // SSH credentials

	private String sshRootLocation; // root path where the file will be in remote system

	public Boolean getCheck() {
		return check;
	}

	public void setCheck(Boolean check) {
		this.check = check;
	}

	public Boolean getRemote() {
		return remote;
	}

	public void setRemote(Boolean remote) {
		this.remote = remote;
	}

	public String getLocalRootLocation() {
		return localRootLocation;
	}

	public void setLocalRootLocation(String localRootLocation) {
		this.localRootLocation = localRootLocation;
	}

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
