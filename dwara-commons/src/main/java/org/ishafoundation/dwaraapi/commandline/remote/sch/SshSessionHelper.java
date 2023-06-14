package org.ishafoundation.dwaraapi.commandline.remote.sch;

import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

@Component
public class SshSessionHelper {
	
	@Autowired
	private Configuration configuration;
	
	static Logger logger = LoggerFactory.getLogger(SshSessionHelper.class);

	public Session getSession(String host, String user) throws Exception {
		Session session = null;
		
		logger.trace("host " + host);
		logger.trace("user " + user);
				
		try {
			JSch.setConfig("StrictHostKeyChecking", "no");
	
			JSch jsch = new JSch();
			jsch.addIdentity(configuration.getSshPrvKeyFileLocation());
			session = jsch.getSession(user, host, 22);			
			session.connect();	
		}
		catch (Exception e) {
			String reason = "Unable to create JSch session for " + user + ":" + host;
			logger.error(reason + " " + e.getMessage(), e);
			throw e;
		}
		return session;
	}
	
	// Create session ....
	public Session getSession(String host, String user, String prvKeyFileLocation) {
		Session session = null;
		logger.trace("host " + host);
		logger.trace("user " + user);
		logger.trace("prvKeyFileLocation " + prvKeyFileLocation);
		try {
			JSch.setConfig("StrictHostKeyChecking", "no");
	
			JSch jsch = new JSch();
			jsch.addIdentity(prvKeyFileLocation);
			session = jsch.getSession(user, host, 22);
			session.connect();	
		}
		catch (Exception e) {
			logger.error("Unable to create JSch session for " + user + ":" + host + " " + e.getMessage(), e);
		}
		return session;
	}
	
	public void disconnectSession(Session session) {
		session.disconnect();
		logger.debug("JSch session deleted :: " + session);
	}
}
