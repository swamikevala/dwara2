package org.ishafoundation.dwaraapi.db.utils;

import java.io.File;

import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ArtifactclassUtil {
	
	@Value("${filesystem.rootlocation}")
	private String appRootlocation;
	
	public String getPathPrefix(Artifactclass artifactClass) {
		return appRootlocation + File.separator + artifactClass.getPathPrefixForArtifactclassUtil();
	}
	
	public String getPath(Artifactclass artifactClass) {
		String pathWithOutLibrary = null;
		if(artifactClass.isSource())
			pathWithOutLibrary = getPathPrefix(artifactClass);
		else
			pathWithOutLibrary = getPathPrefix(artifactClass) + java.io.File.separator + artifactClass.getCategory();// getId();//getCategory();

		return pathWithOutLibrary;
	}
	
	
}
