package org.ishafoundation.dwaraapi.db.utils;

import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;

public class SequenceUtil {
	
	public static Integer incrementCurrentNumber(Sequence sequence) {
		Integer currentNumber = null;
		if(sequence.getSequenceRef() != null) {
			currentNumber = sequence.getSequenceRef().incrementCurrentNumber();
		}
		else {
			currentNumber = sequence.incrementCurrentNumber();
		}
		return currentNumber;
	}
}
