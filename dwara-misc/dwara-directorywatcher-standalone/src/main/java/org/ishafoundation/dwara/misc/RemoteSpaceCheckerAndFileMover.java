package org.ishafoundation.dwara.misc;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.commandline.remote.sch.RemoteCommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.remote.sch.SshSessionHelper;
import org.ishafoundation.dwaraapi.commandline.remote.scp.SecuredCopier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Session;

public class RemoteSpaceCheckerAndFileMover {
	
	private static Logger logger = LoggerFactory.getLogger(RemoteSpaceCheckerAndFileMover.class);
	
	// java org.ishafoundation.dwara.misc.RemoteSpaceCheckerAndFileMover "172.18.1.200" "dwara" "/opt/dwara/.ssh/id_rsa" 1 3000 "/data/prasad-staging/completed" "/data/prasad-staging-test-213"
	
	public static void main(String[] args) throws InterruptedException {
		for (;;) { //infinite loop
			SshSessionHelper sshSessionHelper = new SshSessionHelper();
			RemoteCommandLineExecuter remoteCommandLineExecuter = new RemoteCommandLineExecuter();
	        // now moving back the file from the .copying to the original destination...
	        Session jSchSession = null;
        
	        String host = args[0]; // remote server ip
	        String sshUser = args[1]; // remote server sshUsername
	        String prvKeyFileLocation = args[2]; // remote server key location
			long waitTimesInMilliSec = Integer.parseInt(args[3]) * 60 * 1000; // loop interval in minutes
	        int configuredThresholdInGB = Integer.parseInt(args[4]); //Threshold size in GB- eg., 6144 for 6TB
	        String completedDirLocation = args[5]; // local server completed dir location /data/prasad-staging/completed
	        String remoteServerDirLocation = args[6]; // Remote server watcher location /data/prasad-staging
	        
	        try {
	        	jSchSession = sshSessionHelper.getSession(host, sshUser, prvKeyFileLocation);
	        	
	        	int availableSpaceInRemoteServerInGB = checkSpaceInIngest(remoteCommandLineExecuter, jSchSession);
				
	        	if(availableSpaceInRemoteServerInGB > configuredThresholdInGB) {
	        		for (File artifactDirectory : new File(completedDirLocation).listFiles()) {
	        			if(artifactDirectory.isDirectory()) {
	        				String remoteLocation = remoteServerDirLocation + File.separator + artifactDirectory.getName();
	        				String command1 = "mkdir -p \"" + remoteLocation + "\"";
	        				remoteCommandLineExecuter.executeCommandRemotelyOnServer(jSchSession, command1, artifactDirectory.getName() + ".out_mkdir");

	        				for (File file : artifactDirectory.listFiles()) {
		        				moveFileToIngest(jSchSession, file.getAbsolutePath(), remoteLocation + File.separator + file.getName());
	        				}
	        				FileUtils.deleteDirectory(artifactDirectory);
		        			break;
	        			}
	        		}
	        		
//	        		Collection<File> artifactList = FileUtils.listFilesAndDirs(new File(completedDirLocation), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
//	        		for (File file : artifactList) {
//	        			if(!file.isDirectory()) {
//		        			moveFileToIngest(jSchSession, file.getAbsolutePath(), remoteServerDirLocation + File.separator + file.getName());
//	        			}
//					}
//	        		for (File file : artifactList) {
//	        			FileUtils.deleteDirectory(file);
//	        			break;
//	        		}
	        	}else
	        		logger.info("Not enough space in remote server " + availableSpaceInRemoteServerInGB);
				
	        }catch (Exception e) {
	        	logger.error("Unable to check space and move file to ingest remotely" + e.getMessage(), e);
			}finally {
				if (jSchSession != null) 
					sshSessionHelper.disconnectSession(jSchSession);
			}
	        
	        Thread.sleep(waitTimesInMilliSec);
		}    
		
		
		
	}

	private static int checkSpaceInIngest(RemoteCommandLineExecuter remoteCommandLineExecuter, Session jSchSession) throws Exception{
    	String command1 = "/opt/dwara/bin/space";
		try {
			logger.trace("executing remotely " + command1);
			CommandLineExecutionResponse commandLineExecutionResponse = remoteCommandLineExecuter.executeCommandRemotelyOnServer(jSchSession, command1, "ingestServerSpace.out_mv_qcErr");
			
			String spaceAsString = commandLineExecutionResponse.getStdOutResponse().trim();
			logger.trace("spaceAsString " + spaceAsString);
			return Integer.parseInt(spaceAsString);
				
        }catch (Exception e) {
        	logger.error("Unable to execute " + command1 + " remotely" + e.getMessage(), e);
        	throw e;
		}
	}

	private static void moveFileToIngest(Session session, String localFilePath, String remoteFilePath) throws Exception {
		SecuredCopier securedCopier = new SecuredCopier();
		try {
			securedCopier.copyTo(session, localFilePath, remoteFilePath);
		}catch (Exception e) {
			logger.error("Unable to remote copy " + localFilePath + " to " + remoteFilePath + " " +  e.getMessage(), e);
			throw e;
		}
	}
}
