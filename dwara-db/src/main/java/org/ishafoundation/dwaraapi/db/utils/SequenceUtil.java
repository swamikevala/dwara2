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
	
	
	public String getExtractedCode(Sequence sequence, String artifactName){
	    String sequenceCode = null;
		String artifactCodeRegex = sequence.getArtifactCodeRegex();
		String artifactNumberRegex = sequence.getArtifactNumberRegex();
		if(artifactNumberRegex != null)
			sequenceCode = (StringUtils.isNotBlank(sequence.getPrefix()) ? sequence.getPrefix() : "") + extractSequenceFromArtifactName(artifactName, artifactNumberRegex);
		else if(artifactCodeRegex != null)
			sequenceCode = extractSequenceFromArtifactName(artifactName, artifactCodeRegex);
	
		return sequenceCode;
	}
	
	public String getSequenceCode(Sequence sequence, String artifactName) {
	    boolean useExtractedCode = sequence.isArtifactKeep();
	    
	    String sequenceCode = null;
	    if(useExtractedCode) {
	    	sequenceCode = getExtractedCode(sequence, artifactName);
	    }
	    else { // if using extracted code, then we dont want a new code
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
