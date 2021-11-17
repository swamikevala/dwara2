package org.ishafoundation.dwaraapi.api.resp.request;

public class Tape {
	String id;
	boolean loaded = false;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isLoaded() {
		return loaded;
	}
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
	
	

}
