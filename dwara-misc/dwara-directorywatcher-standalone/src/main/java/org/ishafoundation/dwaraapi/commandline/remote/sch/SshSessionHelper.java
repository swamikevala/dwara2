package org.ishafoundation.dwaraapi.commandline.remote.sch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SshSessionHelper {
	
	static Logger logger = LoggerFactory.getLogger(SshSessionHelper.class);
	
	// Create session ....
	public Session getSession(String host, String user, String prvKeyFileLocation) throws Exception {
		Session session = null;

		try {
			JSch.setConfig("StrictHostKeyChecking", "no");
	
			JSch jsch = new JSch();
			jsch.addIdentity(prvKeyFileLocation);
			session = jsch.getSession(user, host, 22);
			session.connect();	
		}
		catch (Exception e) {
			logger.error("Unable to create JSch session for " + user + ":" + host + " " + e.getMessage(), e);
			throw e;
		}
		return session;
	}
	
	public void disconnectSession(Session session) {
		session.disconnect();
		logger.debug("JSch session deleted :: " + session);
	}
}
