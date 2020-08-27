package org.ishafoundation.dwaraapi.ltowala.api.resp;

public class Volume {
	private String barcode;
	private int startBlock;
	
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public int getStartBlock() {
		return startBlock;
	}
	public void setStartBlock(int startBlock) {
		this.startBlock = startBlock;
	}
}
