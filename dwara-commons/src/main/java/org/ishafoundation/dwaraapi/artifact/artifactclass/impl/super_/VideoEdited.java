package org.ishafoundation.dwaraapi.artifact.artifactclass.impl.super_;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.artifact.ArtifactAttributes;
import org.ishafoundation.dwaraapi.artifact.artifactclass.Artifactclass;

public class VideoEdited implements Artifactclass{
	
	private String EDITED_CODE_REGEX = "^Z[\\d]+";
	private static final String BR_CODE_REGEX = "^BR[\\d]+";

	public void setEDITED_CODE_REGEX(String eDITED_CODE_REGEX) {
		EDITED_CODE_REGEX = eDITED_CODE_REGEX;
	}

	@Override
	public ArtifactAttributes getArtifactAttributes(String proposedName) {
		ArtifactAttributes artifactAttributes = new ArtifactAttributes();
		String extractedCode = StringUtils.substringBefore(proposedName, "_");
		if(extractedCode != null) {
			if(extractedCode.matches(EDITED_CODE_REGEX)) {
				artifactAttributes.setKeepCode(true); // just set keepCode and dont do anything with prevCode and seqNum
			}
			else if(extractedCode.matches(BR_CODE_REGEX)){
				String originalName = proposedName.substring(extractedCode.length() + 1);
				String oldOldCode = StringUtils.substringBefore(originalName, "_");
				if (oldOldCode != null) {
					if (oldOldCode.matches(EDITED_CODE_REGEX)) {
						artifactAttributes.setPreviousCode(extractedCode + "_" + oldOldCode);
						artifactAttributes.setSequenceNumber(Integer.parseInt(oldOldCode.substring(1)));
						artifactAttributes.setReplaceCode(true);
					}
				}
			}
		}
		return artifactAttributes;
	}

}
