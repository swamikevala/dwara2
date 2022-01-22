package org.ishafoundation.dwaraapi.artifact.artifactclass.impl.super_;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.artifact.ArtifactAttributes;
import org.ishafoundation.dwaraapi.artifact.artifactclass.Artifactclass;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class DefaultArtifactclassImpl implements Artifactclass{
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultArtifactclassImpl.class);
	
	public static final String NUMBER_REGEX = "^[\\d]+";
	public static final String ORIGINAL_REGEX = "^V[\\d]+";
	public static final String EDITED_REGEX = "^Z[\\d]+";
	public static final String EDITED_PRIV2_REGEX = "^ZX[\\d]+";
	public static final String BR_CODE_REGEX = "^BR[\\d]+";  
	public static final String EDITED_TRANSLATED_REGEX = "^ZG[\\d]+";
	public static final String EDITED_RESIDENTS_REGEX = "^ZR[\\d]+";
	
	private static final Map<String, Integer> ARTIFACTNAME_SEQUENCENUMBER_MAP = new HashMap<String, Integer>();
	private static final Set<String> HYPHENATED_ARTIFACTS_SET = new TreeSet<String>();

	public static final Pattern BR_CODE_NUMBER_REGEX_PATTERN = Pattern.compile(BR_CODE_REGEX + "_" + NUMBER_REGEX.substring(1));
	public static final Pattern BR_CODE_EDITED_REGEX_PATTERN = Pattern.compile(BR_CODE_REGEX + "_" + EDITED_REGEX.substring(1));
	public static final Pattern BR_CODE_EDITED_PRIV2_REGEX_PATTERN = Pattern.compile(BR_CODE_REGEX + "_" + EDITED_PRIV2_REGEX.substring(1));
	
	@PostConstruct
	public void setUp() throws Exception {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("ArtifactNameToSequenceNumberMapping.csv");
        BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        String line = null;
        
        while ((line = br.readLine()) != null) {
        	if(line.startsWith("#") || StringUtils.isBlank(line))
        		continue;
        	if(!line.contains("~!~")) {
        		logger.error("Skipping entry " + line);
        		continue;
        	}
        	String[] parts = line.split("~!~");
        	String artifactName = parts[0];
        	Integer sequenceNumber = null;
        	try {
        		sequenceNumber = Integer.parseInt(parts[1]);
        	}
        	catch (Exception e) {
        		logger.error("Skipping entry " + line, e);
        	}
        	ARTIFACTNAME_SEQUENCENUMBER_MAP.put(artifactName, sequenceNumber);
        }
        
        resource = resourceLoader.getResource("HyphenatedArtifactList.csv");
        br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        line = null;
        
        while ((line = br.readLine()) != null) {
        	if(line.startsWith("#") || StringUtils.isBlank(line))
        		continue;
        	HYPHENATED_ARTIFACTS_SET.add(line);
        }
        
	}
	
	@Override
	public void preImport(Artifact artifact) {
		String artifactName = artifact.getName();
		if(artifactName.startsWith(" ")) {
			artifactName = artifactName.trim();
			artifact.setRename(artifactName);
		}
		if(HYPHENATED_ARTIFACTS_SET.contains(artifactName)) {
			String extractedCode = StringUtils.substringBefore(artifactName, "-");
			
			if(extractedCode != null) {
				if(extractedCode.matches(NUMBER_REGEX) || extractedCode.matches(EDITED_REGEX) || extractedCode.matches(EDITED_PRIV2_REGEX)) { // 2283_BR-Meet-Day1-SG-SPH_18-Oct-09_Cam2 or Z2283-BR-Meet-Day1-SG-SPH_18-Oct-09_Cam2
					artifactName = artifactName.replace(extractedCode + "-", extractedCode + "_");
					artifact.setRename(artifactName);
				}
			}
		}
	}
	
	@Override
	public boolean validateImport(Artifact artifact) throws Exception {
		return true;
	}
	
	@Override
	public ArtifactAttributes getArtifactAttributes(String proposedName) {
		ArtifactAttributes artifactAttributes = new ArtifactAttributes();
		String extractedCode = StringUtils.substringBefore(proposedName, "_");
		if(extractedCode != null) {
			if(extractedCode.matches(NUMBER_REGEX)) {  //2283_BR-Meet-Day1-SG-SPH_18-Oct-09_Cam2
				int oldSeq = Integer.parseInt(extractedCode);
				artifactAttributes.setSequenceNumber(oldSeq);
				artifactAttributes.setMatchCode(extractedCode);
				artifactAttributes.setPreviousCode(extractedCode);
				artifactAttributes.setReplaceCode(true);
			}
			else if(extractedCode.matches(EDITED_REGEX) || extractedCode.matches(EDITED_PRIV2_REGEX)) {  //Z2283_BR-Meet-Day1-SG-SPH_18-Oct-09_Cam2
				artifactAttributes.setKeepCode(true);
				artifactAttributes.setMatchCode(extractedCode); // TODO @Swami K why do we need this for keep code scenario?
			}
			else if(extractedCode.matches(BR_CODE_REGEX)){  
				int idx = ordinalIndexOf(proposedName, "_", 2);
				if( idx > -1 ) {
					String prefix = proposedName.substring(0, idx); //BR00326_2283 


					if(BR_CODE_NUMBER_REGEX_PATTERN.matcher(proposedName).find()) {  //BR00326_2283_BR-Meet-Day1-SG-SPH_18-Oct-09_Cam2
						int oldSeq = Integer.parseInt(StringUtils.substringAfter(prefix,  "_")); //2283	
						artifactAttributes.setSequenceNumber(oldSeq);
						artifactAttributes.setMatchCode(prefix);
						artifactAttributes.setPreviousCode(prefix);
						artifactAttributes.setReplaceCode(true);
					} 
					else if(BR_CODE_EDITED_REGEX_PATTERN.matcher(proposedName).find() || BR_CODE_EDITED_PRIV2_REGEX_PATTERN.matcher(proposedName).find()) {  //BR00326_Z2283_BR-Meet-Day1-SG-SPH_18-Oct-09_Cam2
						int oldSeq = Integer.parseInt(StringUtils.substringAfter(prefix,  "_").substring(1)); //2283
						artifactAttributes.setSequenceNumber(oldSeq);
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
			else if(extractedCode.matches(ORIGINAL_REGEX)) { // For a tape containing dwara written artifacts and such 
				artifactAttributes.setMatchCode(extractedCode);
				artifactAttributes.setKeepCode(true);
			}
			else if(extractedCode.matches(EDITED_TRANSLATED_REGEX)){ //ZG72_SGCknYT001555_How-To-Simplify-&-Declutter-Your-Life-Sadhguru_Kannada_16-Oct-2019
				int idx = ordinalIndexOf(proposedName, "_", 2);
				if( idx > -1 ) {
					String prefix = proposedName.substring(0, idx); //ZG72_SGCknYT001555
					artifactAttributes.setPreviousCode(prefix);
				}
			}
			else if(extractedCode.matches(EDITED_RESIDENTS_REGEX)){ // ZR17_One-Drop-Of-Spirituality_Athur-Volunteers-Meet_29-Nov-2010_Edited-Files
				artifactAttributes.setSequenceNumber(Integer.parseInt(extractedCode.substring(2)));
				artifactAttributes.setMatchCode(extractedCode);
				artifactAttributes.setPreviousCode(extractedCode);
				artifactAttributes.setReplaceCode(true);
			}
		}
		
		if(ARTIFACTNAME_SEQUENCENUMBER_MAP.get(proposedName) != null){ // get the seqNumber from the list... NOTE: There are some range of sequences left for some artifacts missing out sequences - use them here
			if(Boolean.TRUE.equals(artifactAttributes.getKeepCode())) {
				artifactAttributes.setKeepCode(false);
				artifactAttributes.setPreviousCode(extractedCode);
				if(artifactAttributes.getMatchCode() != null)
					artifactAttributes.setReplaceCode(true);
			}
			artifactAttributes.setSequenceNumber(ARTIFACTNAME_SEQUENCENUMBER_MAP.get(proposedName));
		}

		return artifactAttributes;
	}
		
	public static int ordinalIndexOf(String str, String substr, int n) {
	    int pos = str.indexOf(substr);
	    while (--n > 0 && pos != -1)
	        pos = str.indexOf(substr, pos + 1);
	    return pos;
	}

	
	public static void main(String[] args) {
		Artifactclass ac = new DefaultArtifactclassImpl();
		
		String artifactName = "2283_BR-Meet-Day1-SG-SPH_18-Oct-09_Cam2";
		System.out.println(artifactName + "\n" + ac.getArtifactAttributes(artifactName) + "\n\n");
		
		artifactName = "Z2283_BR-Meet-Day1-SG-SPH_18-Oct-09_Cam2";
		System.out.println(artifactName + "\n" + ac.getArtifactAttributes(artifactName) + "\n\n");
		
		artifactName = "BR00326_2283_BR-Meet-Day1-SG-SPH_18-Oct-09_Cam2";
		System.out.println(artifactName + "\n" + ac.getArtifactAttributes(artifactName) + "\n\n");
		
		artifactName = "BR00326_Z2283_BR-Meet-Day1-SG-SPH_18-Oct-09_Cam2";
		System.out.println(artifactName + "\n" + ac.getArtifactAttributes(artifactName) + "\n\n");
		
		artifactName = "BR00326_BR-Meet-Day1-SG-SPH_18-Oct-09_Cam2";
		System.out.println(artifactName + "\n" + ac.getArtifactAttributes(artifactName) + "\n\n");
		
		artifactName = "V2283_BR-Meet-Day1-SG-SPH_18-Oct-09_Cam2";
		System.out.println(artifactName + "\n" + ac.getArtifactAttributes(artifactName) + "\n\n");
		
		ARTIFACTNAME_SEQUENCENUMBER_MAP.put("Ananda-Alai-Sathsang-Kothagiri_10-May-09_Cam1", 450);
		ARTIFACTNAME_SEQUENCENUMBER_MAP.put("Coimbatore-Mayor-Visit-to-IYC_26-Jun-09", 454);
		ARTIFACTNAME_SEQUENCENUMBER_MAP.put("7231_IE_Day1_Urban-Zen_New-York_2-May-14_Cam1", 22752);
		ARTIFACTNAME_SEQUENCENUMBER_MAP.put("Z2012_Br-Meet_HD_2009-Mar-10-Afternoon_Edited-Files", 712);
		ARTIFACTNAME_SEQUENCENUMBER_MAP.put("BR00402_Z2504_Br-Trip_Sadhguru-Sri-Brahma-Disciple-Guruvel-Ashram_Palani_30-Jul-2013_Edited-Files",910);
		
		artifactName = "Ananda-Alai-Sathsang-Kothagiri_10-May-09_Cam1";
		System.out.println(artifactName + "\n" + ac.getArtifactAttributes(artifactName) + "\n\n");
		
		artifactName = "7231_IE_Day1_Urban-Zen_New-York_2-May-14_Cam1";
		System.out.println(artifactName + "\n" + ac.getArtifactAttributes(artifactName) + "\n\n");

		artifactName = "Z2012_Br-Meet_HD_2009-Mar-10-Afternoon_Edited-Files";
		System.out.println(artifactName + "\n" + ac.getArtifactAttributes(artifactName) + "\n\n");

		artifactName = "BR00402_Z2504_Br-Trip_Sadhguru-Sri-Brahma-Disciple-Guruvel-Ashram_Palani_30-Jul-2013_Edited-Files";
		System.out.println(artifactName + "\n" + ac.getArtifactAttributes(artifactName) + "\n\n");

		HYPHENATED_ARTIFACTS_SET.add("863-Isha-Samskiriti-Krishna-Janmasti-Day-1-Aug-10");
		artifactName = "863-Isha-Samskiriti-Krishna-Janmasti-Day-1-Aug-10";
		Artifact artifact = new Artifact();
		artifact.setName(artifactName);
		ac.preImport(artifact);
		System.out.println(artifact);
		
		artifactName = " 863_StartingWithSpace-Isha-Samskiriti-Krishna-Janmasti-Day_1-Aug-10";
		artifact.setName(artifactName);
		ac.preImport(artifact);
		System.out.println(artifact);
		
		artifactName = "ZG72_SGCknYT001555_How-To-Simplify-&-Declutter-Your-Life-Sadhguru_Kannada_16-Oct-2019";
		System.out.println(artifactName + "\n" + ac.getArtifactAttributes(artifactName) + "\n\n");
		
		artifactName = "ZR17_One-Drop-Of-Spirituality_Athur-Volunteers-Meet_29-Nov-2010_Edited-Files";
		System.out.println(artifactName + "\n" + ac.getArtifactAttributes(artifactName) + "\n\n");
	}

}
