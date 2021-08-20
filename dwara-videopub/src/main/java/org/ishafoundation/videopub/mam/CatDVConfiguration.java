package org.ishafoundation.videopub.mam;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="catdv")
public class CatDVConfiguration {
	
	private boolean isSecured;
	private String host;
	private String port;
	
	// SSH credentials
	private String sshSystemUser;
	// root path where the file to be copied to
	private String sshProxiesRootLocation;

	// Web UI Credentials
	private String webUserID;
	private String webUserPwd;
	
	private String proxiesRootLocationSoftLinkName;
	
	public boolean isSecured() {
		return isSecured;
	}
	public void setSecured(boolean isSecured) {
		this.isSecured = isSecured;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getSshSystemUser() {
		return sshSystemUser;
	}
	public void setSshSystemUser(String sshSystemUser) {
		this.sshSystemUser = sshSystemUser;
	}
	public String getSshProxiesRootLocation() {
		return sshProxiesRootLocation;
	}
	public void setSshProxiesRootLocation(String sshProxiesRootLocation) {
		this.sshProxiesRootLocation = sshProxiesRootLocation;
	}
	public String getWebUserID() {
		return webUserID;
	}
	public void setWebUserID(String webUserID) {
		this.webUserID = webUserID;
	}
	public String getWebUserPwd() {
		return webUserPwd;
	}
	public void setWebUserPwd(String webUserPwd) {
		this.webUserPwd = webUserPwd;
	}
	public String getProxiesRootLocationSoftLinkName() {
		return proxiesRootLocationSoftLinkName;
	}
	public void setProxiesRootLocationSoftLinkName(String proxiesRootLocationSoftLinkName) {
		this.proxiesRootLocationSoftLinkName = proxiesRootLocationSoftLinkName;
	}
}
