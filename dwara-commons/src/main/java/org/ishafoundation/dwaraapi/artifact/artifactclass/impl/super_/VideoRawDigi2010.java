package org.ishafoundation.dwaraapi.artifact.artifactclass.impl.super_;

import org.ishafoundation.dwaraapi.artifact.ArtifactAttributes;
import org.ishafoundation.dwaraapi.artifact.artifactclass.Artifactclass;

public class VideoRawDigi2010 implements Artifactclass{

	private static final String DV_CODE_REGEX = "^[A-Z]{1,2}[\\d]{1,5}$";
	private static final String DVCAPTURED_CODE_REGEX = "^[\\d]+_D[Vv]-[Cc]aptured";

	@Override
	public ArtifactAttributes getArtifactAttributes(String proposedName) {
		ArtifactAttributes artifactAttributes = new ArtifactAttributes();
		
		if(proposedName.matches(DV_CODE_REGEX)) {
			artifactAttributes.setPreviousCode(proposedName);
			
		} else if(proposedName.matches(DVCAPTURED_CODE_REGEX + "_" + DV_CODE_REGEX) {  //5902_DV-Captured_A1929_Inner-Engineering_Tampa_Day3_Cam2_Tape2_10-Nov-06
			int idx = ordinalIndexOf(proposedName, "_", 3);
			String prefix = proposedName.substring(0,idx); //5902_DV-Captured_A1929
			int oldSeq = Integer.parseInt(prefix.substring(0, prefix.indexOf("_"))); //5902
			
			artifactAttributes.setPreviousCode(prefix.replace("_D[Vv]-[Cc]aptured", ""));  //5902_A1929
			artifactAttributes.setSequenceNumber(oldSeq);
		}
		return artifactAttributes;
	}
}		