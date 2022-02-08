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

	public synchronized String generateSequenceCode(Sequence sequence, String artifactName) {
		Integer incrementedCurrentNumber = null;
		Sequence sequenceRef = sequence.getSequenceRef();
		if(sequenceRef != null) {
			String sequenceRefId = sequenceRef.getId();

			sequenceRef = sequenceDao.findById(sequenceRefId).get();
			incrementedCurrentNumber = sequenceRef.incrementCurrentNumber();
			sequenceDao.save(sequenceRef);
		}
		else {
			sequence = sequenceDao.findById(sequence.getId()).get(); // get the sequence from DB again so its thread safe[even if other thread had got that object with same value]
			incrementedCurrentNumber = sequence.incrementCurrentNumber();
			sequenceDao.save(sequence);
		}
		String sequenceCode = (StringUtils.isNotBlank(sequence.getPrefix()) ? sequence.getPrefix() : "") + incrementedCurrentNumber;

		return sequenceCode;
	}	 
}
