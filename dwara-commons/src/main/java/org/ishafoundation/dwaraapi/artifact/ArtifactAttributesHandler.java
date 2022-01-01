package org.ishafoundation.dwaraapi.artifact;

import org.apache.commons.lang3.StringUtils;

public class ArtifactAttributesHandler {
	
	private static final String NUMERIC_SEQUENCE_REGEX = "^[\\d]{1,5}";
	private static final String EDITED_CODE_REGEX = "^Z[\\d]{1,5}";
	private static final String BR_CODE_REGEX = "^BR[\\d]{1,5}";
	private static final String EDITED_TR_CODE_REGEX = "^[A-Z]{3}[a-z]{3}[A-Z]{2}\\d{6}(?=_)";
	private static final String DIGI_2020_RAW_CODE_REGEX = "^[0-9A-Za-z-]+";
	private static final String DIGI_2020_EDITED_CODE_REGEX = "^ZP?\\d+";
	private static final String DIGI_2020_EDITED_P2_CODE_REGEX = "^ZY?\\d+";	
	public ArtifactAttributes getArtifactAttributes(String artifactclass, String proposedName) {
		ArtifactAttributes artifactAttributes = new ArtifactAttributes();
		String oldCode = null; 
		Integer oldSeq = null;
		Boolean keepCode = null;
		Boolean replaceCode = null;
		String extractedCode = null;
		switch(artifactclass) {
			case "video-edit-pub":
			case "video-edit-priv1":
			case "video-edit-priv2":	
				extractedCode = StringUtils.substringBefore(proposedName, "_");
				if(extractedCode != null) {
					if(extractedCode.matches(EDITED_CODE_REGEX)) {
						keepCode = true; // just set keepCode and dont do anything with prevCode and seqNum
						break; 
					}
					else if(extractedCode.matches(BR_CODE_REGEX)){
						String originalName = proposedName.substring(extractedCode.length() + 1);
						String oldOldCode = StringUtils.substringBefore(originalName, "_");
						if (oldOldCode != null) {
							if (oldOldCode.matches(EDITED_CODE_REGEX)) {
								oldCode = extractedCode + "_" + oldOldCode;
								oldSeq = Integer.parseInt(oldOldCode.substring(1));
								replaceCode = true;
							}
						}
					}
				}
				break;
			case "video-digi-2010-pub":
			case "video-digi-2010-priv1":
			case "video-digi-2010-priv2":
				replaceCode = false;
				break;
			case "video-edit-tr-pub":
			case "video-edit-tr-priv1":
			case "video-edit-tr-priv2":	
				extractedCode = StringUtils.substringBefore(proposedName, "_");
				if(extractedCode != null) {
					if(extractedCode.matches(EDITED_TR_CODE_REGEX)) {
						oldCode = extractedCode;
					}
				}
				break;
			case "video-digi-2020-pub":
			case "video-digi-2020-priv1":
			case "video-digi-2020-priv2":
				extractedCode = StringUtils.substringBefore(proposedName, "_");
				if(extractedCode != null) {
					if(extractedCode.matches(DIGI_2020_RAW_CODE_REGEX)) {
						oldCode = extractedCode;
					}
				}
				break;				
			case "video-digi-2020-edit-pub":
			case "video-digi-2020-edit-priv1":
				extractedCode = StringUtils.substringBefore(proposedName, "_");
				if(extractedCode != null) {
					if(extractedCode.matches(DIGI_2020_EDITED_CODE_REGEX)) {
						oldCode = extractedCode;
					}
				}
				break;				
			case "video-digi-2020-edit-priv2":
				extractedCode = StringUtils.substringBefore(proposedName, "_");
				if(extractedCode != null) {
					if(extractedCode.matches(DIGI_2020_EDITED_P2_CODE_REGEX)) {
						oldCode = extractedCode;
					}
				}
				break;					
			default:
				extractedCode = StringUtils.substringBefore(proposedName, "_");
				if(extractedCode != null) {
					if(extractedCode.matches(NUMERIC_SEQUENCE_REGEX)) {
						oldCode = extractedCode;
						oldSeq = Integer.parseInt(oldCode);
						replaceCode = true;
						break; 
					}
					else if(extractedCode.matches(BR_CODE_REGEX)){
						String originalName = proposedName.substring(extractedCode.length() + 1);
						String oldOldCode = StringUtils.substringBefore(originalName, "_");
						if (oldOldCode != null) {
							if (oldOldCode.matches(NUMERIC_SEQUENCE_REGEX)) {
								oldCode = extractedCode + "_" + oldOldCode;
								oldSeq = Integer.parseInt(oldOldCode);
								replaceCode = true;
							}
						}
					}
				}				
				break;
		}
		
		artifactAttributes.setPreviousCode(oldCode);
		artifactAttributes.setSequenceNumber(oldSeq);
		artifactAttributes.setKeepCode(keepCode);
		artifactAttributes.setReplaceCode(replaceCode);
		return artifactAttributes;
	}
	
	public class ArtifactAttributes{
		private String previousCode;
		private Integer sequenceNumber;
		private Boolean keepCode;
		private Boolean replaceCode;
		
		public String getPreviousCode() {
			return previousCode;
		}
		public void setPreviousCode(String previousCode) {
			this.previousCode = previousCode;
		}
		public Integer getSequenceNumber() {
			return sequenceNumber;
		}
		public void setSequenceNumber(Integer sequenceNumber) {
			this.sequenceNumber = sequenceNumber;
		}
		public Boolean getKeepCode() {
			return keepCode;
		}
		public void setKeepCode(Boolean keepCode) {
			this.keepCode = keepCode;
		}
		public Boolean getReplaceCode() {
			return replaceCode;
		}
		public void setReplaceCode(Boolean replaceCode) {
			this.replaceCode = replaceCode;
		}
		
		@Override
		public String toString() {
			return "\tpreviousCode - " + previousCode + "\n\tsequenceNumber - " + sequenceNumber + "\n\tkeepCode - " + keepCode + "\n\treplaceCode - " + replaceCode;
		}		
	} 
	
//	public static void main(String[] args) {
//		ArtifactAttributesHandler aah = new ArtifactAttributesHandler();
//		
//		String artifactclass = "video-pub";
//		String artifactName = "123_Mumbai-Satsang_12-Apr-2020_Cam1_FS7";
//		checkOutput(artifactclass, artifactName, aah);
//		
//		artifactclass = "video-priv1";
//		artifactName = "Mumbai-Satsang_12-Apr-2020_Cam1_FS7";
//		checkOutput(artifactclass, artifactName, aah);
//
//		artifactclass = "video-priv2";
//		artifactName = "BR555_123_Mumbai-Satsang_12-Apr-2020_Cam1_FS7";
//		checkOutput(artifactclass, artifactName, aah);
//		
//		artifactclass = "video-edit-pub";
//		artifactName = "Z456_Mumbai-Satsang_12-Apr-2020_Edited";
//		checkOutput(artifactclass, artifactName, aah);
//
//		artifactclass = "video-edit-priv1";
//		artifactName = "Mumbai-Satsang_12-Apr-2020_Edited";
//		checkOutput(artifactclass, artifactName, aah);
//
//		artifactclass = "video-edit-priv2";
//		artifactName = "BR999_Z456_Mumbai-Satsang_12-Apr-2020_Edited";
//		checkOutput(artifactclass, artifactName, aah);
//
//		artifactclass = "video-digi-2010-pub";
//		artifactName = "M228";
//		checkOutput(artifactclass, artifactName, aah);
//	}
//	
//	private static void checkOutput(String artifactclass, String artifactName, ArtifactAttributesHandler aah) {
//		System.out.println(artifactclass + " --- " + artifactName + "\n" + aah.getArtifactAttributes(artifactclass, artifactName));
//	}
}
