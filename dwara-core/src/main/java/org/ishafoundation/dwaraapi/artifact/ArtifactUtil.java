package org.ishafoundation.dwaraapi.artifact;



import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.utils.SequenceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArtifactUtil {
	
	@Autowired
	private ArtifactAttributesHandler artifactAttributesHandler;

	@Autowired
	private SequenceUtil sequenceUtil;

	public ArtifactMeta getArtifactMeta(String artifactName, String artifactclass, Sequence sequence, boolean generateSequence) throws Exception {
		ArtifactAttributes artifactAttributes = artifactAttributesHandler.getArtifactAttributes(artifactclass, artifactName, sequence.getPrefix());
		return getArtifactMeta(artifactName, sequence, artifactAttributes, generateSequence);
	}
	

	private ArtifactMeta getArtifactMeta(String artifactName, Sequence sequence, ArtifactAttributes artifactAttributes, boolean generateSequence) throws Exception {
		String previousCode = artifactAttributes.getPreviousCode();
		Integer sequenceNumber = artifactAttributes.getSequenceNumber();

		boolean keepCode = Boolean.TRUE.equals(artifactAttributes.getKeepCode());
		boolean replaceCode = Boolean.TRUE.equals(artifactAttributes.getReplaceCode());
		
		String toBeArtifactName = null;
		String extractedCodeFromProposedArtifactName = StringUtils.substringBefore(artifactName, "_");
		
		String sequenceCode =  null;
		
		if(keepCode) {
			sequenceCode = extractedCodeFromProposedArtifactName;
			toBeArtifactName = artifactName; // retaining the same name
		}
		else if(replaceCode) {
			if(previousCode == null || sequenceNumber == null)
				throw new Exception ("To replace a code both previousCode and sequenceNumber attributes should be present");
			sequenceCode = sequence.getPrefix() + sequenceNumber;
			toBeArtifactName = artifactName.replace(previousCode, sequenceCode);
		}else if(sequenceNumber != null){
			sequenceCode = sequence.getPrefix() + sequenceNumber;
			toBeArtifactName = sequenceCode + "_" + artifactName;
		}
		
		ArtifactMeta am = new ArtifactMeta();
		am.setArtifactName(toBeArtifactName);
		am.setSequenceCode(sequenceCode);				
		am.setPrevSequenceCode(previousCode);
		
		if(sequenceCode == null) {
			if(generateSequence) {
				am = generateSequenceCodeAndPrefixToName(artifactName, sequence);
			}
		}
		return am;
	}
	
	public ArtifactMeta generateSequenceCodeAndPrefixToName(String artifactName, Sequence sequence) {
		String sequenceCode = sequenceUtil.generateSequenceCode(sequence, artifactName);
		String toBeArtifactName = sequenceCode + "_" + artifactName;
		
		ArtifactMeta am = new ArtifactMeta();
		am.setArtifactName(toBeArtifactName);
		am.setSequenceCode(sequenceCode);
		return am;
	}
}
