package org.ishafoundation.dwaraapi.db.utils;

import java.io.File;

import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ArtifactclassUtil {
	
	@Value("${filesystem.rootlocation}")
	private String appRootlocation;
	
	public String getPath(Artifactclass artifactClass) {
		String pathWithOutLibrary = null;
		if(artifactClass.isSource())
			pathWithOutLibrary = appRootlocation + File.separator + artifactClass.getPathPrefix();
		else
			pathWithOutLibrary = appRootlocation + File.separator + artifactClass.getPathPrefix() + java.io.File.separator + artifactClass.getCategory();// getId();//getCategory();

		return pathWithOutLibrary;
	}
	
	
}
