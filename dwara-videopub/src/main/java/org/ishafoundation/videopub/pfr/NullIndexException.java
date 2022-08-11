package org.ishafoundation.videopub.pfr;

public class NullIndexException extends Exception {

	private static final long serialVersionUID = 7129912158826730575L;

	public NullIndexException(String msg) {
		super(msg);
	}
	
	public NullIndexException() {
		super();
	}
}
