package org.ishafoundation.dwaraapi.artifact.artifactclass.impl.super_;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.artifact.ArtifactAttributes;
import org.ishafoundation.dwaraapi.artifact.artifactclass.impl.super_.DefaultArtifactclassImpl;
import org.springframework.stereotype.Component;

@Component
public class Audio extends DefaultArtifactclassImpl{
	
	@Override
	public ArtifactAttributes getArtifactAttributes(String proposedName) {
		ArtifactAttributes artifactAttributes = new ArtifactAttributes();
		String extractedCode = StringUtils.substringBefore(proposedName, "_");
		if(extractedCode != null) {
			if(extractedCode.matches(NUMBER_REGEX)) {  //13084_Sathsang-With-Sadhguru-For-Brahmacharis_AYA-IYC_01-Aug-2016_Audios
				artifactAttributes.setMatchCode(extractedCode);
				artifactAttributes.setPreviousCode(extractedCode);
				artifactAttributes.setReplaceCode(true);
			}
		}
		return artifactAttributes;
	}

}