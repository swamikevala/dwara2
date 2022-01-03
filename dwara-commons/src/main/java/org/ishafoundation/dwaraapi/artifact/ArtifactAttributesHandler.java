package org.ishafoundation.dwaraapi.artifact;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.artifact.artifactclass.Artifactclass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArtifactAttributesHandler {
	
	@Autowired
	private Map<String, Artifactclass> iArtifactclassMap;
	
	public ArtifactAttributes getArtifactAttributes(String artifactclass, String proposedName, String prefix) throws Exception {
		Artifactclass ac = iArtifactclassMap.get(artifactclass);
		if(ac == null) {
			// if there is no custom class fallback to default logic
			ArtifactAttributes artifactAttributes = new ArtifactAttributes();
			String extractedCode = StringUtils.substringBefore(proposedName, "_");
			if(extractedCode != null) {
				if((extractedCode + "_").matches("^" + prefix + "\\d+_")) {
					artifactAttributes.setKeepCode(true); 
				}
			}				
			return artifactAttributes;
		}
		return ac.getArtifactAttributes(proposedName);
	}
}