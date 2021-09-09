package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.lang.reflect.Method;

import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ArtifactEntityUtil {

	private static final Logger logger = LoggerFactory.getLogger(ArtifactEntityUtil.class);
	
    public Artifact getDomainSpecificArtifactRef(Artifact artifact) throws Exception {
		Method artifactRefGetter = artifact.getClass().getMethod("get" + artifact.getClass().getSimpleName() + "Ref");
		return (Artifact) artifactRefGetter.invoke(artifact);
	}
	
	public void setDomainSpecificArtifactRef(Artifact artifact, Artifact artifactRef) throws Exception {
		Method artifactRefSetter = artifact.getClass().getMethod("set" + artifact.getClass().getSimpleName() + "Ref", artifact.getClass());
		artifactRefSetter.invoke(artifact, artifactRef); //sourceArtifactId
	}
}
