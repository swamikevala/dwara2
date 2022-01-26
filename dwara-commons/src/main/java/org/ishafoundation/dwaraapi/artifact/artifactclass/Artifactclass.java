package org.ishafoundation.dwaraapi.artifact.artifactclass;

import org.ishafoundation.dwaraapi.artifact.ArtifactAttributes;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Artifact;

public interface Artifactclass {
	
	void preImport(Artifact artifact);
	
	boolean validateImport(Artifact artifact) throws Exception;
	
	ArtifactAttributes getArtifactAttributes(String artifactName);
}
