package org.ishafoundation.dwaraapi.artifact;



import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.artifact.artifactclass.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.utils.SequenceUtil;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Artifact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArtifactUtil {

	@Autowired
	private SequenceUtil sequenceUtil;

	@Autowired
	private Map<String, Artifactclass> iArtifactclassMap;

	public void preImport(Artifact artifact){
		String artifactclass = artifact.getArtifactclass();
		Artifactclass ac = iArtifactclassMap.get(artifactclass);
		if(ac != null)
			ac.preImport(artifact);
	}
	
	public boolean validateImport(Artifact artifact) throws Exception{
		String artifactclass = artifact.getArtifactclass();
		Artifactclass ac = iArtifactclassMap.get(artifactclass);
		if(ac == null)
			return true;
		return ac.validateImport(artifact);
	}
	
	public ArtifactMeta getArtifactMeta(String artifactName, String artifactclass, Sequence sequence, boolean generateSequence) throws Exception {
		ArtifactAttributes artifactAttributes = getArtifactAttributes(artifactclass, artifactName, sequence.getPrefix());
		return getArtifactMeta(artifactName, sequence, artifactAttributes, generateSequence);
	}
	
	public ArtifactAttributes getArtifactAttributes(String artifactclass, String proposedName, String prefix) throws Exception {
		Artifactclass ac = iArtifactclassMap.get(artifactclass);
		if(ac == null) {
			// if there is no custom class fallback to default logic
			ArtifactAttributes artifactAttributes = new ArtifactAttributes();
			String extractedCode = StringUtils.substringBefore(proposedName, "_");
			if(extractedCode != null) {
				if((extractedCode + "_").matches("^" + prefix + "\\d+_")) {
					artifactAttributes.setKeepCode(true); 
				}
			}				
			return artifactAttributes;
		}
		return ac.getArtifactAttributes(proposedName);
	}

	private ArtifactMeta getArtifactMeta(String artifactName, Sequence sequence, ArtifactAttributes artifactAttributes, boolean generateSequence) throws Exception {
		String previousCode = artifactAttributes.getPreviousCode();
		String matchCode = artifactAttributes.getMatchCode();
		Integer sequenceNumber = artifactAttributes.getSequenceNumber();

		boolean keepCode = Boolean.TRUE.equals(artifactAttributes.getKeepCode());
		boolean replaceCode = Boolean.TRUE.equals(artifactAttributes.getReplaceCode());
		
		String toBeArtifactName = null;
		String extractedCodeFromProposedArtifactName = StringUtils.substringBefore(artifactName, "_");
		
		String sequenceCode =  null;
		
		if(keepCode) {
			sequenceCode = extractedCodeFromProposedArtifactName;
			toBeArtifactName = artifactName; // retaining the same name
			previousCode = null;
		}
		else if(replaceCode) {
			if(matchCode == null)
				throw new Exception ("To replace a code matchCode should be present");
			
			if(sequenceNumber != null) {
				sequenceCode = sequence.getPrefix() + sequenceNumber;
				toBeArtifactName = artifactName.replace(matchCode, sequenceCode);
			}
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
				am = generateSequenceCodeAndPrefixToName(artifactName, replaceCode, matchCode, sequence);
			}
		}
		return am;
	}
	
	public ArtifactMeta generateSequenceCodeAndPrefixToName(String artifactName, Boolean replaceCode, String matchCode, Sequence sequence) {
		String sequenceCode = sequenceUtil.generateSequenceCode(sequence, artifactName);
		String toBeArtifactName = null;
		if(Boolean.TRUE.equals(replaceCode) && matchCode != null)
			artifactName = artifactName.replace(matchCode, sequenceCode);
		else
			toBeArtifactName = sequenceCode + "_" + artifactName;
		
		ArtifactMeta am = new ArtifactMeta();
		am.setArtifactName(toBeArtifactName);
		am.setSequenceCode(sequenceCode);
		return am;
	}
}

