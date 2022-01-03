package org.ishafoundation.dwaraapi.artifact.artifactclass.impl.super_;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.artifact.ArtifactAttributes;
import org.ishafoundation.dwaraapi.artifact.artifactclass.Artifactclass;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class VideoRaw implements Artifactclass{
	
	private static final String NUMERIC_SEQUENCE_REGEX = "^[\\d]{1,5}";
	private static final String BR_CODE_REGEX = "^BR[\\d]{1,5}";
	
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
        ARTIFACTNAME_SEQUENCENUMBER_MAP.put("xyz", 999);
	}
	
	@Override
	public ArtifactAttributes getArtifactAttributes(String proposedName) {
		ArtifactAttributes artifactAttributes = new ArtifactAttributes();
		String extractedCode = StringUtils.substringBefore(proposedName, "_");
		if(extractedCode != null) {
			if(extractedCode.matches(NUMERIC_SEQUENCE_REGEX)) {
				artifactAttributes.setPreviousCode(extractedCode);
				artifactAttributes.setSequenceNumber(Integer.parseInt(extractedCode));
				artifactAttributes.setReplaceCode(true);
			}
			else if(extractedCode.matches(BR_CODE_REGEX)){
				String originalName = proposedName.substring(extractedCode.length() + 1);
				String oldOldCode = StringUtils.substringBefore(originalName, "_");
				if (oldOldCode != null) {
					if (oldOldCode.matches(NUMERIC_SEQUENCE_REGEX)) {
						artifactAttributes.setPreviousCode(extractedCode + "_" + oldOldCode);
						artifactAttributes.setSequenceNumber(Integer.parseInt(oldOldCode));
						artifactAttributes.setReplaceCode(true);
					}
				}
			}
			else if(ARTIFACTNAME_SEQUENCENUMBER_MAP.get(proposedName) != null){ // get the seqNumber from the list... NOTE: There are some range of sequences left for some artifacts missing out sequences - use them here
				artifactAttributes.setSequenceNumber(ARTIFACTNAME_SEQUENCENUMBER_MAP.get(proposedName));
			}
		}				
		return artifactAttributes;
	}

}
