package org.ishafoundation.dwaraapi.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="filesystem-permissions")
public class FilesystemPermissionsConfiguration {
			
	private String scriptPath;
	private String owner;
	private String group;
	private String directoryMode;
	private String fileMode;
	
	public String getScriptPath() {
		return scriptPath;
	}
	public void setScriptPath(String scriptPath) {
		this.scriptPath = scriptPath;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getDirectoryMode() {
		return directoryMode;
	}
	public void setDirectoryMode(String directoryMode) {
		this.directoryMode = directoryMode;
	}
	public String getFileMode() {
		return fileMode;
	}
	public void setFileMode(String fileMode) {
		this.fileMode = fileMode;
	}
}
