package org.ishafoundation.videopub.mam.core;

import org.apache.http.impl.cookie.BasicClientCookie;
import org.ishafoundation.videopub.mam.CatDVConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

public class CatDVInteractor {
	
	@Autowired
	private CatDVConfiguration catdvConfiguration;
	
	protected String getURLPrefix(){
    	boolean isSecured = catdvConfiguration.isSecured();
    	String protocol =  isSecured ? "https" : "http";
    	String hostname = catdvConfiguration.getHost(); 
    	String port = catdvConfiguration.getPort();
    	
    	return  protocol + "://" + hostname + ":" + port;
    } 

    protected String getCatDVUserID(){
		return catdvConfiguration.getWebUserID();
	}
	
    protected String getCatDVUserPwd() {
		return catdvConfiguration.getWebUserPwd();
	}
    
    protected BasicClientCookie getCookie(String jsessionId) {
	    BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", jsessionId);
	    cookie.setDomain(catdvConfiguration.getHost());
	    cookie.setPath("/");
	    return cookie;
	}
}
