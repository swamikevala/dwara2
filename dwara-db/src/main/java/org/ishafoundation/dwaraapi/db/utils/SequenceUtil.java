package org.ishafoundation.dwaraapi.db.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.dao.master.SequenceDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SequenceUtil {
	
	@Autowired
	protected SequenceDao sequenceDao;
	
	// Used during scanning
	public String getExtractedCode(Sequence sequence, String artifactName){
	    String sequenceCode = null;
		String artifactCodeRegex = sequence.getCodeRegex();
		if(artifactCodeRegex != null)
			sequenceCode = extractSequenceFromArtifactName(artifactName, artifactCodeRegex);
	
		return sequenceCode;
	}
	
	public String getExtractedSeqNum(Sequence sequence, String artifactName){
		String artifactNumberRegex = sequence.getNumberRegex();
		String extractedSeqNum = artifactNumberRegex != null ? extractSequenceFromArtifactName(artifactName, artifactNumberRegex) : null;
		return extractedSeqNum;
	}
	
	public String getSequenceCode(Sequence sequence, String artifactName) {
		return getSequenceCode(sequence, artifactName, null);
	}
	// Used when ingesting or by the processing framework when framing the outputartifactname for artifactclasses that produce outputArtifactclass  
	public String getSequenceCode(Sequence sequence, String artifactName, String overrideSequenceRefId) {
	    String sequenceCode = null;

		boolean useExtractedCode = sequence.isKeepCode();
	    if(useExtractedCode) { 
    		sequenceCode = getExtractedCode(sequence, artifactName); // if use extracted code, and code_regex matches then we dont want a new code
	    }
	    
	    if(StringUtils.isBlank(sequenceCode)) { // if keep_code is true but code_regex doesnt return a match...
	    	/*
	    	The code generation logic for artifacts is tried the following order:

	    		If number_regex returns a match value, use concat(prefix, value)
	    		Use concat(prefix, current_number + 1)
	    	*/
			String extractedSeqNum = getExtractedSeqNum(sequence, artifactName); 

			if(StringUtils.isNotBlank(extractedSeqNum)) // If number_regex returns a match value, use concat(prefix, value)
				sequenceCode = (StringUtils.isNotBlank(sequence.getPrefix()) ? sequence.getPrefix() : "") + extractedSeqNum;
			else {
					// generating the sequence
			    	synchronized (artifactName) {
			    		Integer incrementedCurrentNumber = null;
			    		Sequence sequenceRef = sequence.getSequenceRef();
			    		if(sequenceRef != null) {
			    			String sequenceRefId = sequenceRef.getId();
							// TODO - Explain this why needed... 
			    			if(overrideSequenceRefId != null) {
								sequenceRefId = overrideSequenceRefId;
							}

			    			sequenceRef = sequenceDao.findById(sequenceRefId).get();
			    			incrementedCurrentNumber = sequenceRef.incrementCurrentNumber();
			    			sequenceDao.save(sequenceRef);
			    		}
			    		else {
			    			incrementedCurrentNumber = sequence.incrementCurrentNumber();
			    			sequenceDao.save(sequence);
			    		}
			    		sequenceCode = (StringUtils.isNotBlank(sequence.getPrefix()) ? sequence.getPrefix() : "") + incrementedCurrentNumber;
			    	}
				}
	    }
		return sequenceCode;
	}	 
	
	private String extractSequenceFromArtifactName(String artifactName, String extractionRegex){
		String extractedSequenceFromArtifactName = null;
		
		Pattern p = Pattern.compile(extractionRegex);
		Matcher m = p.matcher(artifactName);  		
		if(m.find())
			extractedSequenceFromArtifactName = m.group();
		return extractedSequenceFromArtifactName;
	}
}
