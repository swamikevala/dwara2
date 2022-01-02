package org.ishafoundation.dwaraapi.artifact;



import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.utils.SequenceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArtifactUtil {
	
	@Autowired
	protected SequenceUtil sequenceUtil;

	public ArtifactMeta getArtifactMeta(String artifactName, Sequence sequence, ArtifactAttributes artifactAttributes, boolean generateSequence) throws Exception {
		String previousCode = artifactAttributes.getPreviousCode();
		boolean keepCode = Boolean.TRUE.equals(artifactAttributes.getKeepCode());
		boolean replaceCode = Boolean.TRUE.equals(artifactAttributes.getReplaceCode());
		Integer sequenceNumber = artifactAttributes.getSequenceNumber();

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
		}
		
		ArtifactMeta am = new ArtifactMeta();
		am.setArtifactName(toBeArtifactName);
		am.setSequenceCode(sequenceCode);				
		
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
