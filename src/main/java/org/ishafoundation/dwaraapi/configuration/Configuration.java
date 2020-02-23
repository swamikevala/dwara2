package org.ishafoundation.dwaraapi.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class Configuration {

	private String stagingSrcDirRoot;

	public String getStagingSrcDirRoot() {
		return stagingSrcDirRoot;
	}

	public void setStagingSrcDirRoot(String stagingSrcDirRoot) {
		this.stagingSrcDirRoot = stagingSrcDirRoot;
	}
}
