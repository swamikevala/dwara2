package org.ishafoundation.dwara.misc.remote;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwara.misc.common.MoveUtil;
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
	
	public static void main(String[] args) {
        
		String host = args[0]; // remote server ip
        String sshUser = args[1]; // remote server sshUsername
        String prvKeyFileLocation = args[2]; // remote server' pub key location in local
		long waitTimesInMilliSec = Integer.parseInt(args[3]) * 60 * 1000; // loop interval in minutes
        int configuredThresholdInGB = Integer.parseInt(args[4]); //Threshold size in GB- eg., 6144 for 6TB
        String localDirLocation = args[5]; // local server root dir location /data/prasad-staging
        String remoteServerDirLocation = args[6]; // Remote server watcher location /data/prasad-staging

        
        String copiedDirName = "copied";
        String copiedDirLocation = Paths.get(localDirLocation, copiedDirName).toString();
        String validatedDirName = "validated";
        String validatedDirLocation = Paths.get(localDirLocation, validatedDirName).toString();
        String copyFailedDirName = "copyfailed";
        String copyFailedDirLocation = Paths.get(localDirLocation, copyFailedDirName).toString();
        
		for (;;) { //infinite loop
			File[] copiedFiles = new File(copiedDirLocation).listFiles();
			File[] validatedFiles = new File(validatedDirLocation).listFiles();
			if(copiedFiles.length > 0 || validatedFiles.length > 0) {
				SshSessionHelper sshSessionHelper = new SshSessionHelper();
				RemoteCommandLineExecuter remoteCommandLineExecuter = new RemoteCommandLineExecuter();
		        Session jSchSession = null;
		        
		        try {
		        	jSchSession = sshSessionHelper.getSession(host, sshUser, prvKeyFileLocation);
		        	
		        	if(copiedFiles.length > 0) {
			        	logger.info("Deleting copied files ready for deletion");
				        try {	
				        	for (File artifactDirectory : copiedFiles) {
				        		String artifactName = artifactDirectory.getName();
				        		ChecksumStatus chkSumStatus = isChecksumValidatedInIngest(remoteCommandLineExecuter, jSchSession, artifactName);
				        		switch (chkSumStatus) {
									case valid:
										FileUtils.deleteDirectory(artifactDirectory);
										logger.info("Deleted succesfully " + artifactName);
										break;
									case invalid:
										logger.warn("Copy and validate chksum failed " + artifactName + ". Moving " + artifactName + " to copyfailed dir");
										MoveUtil.move(artifactDirectory.toPath(), Paths.get(artifactDirectory.getAbsolutePath().replace(copiedDirLocation, copyFailedDirLocation)));
										break;
									case pending:
										break;
								}
				        	}
				        }catch (Exception e) {
				        	logger.error("Unable to check if chksum validated on ingest " + e.getMessage(), e);
				        }
		        	}
		        	
			        if(validatedFiles.length > 0) {
			        	logger.info("Checking space and scp-ing the validated files now");
			        	try {
				        	for (File artifactDirectory : validatedFiles) {
				        		logger.info("Space check");
					        	int availableSpaceInRemoteServerInGB = checkSpaceInIngest(remoteCommandLineExecuter, jSchSession);
								
					        	if(availableSpaceInRemoteServerInGB > configuredThresholdInGB) {
					        		logger.info("Space ok");
				        			if(artifactDirectory.isDirectory()) {
				        				String remoteLocation = remoteServerDirLocation + File.separator + artifactDirectory.getName();
				        				String command1 = "mkdir -p \"" + remoteLocation + "\"";
				        				remoteCommandLineExecuter.executeCommandRemotelyOnServer(jSchSession, command1, artifactDirectory.getName() + ".out_mkdir");
				        				try {
							        		logger.info("Scp-ing " + artifactDirectory.getName());
					        				for (File file : artifactDirectory.listFiles()) {
				        						copyFileToIngest(jSchSession, file.getAbsolutePath(), remoteLocation + File.separator + file.getName());
					        				}
					        				logger.info("Scp complete " + artifactDirectory.getName());
					        				MoveUtil.move(artifactDirectory.toPath(), Paths.get(artifactDirectory.getAbsolutePath().replace(validatedDirLocation, copiedDirLocation)));
				        				}catch (Exception e) {
											logger.error("Unable to scp " + artifactDirectory.getName() + ":" +  e.getMessage() , e);
										}
				        			}
					        	}else {
					        		logger.warn("Not enough space in remote server " + availableSpaceInRemoteServerInGB);
					        		break; // If enough space isnt there means subsequent artifacts too will not have space. So break the loop and let it try the next schedule... 
					        	}
				        	}
				        }catch (Exception e) {
				        	logger.error("Unable to check space and move file to ingest remotely" + e.getMessage(), e);
				        }
			        }
				} catch (Exception e1) {
					logger.error("Unable to create jsch session " + e1.getMessage() , e1);
				}finally {
					if (jSchSession != null) 
						sshSessionHelper.disconnectSession(jSchSession);
				}
			}
			
			logger.trace("Sleeping for " + waitTimesInMilliSec);
	        try {
				Thread.sleep(waitTimesInMilliSec);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
	        logger.trace("Awake");
		}    
	}
	
	private static ChecksumStatus isChecksumValidatedInIngest(RemoteCommandLineExecuter remoteCommandLineExecuter, Session jSchSession, String artifactName) throws Exception{
    	String command1 = "/opt/dwara/bin/digi-valid " + artifactName;
		try {
			logger.trace("executing remotely " + command1);
			CommandLineExecutionResponse commandLineExecutionResponse = remoteCommandLineExecuter.executeCommandRemotelyOnServer(jSchSession, command1, "ingestServerChkSumCompletion.out_mv_qcErr");
			
			String stdOutResponseAsString = commandLineExecutionResponse.getStdOutResponse().trim();
			return ChecksumStatus.valueOf(stdOutResponseAsString);
        }catch (Exception e) {
        	logger.error("Unable to execute " + command1 + " remotely" + e.getMessage(), e);
        	throw e;
		}
	}
	
	private static int checkSpaceInIngest(RemoteCommandLineExecuter remoteCommandLineExecuter, Session jSchSession) throws Exception{
    	String command1 = "/opt/dwara/bin/space";
		try {
			logger.trace("executing remotely " + command1);
			CommandLineExecutionResponse commandLineExecutionResponse = remoteCommandLineExecuter.executeCommandRemotelyOnServer(jSchSession, command1, "ingestServerSpace.out_mv_qcErr");
			
			String spaceAsString = commandLineExecutionResponse.getStdOutResponse().trim();
			return Integer.parseInt(spaceAsString);
        }catch (Exception e) {
        	logger.error("Unable to execute " + command1 + " remotely" + e.getMessage(), e);
        	throw e;
		}
	}

	private static void copyFileToIngest(Session session, String localFilePath, String remoteFilePath) throws Exception {
		SecuredCopier securedCopier = new SecuredCopier();
		try {
			securedCopier.copyTo(session, localFilePath, remoteFilePath);
		}catch (Exception e) {
			logger.error("Unable to remote copy " + localFilePath + " to " + remoteFilePath + " " +  e.getMessage(), e);
			throw e;
		}
	}
}
