package org.ishafoundation.dwaraapi.db.utils;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.dao.master.SequenceDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SequenceUtil {
	
	@Autowired
	protected SequenceDao sequenceDao;

	public String generateSequenceCode(Sequence sequence, String artifactName) {
		return getSequenceCode(sequence, artifactName, null);
	}
	
	// Used when ingesting or by the processing framework when framing the outputartifactname for artifactclasses that produce outputArtifactclass  
	public String getSequenceCode(Sequence sequence, String artifactName, String overrideSequenceRefId) {
	    String sequenceCode = null;

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
    			sequence = sequenceDao.findById(sequence.getId()).get(); // get the sequence from DB again so its thread safe[even if other thread had got that object with same value]
    			incrementedCurrentNumber = sequence.incrementCurrentNumber();
    			sequenceDao.save(sequence);
    		}
    		sequenceCode = (StringUtils.isNotBlank(sequence.getPrefix()) ? sequence.getPrefix() : "") + incrementedCurrentNumber;
    	}
		return sequenceCode;
	}	 
}
