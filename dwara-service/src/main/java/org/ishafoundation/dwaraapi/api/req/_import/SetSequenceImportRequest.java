package org.ishafoundation.dwaraapi.api.req._import;

public class SetSequenceImportRequest extends ImportRequest{
	
	private Integer startingNumber; // input base startingNumber

	public Integer getStartingNumber() {
		return startingNumber;
	}

	public void setStartingNumber(Integer startingNumber) {
		this.startingNumber = startingNumber;
	}
}
