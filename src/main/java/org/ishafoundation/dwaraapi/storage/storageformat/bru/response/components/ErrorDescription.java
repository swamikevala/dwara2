package org.ishafoundation.dwaraapi.storage.storageformat.bru.response.components;

public class ErrorDescription {
	private String code;
	private String desc;
	

	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	} 
	@Override
	public String toString() {
		return "code : " + code + " desc : " + desc;
	}
	
}
