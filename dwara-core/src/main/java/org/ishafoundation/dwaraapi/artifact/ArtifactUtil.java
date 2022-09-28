package org.ishafoundation.dwaraapi.artifact;



import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

		String artifactNameMinusSequence = matchCode != null ? artifactName.replace(matchCode + "_", "") : artifactName;
		String toBeArtifactName = null;
		String sequenceCode =  null;
		
		if(keepCode) {
			sequenceCode = StringUtils.substringBefore(artifactName, "_");
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
		am.setArtifactNameMinusSequence(artifactNameMinusSequence);
		
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
			toBeArtifactName = artifactName.replace(matchCode, sequenceCode);
		else
			toBeArtifactName = sequenceCode + "_" + artifactName;
		
		ArtifactMeta am = new ArtifactMeta();
		am.setArtifactName(toBeArtifactName);
		am.setSequenceCode(sequenceCode);
		return am;
	}
	
	public static String renameWithDate(String name) {
		
		//VM34910_Conscious-Planet_Sadhguru-Shots_Hilton-Batumi_26-Apr-2022_FX3_4K
			
		String newDate = "";
		Pattern p = Pattern.compile("\\d{2}-\\w{3}-\\d{4}");
		Matcher m = p.matcher(name);
			
		if(m.find()) {
			String res = m.group();
			String[] tokens = res.split("-");
			newDate = tokens[2]+monthNum(tokens[1])+tokens[0]+"_";
		}
		return newDate + name;
	}
	
	private static String monthNum(String threeLetterMonth) {
		String twoLetterMonth = "";
		if (threeLetterMonth.equals("Jan")) {
		 twoLetterMonth = "01";
		} else if (threeLetterMonth.equals("Feb")) {
		 twoLetterMonth = "02";
		} else if (threeLetterMonth.equals("Mar")) {
		 twoLetterMonth = "03";
		} else if (threeLetterMonth.equals("Apr")) {
		 twoLetterMonth = "04";
		} else if (threeLetterMonth.equals("May")) {
		 twoLetterMonth = "05";
		} else if (threeLetterMonth.equals("Jun")) {
		 twoLetterMonth = "06";
		} else if (threeLetterMonth.equals("Jul")) {
		 twoLetterMonth = "07";
		} else if (threeLetterMonth.equals("Aug")) {
		 twoLetterMonth = "08";
		} else if (threeLetterMonth.equals("Sep")) {
		 twoLetterMonth = "09";
		} else if (threeLetterMonth.equals("Oct")) {
		 twoLetterMonth = "10";
		} else if (threeLetterMonth.equals("Nov")) {
		 twoLetterMonth = "11";
		} else if (threeLetterMonth.equals("Dec")) {
		 twoLetterMonth = "12";
		}
		return twoLetterMonth;
	}

}

