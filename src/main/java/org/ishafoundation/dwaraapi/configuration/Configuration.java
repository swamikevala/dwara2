package org.ishafoundation.dwaraapi.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class Configuration {

	private String stagingSrcDirRoot;

	private String sshPrvKeyFileLocation;


	public String getStagingSrcDirRoot() {
		return stagingSrcDirRoot;
	}

	public void setStagingSrcDirRoot(String stagingSrcDirRoot) {
		this.stagingSrcDirRoot = stagingSrcDirRoot;
	}

	public String getSshPrvKeyFileLocation() {
		return sshPrvKeyFileLocation;
	}

	public void setSshPrvKeyFileLocation(String sshPrvKeyFileLocation) {
		this.sshPrvKeyFileLocation = sshPrvKeyFileLocation;
	}
}
