package org.ishafoundation.dwaraapi.artifact.artifactclass.impl.super_;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.artifact.ArtifactAttributes;
import org.ishafoundation.dwaraapi.artifact.artifactclass.Artifactclass;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Artifact;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class DefaultArtifactclassImpl implements Artifactclass{
	
	private static final String NUMBER_REGEX = "^[\\d]+";
	private static final String ORIGINAL_REGEX = "^V[\\d]+";
	private static final String EDITED_REGEX = "^Z[\\d]+";
	private static final String EDITED_PRIV2_REGEX = "^ZX[\\d]+";
	private static final String BR_CODE_REGEX = "^BR[\\d]+";  
	
	private static final Map<String, Integer> ARTIFACTNAME_SEQUENCENUMBER_MAP = new HashMap<String, Integer>();

	
	@PostConstruct
	public void setUp() throws Exception {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("ArtifactNameToSequenceNumberMapping.csv");
        BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        String line = null;
        
        while ((line = br.readLine()) != null) {
        	String[] parts = line.split("~!~");
        	String artifactName = parts[0];
        	Integer sequenceNumber = Integer.parseInt(parts[1]);
        	ARTIFACTNAME_SEQUENCENUMBER_MAP.put(artifactName, sequenceNumber);
        }
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
				String brCode = StringUtils.substringBefore(proposedName, "_");
				int idx = ordinalIndexOf(proposedName, "_", 2);
				if( idx > -1 ) {
					String prefix = proposedName.substring(0, idx); //BR00326_2283 

					if(proposedName.matches(BR_CODE_REGEX + "_" + NUMBER_REGEX)) {  //BR00326_2283_BR-Meet-Day1-SG-SPH_18-Oct-09_Cam2
						int oldSeq = Integer.parseInt(StringUtils.substringAfter(prefix,  "_")); //2283	
						artifactAttributes.setSequenceNumber(oldSeq);
						artifactAttributes.setMatchCode(prefix);
						artifactAttributes.setPreviousCode(prefix);
						artifactAttributes.setReplaceCode(true);
					} 
					else if(proposedName.matches(BR_CODE_REGEX + "_" + EDITED_REGEX) || proposedName.matches(BR_CODE_REGEX + "_" + EDITED_PRIV2_REGEX)) {  //BR00326_Z2283_BR-Meet-Day1-SG-SPH_18-Oct-09_Cam2
						int oldSeq = Integer.parseInt(StringUtils.substringAfter(prefix,  "_").substring(1)); //2283
						artifactAttributes.setSequenceNumber(oldSeq);
						artifactAttributes.setMatchCode(prefix);
						artifactAttributes.setPreviousCode(prefix);
						artifactAttributes.setReplaceCode(true);
					}	
					else {
						artifactAttributes.setPreviousCode(brCode); //BR00326
						artifactAttributes.setReplaceCode(true);
					}
				}
			} 
			else if(extractedCode.matches(ORIGINAL_REGEX)) {
				artifactAttributes.setMatchCode(extractedCode);
				artifactAttributes.setKeepCode(true);
			}
			else if(ARTIFACTNAME_SEQUENCENUMBER_MAP.get(proposedName) != null){ // get the seqNumber from the list... NOTE: There are some range of sequences left for some artifacts missing out sequences - use them here
				artifactAttributes.setSequenceNumber(ARTIFACTNAME_SEQUENCENUMBER_MAP.get(proposedName));
			}
		}				
		return artifactAttributes;
	}
		
	public static int ordinalIndexOf(String str, String substr, int n) {
	    int pos = str.indexOf(substr);
	    while (--n > 0 && pos != -1)
	        pos = str.indexOf(substr, pos + 1);
	    return pos;
	}

	@Override
	public boolean validateImport(Artifact artifact) throws Exception {
		return true;
	}
	
}
