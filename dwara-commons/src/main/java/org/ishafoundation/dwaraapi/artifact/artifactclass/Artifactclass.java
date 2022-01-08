package org.ishafoundation.dwaraapi.artifact.artifactclass;

import org.ishafoundation.dwaraapi.artifact.ArtifactAttributes;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Artifact;

public interface Artifactclass {
	
	ArtifactAttributes getArtifactAttributes(String artifactName);
	
	boolean validateImport(Artifact artifact) throws Exception;

}
