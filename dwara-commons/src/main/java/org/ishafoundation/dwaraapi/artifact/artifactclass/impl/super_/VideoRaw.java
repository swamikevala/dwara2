package org.ishafoundation.dwaraapi.artifact.artifactclass.impl.super_;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.artifact.ArtifactAttributes;
import org.ishafoundation.dwaraapi.artifact.artifactclass.Artifactclass;

public class VideoRaw implements Artifactclass{
	
	private static final String NUMERIC_SEQUENCE_REGEX = "^[\\d]{1,5}";
	private static final String BR_CODE_REGEX = "^BR[\\d]{1,5}";

	
	@Override
	public ArtifactAttributes getArtifactAttributes(String proposedName) {
		ArtifactAttributes artifactAttributes = new ArtifactAttributes();
		String extractedCode = StringUtils.substringBefore(proposedName, "_");
		if(extractedCode != null) {
			if(extractedCode.matches(NUMERIC_SEQUENCE_REGEX)) {
				artifactAttributes.setPreviousCode(extractedCode);
				artifactAttributes.setSequenceNumber(Integer.parseInt(extractedCode));
				artifactAttributes.setReplaceCode(true);
			}
			else if(extractedCode.matches(BR_CODE_REGEX)){
				String originalName = proposedName.substring(extractedCode.length() + 1);
				String oldOldCode = StringUtils.substringBefore(originalName, "_");
				if (oldOldCode != null) {
					if (oldOldCode.matches(NUMERIC_SEQUENCE_REGEX)) {
						artifactAttributes.setPreviousCode(extractedCode + "_" + oldOldCode);
						artifactAttributes.setSequenceNumber(Integer.parseInt(oldOldCode));
						artifactAttributes.setReplaceCode(true);
					}
				}
			}
		}				
		return artifactAttributes;
	}

}
