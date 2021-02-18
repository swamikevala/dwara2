package org.ishafoundation.dwara.misc.remote;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwara.misc.commandline.remote.sch.RemoteCommandLineExecuter;
import org.ishafoundation.dwara.misc.commandline.remote.sch.SshSessionHelper;
import org.ishafoundation.dwara.misc.common.Constants;
import org.ishafoundation.dwara.misc.common.MoveUtil;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.commandline.remote.scp.SecuredCopier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Session;

public class RemoteSpaceCheckerAndFileMover {
	
	private static Logger logger = LoggerFactory.getLogger(RemoteSpaceCheckerAndFileMover.class);
	
	// java org.ishafoundation.dwara.misc.RemoteSpaceCheckerAndFileMover "172.18.1.200" "dwara" "/opt/dwara/.ssh/id_rsa" 1 3000 "/data/prasad-staging/completed" "/data/prasad-staging-test-213"
	private static void usage() {
		System.err.println("usage: java org.ishafoundation.dwara.misc.remote.RemoteSpaceCheckerAndFileMover "
				+ "remoteServerIP "
				+ "remoteServerSshUserName "
				+ "localServerPubKeyLocation "
				+ "thresholdSizeInGB "
				+ "localWatchedDirLocation "
				+ "remoteWatchedDirLocation");
		
		System.err.println("where,");
		System.err.println("args[0] - remoteServerIP - The host IP to which files are to be copied");
		System.err.println("args[1] - remoteServerSshUserName - The ssh username to be used to connect the remote host");
		System.err.println("args[2] - localServerPubKeyLocation - The key to connect to remote host");
		System.err.println("args[3] - thresholdSizeInGB - Free space threshold. If remote server has < this value we should not copy");
		System.err.println("args[4] - localSystemDirLocation - The directory where sub directories like \"Validated\", \"Copied\", \"CopyFailed\" will be");
		System.err.println("args[5] - remoteWatchedDirLocation - The directory watched by the remote servers watcher");
		
		System.err.println("e.g.,");
		System.err.println("cd /opt/dwara/bin; nohup java -cp dwara-watcher-2.0.jar -Dlogback.configurationFile=logback-filemover.xml org.ishafoundation.dwara.misc.remote.RemoteSpaceCheckerAndFileMover \"172.18.1.213\" \"dwara\" \"/opt/dwara/.ssh/id_rsa\" 1 3000 \"/data/prasad-staging\" \"/data/prasad-staging\"&");
		System.exit(-1);
	}

	
	public static void main(String[] args) {
		
		// parse arguments
		if (args.length != 7)
			usage();

		String host = args[0]; // remote server ip
        String sshUser = args[1]; // remote server sshUsername
        String prvKeyFileLocation = args[2]; // local server' pub key location in local
		long waitTimesInMilliSec = Integer.parseInt(args[3]) * 60 * 1000; // loop interval in minutes
        int configuredThresholdInGB = Integer.parseInt(args[4]); //Threshold size in GB- eg., 6144 for 6TB
        String localSystemDirLocation = args[5]; // local server root dir location /data/prasad-staging
        String remoteServerDirLocation = args[6]; // Remote server watcher location /data/prasad-staging

        
        
        String copiedDirLocation = Paths.get(localSystemDirLocation, Constants.copiedDirName).toString();
        String validatedDirLocation = Paths.get(localSystemDirLocation, Constants.validatedDirName).toString();
        String copyFailedDirLocation = Paths.get(localSystemDirLocation, Constants.copyFailedDirName).toString();
        
		for (;;) { //infinite loop
			File copiedDirObj = new File(copiedDirLocation);
			File[] copiedFiles = {};
			if(copiedDirObj.exists())
				copiedFiles = copiedDirObj.listFiles();
			
			File validatedDirObj = new File(validatedDirLocation);
			File[] validatedFiles = {};
			if(validatedDirObj.exists())
				validatedFiles = validatedDirObj.listFiles();
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
				        		if(artifactDirectory.getAbsolutePath().equals(copiedDirLocation))
				        			continue;
				        		String artifactName = artifactDirectory.getName();
				        		
				        		ChecksumStatus chkSumStatus = isChecksumValidatedInIngest(remoteCommandLineExecuter, jSchSession, artifactName);
				        		if(chkSumStatus != null) {
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
				        	}
				        }catch (Exception e) {
				        	logger.error("Unable to check if chksum validated on ingest " + e.getMessage(), e);
				        }
		        	}
		        	
			        if(validatedFiles.length > 0) {
			        	logger.info("Checking space and scp-ing the validated files now");
			        	try {
				        	for (File artifactDirectory : validatedFiles) {
				        		if(artifactDirectory.getAbsolutePath().equals(copiedDirLocation))
				        			continue;
				        		
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
    	String command1 = Constants.validatorScriptPathname + " " + artifactName;
		try {
			logger.trace("executing remotely " + command1);
			CommandLineExecutionResponse commandLineExecutionResponse = remoteCommandLineExecuter.executeCommandRemotelyOnServer(jSchSession, command1, "ingestServerChkSumCompletion.out_mv_qcErr");
			
			if(commandLineExecutionResponse.isComplete()) {
				String stdOutResponseAsString = commandLineExecutionResponse.getStdOutResponse().trim();
				
				try {
					return ChecksumStatus.valueOf(stdOutResponseAsString);	
				}catch (Exception e) {
					logger.warn("Not expected response " + stdOutResponseAsString);
					return null;
				}
			}else {
				throw new Exception(commandLineExecutionResponse.getFailureReason());
			}

        }catch (Exception e) {
        	logger.error("Unable to execute " + command1 + " remotely" + e.getMessage(), e);
        	throw e;
		}
	}
	
	private static int checkSpaceInIngest(RemoteCommandLineExecuter remoteCommandLineExecuter, Session jSchSession) throws Exception{
    	String command1 = Constants.spaceScriptPathname;
		try {
			logger.trace("executing remotely " + command1);
			CommandLineExecutionResponse commandLineExecutionResponse = remoteCommandLineExecuter.executeCommandRemotelyOnServer(jSchSession, command1, "ingestServerSpace.out_mv_qcErr");
			if(commandLineExecutionResponse.isComplete()) {
				String spaceAsString = commandLineExecutionResponse.getStdOutResponse().trim();
				return Integer.parseInt(spaceAsString);
			}else {
				throw new Exception(commandLineExecutionResponse.getFailureReason());
			}

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
