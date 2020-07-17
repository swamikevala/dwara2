package org.ishafoundation.videopub.mam.sch;

import org.ishafoundation.dwaraapi.commandline.remote.sch.SshSessionHelper;
import org.ishafoundation.videopub.mam.CatDVConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.Session;

@Component
public class CatdvSshSessionHelper extends SshSessionHelper{

	@Autowired
	private CatDVConfiguration catdvConfiguration;
	
	static Logger logger = LoggerFactory.getLogger(CatdvSshSessionHelper.class);
	
	// Create session ....
	public Session getSession() throws Exception {
		String host = catdvConfiguration.getHost();
		String user = catdvConfiguration.getSshSystemUser();
		return getSession(host, user);
	}

}
