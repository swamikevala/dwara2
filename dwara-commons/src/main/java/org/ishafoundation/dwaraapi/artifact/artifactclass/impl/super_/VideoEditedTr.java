package org.ishafoundation.dwaraapi.artifact.artifactclass.impl.super_;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.artifact.ArtifactAttributes;
import org.ishafoundation.dwaraapi.artifact.artifactclass.Artifactclass;

public class VideoEditedTr implements Artifactclass{
	
	private static final String EDITED_TR_CODE_REGEX = "^[A-Z]{3}[a-z]{3}[A-Z]{2}\\d+(?=_)";

	@Override
	public ArtifactAttributes getArtifactAttributes(String proposedName) {
		ArtifactAttributes artifactAttributes = new ArtifactAttributes();
		String extractedCode = StringUtils.substringBefore(proposedName, "_");
		if(extractedCode != null) {
			if(extractedCode.matches(EDITED_TR_CODE_REGEX)) {
				artifactAttributes.setPreviousCode(extractedCode);
			}
		}
		return artifactAttributes;
	}

}
