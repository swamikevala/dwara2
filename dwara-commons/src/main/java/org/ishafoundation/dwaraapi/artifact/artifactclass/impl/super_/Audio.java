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
			else if(extractedCode.matches(BR_CODE_REGEX)){
				/*
				BR00343_AUDIO_Br-Meet_28-Sep-10
				BR00346_AUDIO_Br-Meet-SPH_7-to-8-Nov-10
				BR00353_Br_Meet_Dhyanalinga_Temple_11-Jan-11_AUDIO
				BR00358_Br_Pre-Initiation_Talk_2011_Batch_Coconut-Grove_1-Apr-11_AUDIO
				
				Expected - AX2_AUDIO_Br-Meet_28-Sep-10 - prevCode: BR00343
				*/
				int idx = ordinalIndexOf(proposedName, "_", 2);
				if( idx > -1 ) {
					String prefix = proposedName.substring(0, idx); //BR00326_2283 


					if(BR_CODE_NUMBER_REGEX_PATTERN.matcher(proposedName).find() || BR_CODE_EDITED_REGEX_PATTERN.matcher(proposedName).find() || BR_CODE_EDITED_PRIV2_REGEX_PATTERN.matcher(proposedName).find()) {  //BR00326_Z2283_BR-Meet-Day1-SG-SPH_18-Oct-09_Cam2
						artifactAttributes.setMatchCode(prefix);
						artifactAttributes.setPreviousCode(prefix);
						artifactAttributes.setReplaceCode(true);
					}	
					else {
						artifactAttributes.setMatchCode(extractedCode); //BR00326
						artifactAttributes.setPreviousCode(extractedCode); //BR00326
						artifactAttributes.setReplaceCode(true);
					}
				}
			} 
			
		}
		return artifactAttributes;
	}

}