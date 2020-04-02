package org.ishafoundation.dwaraapi.entrypoint.resource;

public class CommonResponse {
	
	private Long totalNoOfRecords;
	
	private int pageNumber; // pageNumber that is getting shown now
	
	
	public Long getTotalNoOfRecords() {
		return totalNoOfRecords;
	}

	public void setTotalNoOfRecords(Long totalNoOfRecords) {
		this.totalNoOfRecords = totalNoOfRecords;
	}
	
	public int getPageNumber() {
		return pageNumber;
	}
	
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
}
