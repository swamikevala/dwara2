package org.ishafoundation.dwaraapi.artifact;

import java.util.Map;

import org.ishafoundation.dwaraapi.artifact.artifactclass.Artifactclass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArtifactAttributesHandler {
	
	@Autowired
	private Map<String, Artifactclass> iArtifactclassMap;
	
	public ArtifactAttributes getArtifactAttributes(String artifactclass, String proposedName) throws Exception {
		Artifactclass ac = iArtifactclassMap.get(artifactclass);
		if(ac == null)
			throw new Exception(artifactclass + " dont have a class implementing IArtifactclass");
		return ac.getArtifactAttributes(proposedName);
	}
}