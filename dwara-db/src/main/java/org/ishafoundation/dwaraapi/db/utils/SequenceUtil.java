package org.ishafoundation.dwaraapi.db.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.dao.master.SequenceDao;
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
	
	// Used when ingesting or by the processing framework when framing the outputartifactname for artifactclasses that produce outputArtifactclass  
	public String getSequenceCode(Sequence sequence, String artifactName) {
	    String sequenceCode = null;

		boolean useExtractedCode = sequence.isKeepCode();
	    if(useExtractedCode) { 
	    	String artifactCodeRegex = sequence.getCodeRegex();
	    	
	    	if(artifactCodeRegex != null) // if use extracted code, and code_regex matches then we dont want a new code
	    		sequenceCode = extractSequenceFromArtifactName(artifactName, artifactCodeRegex);
	    }
	    
	    if(StringUtils.isBlank(sequenceCode)) { // if keep_code is true but code_regex doesnt return a match...
	    	/*
	    	The code generation logic for artifacts is tried the following order:

	    		If number_regex returns a match value, use concat(prefix, value)
	    		Use concat(prefix, current_number + 1)
	    	*/
			String artifactNumberRegex = sequence.getNumberRegex();
			String extractedSeqNum = artifactNumberRegex != null ? extractSequenceFromArtifactName(artifactName, artifactNumberRegex) : null; 

			if(StringUtils.isNotBlank(extractedSeqNum)) // If number_regex returns a match value, use concat(prefix, value)
				sequenceCode = (StringUtils.isNotBlank(sequence.getPrefix()) ? sequence.getPrefix() : "") + extractedSeqNum;
			else {
				if(sequence.getForceMatch() != null && sequence.getForceMatch()) { // if true means supposed-to/should/must already have sequence in it, and shouldnt generate the sequence
					sequenceCode = null;
				}
				else { // generating the sequence
			    	synchronized (artifactName) {
			    		Integer incrementedCurrentNumber = null;
			    		if(sequence.getSequenceRef() != null) {
			    			incrementedCurrentNumber = sequence.getSequenceRef().incrementCurrentNumber();
			    			sequenceDao.save(sequence.getSequenceRef());
			    		}
			    		else {
			    			incrementedCurrentNumber = sequence.incrementCurrentNumber();
			    			sequenceDao.save(sequence);
			    		}
			    		sequenceCode = (StringUtils.isNotBlank(sequence.getPrefix()) ? sequence.getPrefix() : "") + incrementedCurrentNumber;
			    	}
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
