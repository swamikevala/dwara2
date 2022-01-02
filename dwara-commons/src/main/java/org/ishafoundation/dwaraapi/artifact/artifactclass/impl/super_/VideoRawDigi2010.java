package org.ishafoundation.dwaraapi.artifact.artifactclass.impl.super_;

import org.ishafoundation.dwaraapi.artifact.ArtifactAttributes;
import org.ishafoundation.dwaraapi.artifact.artifactclass.Artifactclass;

public class VideoRawDigi2010 implements Artifactclass{

	@Override
	public ArtifactAttributes getArtifactAttributes(String artifactName) {
		ArtifactAttributes artifactAttributes = new ArtifactAttributes();
		artifactAttributes.setReplaceCode(false);
		return artifactAttributes;
	}

}
