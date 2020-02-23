package org.ishafoundation.dwaraapi.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="clffmpeg")
public class CommandLineFfmpegConfiguration {
	
	private Map<Integer, String> libraryclass = new HashMap<Integer, String>();

	public Map<Integer, String> getLibraryclass() {
		return libraryclass;
	}

	public void setLibraryclass(Map<Integer, String> libraryclass) {
		this.libraryclass = libraryclass;
	}
	
}
