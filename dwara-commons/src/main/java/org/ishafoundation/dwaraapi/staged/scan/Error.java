package org.ishafoundation.dwaraapi.staged.scan;

public class Error {
	private Errortype type;
	private String message;

	public Errortype getType() {
		return type;
	}
	public void setType(Errortype type) {
		this.type = type;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return type.name() + ":" + message;
	}
}
