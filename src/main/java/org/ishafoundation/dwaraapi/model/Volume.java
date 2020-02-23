package org.ishafoundation.dwaraapi.model;

import org.ishafoundation.dwaraapi.db.model.master.storage.Tape;

public class Volume {

	private Tape tape;

	public Tape getTape() {
		return tape;
	}

	public void setTape(Tape tape) {
		this.tape = tape;
	}
}
