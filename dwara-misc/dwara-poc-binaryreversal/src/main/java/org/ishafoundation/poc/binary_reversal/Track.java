package org.ishafoundation.poc.binary_reversal;

import com.google.code.ebmlviewer.core.VariableLengthInteger;

public class Track {
	
	private int id;
	
	private VariableLengthInteger size;

	private String data;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public VariableLengthInteger getSize() {
		return size;
	}

	public void setSize(VariableLengthInteger size) {
		this.size = size;
	}
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
//	private char[] data;
//
//	public char[] getData() {
//		return data;
//	}
//
//	public void setData(char[] data) {
//		this.data = data;
//	}

}
