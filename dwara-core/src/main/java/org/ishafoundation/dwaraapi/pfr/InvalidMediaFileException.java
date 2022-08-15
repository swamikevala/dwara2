package org.ishafoundation.dwaraapi.pfr;

public class InvalidMediaFileException extends IllegalArgumentException {

	private static final long serialVersionUID = -5969059997604635531L;

	public InvalidMediaFileException(String msg) {
		super(msg);
	}
	
	public InvalidMediaFileException() {
		super();
	}
}
