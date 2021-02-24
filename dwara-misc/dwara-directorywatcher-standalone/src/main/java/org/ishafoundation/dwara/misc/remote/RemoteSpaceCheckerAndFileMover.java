package org.ishafoundation.dwara.misc.remote;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwara.misc.commandline.remote.sch.RemoteCommandLineExecuter;
import org.ishafoundation.dwara.misc.commandline.remote.sch.SecuredCopier;
import org.ishafoundation.dwara.misc.commandline.remote.sch.SshSessionHelper;
import org.ishafoundation.dwara.misc.common.Constants;
import org.ishafoundation.dwara.misc.common.MoveUtil;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuterImpl;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Session;

public class RemoteSpaceCheckerAndFileMover {
	
	private static Logger logger = LoggerFactory.getLogger(RemoteSpaceCheckerAndFileMover.class);
	
	private static boolean isScp = false;
	private static boolean overSshSession = false;
	private static int scpBufferSize = 1024;
	private static boolean encryptSsh = false;
	
	private static void usage() {
		System.err.println("usage: java org.ishafoundation.dwara.misc.remote.RemoteSpaceCheckerAndFileMover "
				+ "localSystemDirLocation "
				+ "pollingIntervalInSecs "
				+ "remoteServerIP "
				+ "remoteServerSshUserName "
				+ "localServerPrivKeyLocation "
				+ "thresholdSizeInGB "
				+ "remoteWatchedDirLocation "
				+ "isScp "
				+ "overSshSession "
				+ "encryptSsh "
				+ "scpBufferSize");
		
		System.err.println("where,");
		System.err.println("args[0] - localSystemDirLocation - The directory where sub directories like \"Validated\", \"Copied\", \"CopyFailed\" will be");
		System.err.println("args[1] - pollingIntervalInSecs - The polling interval to check if there are artifacts ready for scp-ing or deleting");
		System.err.println("args[2] - remoteServerIP - The host IP to which files are to be copied");
		System.err.println("args[3] - remoteServerSshUserName - The ssh username to be used to connect the remote host");
		System.err.println("args[4] - localServerPrivKeyLocation - The key to connect to remote host");
		System.err.println("args[5] - thresholdSizeInGB - Free space threshold. If remote server has < this value we should not copy");
		System.err.println("args[6] - remoteWatchedDirLocation - The directory watched by the remote servers watcher");
		System.err.println("args[7] - isScp - Boolean - Is file transfer using Scp or using nc");
		System.err.println("args[8] - overSshSession - Boolean - Scp over a ssh session created? Else will use native passwordless ssh");
		System.err.println("args[9] - encryptSsh - Boolean - Should the ssh connection be encrypted");
		System.err.println("args[10] - scpBufferSize - Buffer size in bytes needed for reading stream for scp-ing, if above is false not effective");
		
		
		
		System.err.println("e.g.,");
		System.err.println("cd /opt/dwara/bin; nohup java -cp dwara-watcher-2.0.jar -Dlogback.configurationFile=logback-filemover.xml org.ishafoundation.dwara.misc.remote.RemoteSpaceCheckerAndFileMover \"/data/prasad-staging\" 60 \"172.18.1.213\" \"dwara\" \"/opt/dwara/.ssh/id_rsa\" 3000  \"/data/prasad-staging\" true true false 1024&");
		System.exit(-1);
	}
	
	public static void main(String[] args) {
		// parse arguments
		if (args.length != 11)
			usage();

		String localSystemDirLocation = args[0]; // local server root dir location /data/prasad-staging
		long waitTimesInMilliSec = Long.parseLong(args[1]) * 1000; // loop interval in secs
		String host = args[2]; // remote server ip
        String sshUser = args[3]; // remote server sshUsername
        String prvKeyFileLocation = args[4]; // local server' priv key location
        int configuredThresholdInGB = Integer.parseInt(args[5]); //Threshold size in GB- eg., 6144 for 6TB
        String remoteServerDirLocation = args[6]; // Remote server watcher location /data/prasad-staging
        isScp = Boolean.parseBoolean(args[7]);
        overSshSession = Boolean.parseBoolean(args[8]); // Is scp to be done over a ssh session created or using passwordless ssh
        encryptSsh = Boolean.parseBoolean(args[9]); // should the ssh connection be encrypted
        scpBufferSize = Integer.parseInt(args[10]); // bufferSize
        
        
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
		        	
		        	if(!encryptSsh) {
			        	jSchSession.setConfig("cipher.s2c", "none,aes128-cbc,3des-cbc,blowfish-cbc");
			        	jSchSession.setConfig("cipher.c2s", "none,aes128-cbc,3des-cbc,blowfish-cbc");
		        	}
		        	
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
				        				/*
				        				String command1 = "mkdir -p \"" + remoteLocation + "\"";
				        				remoteCommandLineExecuter.executeCommandRemotelyOnServer(jSchSession, command1, artifactDirectory.getName() + ".out_mkdir");
				        				*/
				        				try {
				        					if(isScp) {
								        		logger.info("Scp-ing " + artifactDirectory.getName());
								        		if(!overSshSession) {
									        		CommandLineExecuterImpl clei = new CommandLineExecuterImpl();
									        		try {
									        			List<String> scpCommandParamsList = new ArrayList<String>();
									        			scpCommandParamsList.add("scp");
									        			scpCommandParamsList.add("-i");
									        			scpCommandParamsList.add(prvKeyFileLocation);
									        			scpCommandParamsList.add("-pr");
									        			scpCommandParamsList.add(artifactDirectory.getAbsolutePath());
									        			scpCommandParamsList.add(sshUser + "@" + host + ":" + remoteLocation);
									        			clei.executeCommand(scpCommandParamsList, false);
									        		} catch (Exception e) {
									        			logger.error(e.getMessage(), e);
									        		}
								        		}
								        		else {
							        				for (File file : artifactDirectory.listFiles()) {
						        						copyFileToIngest(jSchSession, file.getAbsolutePath(), remoteLocation + File.separator + file.getName());
							        				}
								        		}
						        				logger.info("Scp complete " + artifactDirectory.getName());
					        				}
				        					else {
				        						CommandLineExecuterImpl clei = new CommandLineExecuterImpl();
								        		try {
								        			List<String> ncCommandParamsList = new ArrayList<String>();
								        			ncCommandParamsList.add("sh");
								        			ncCommandParamsList.add("-c");
								        			ncCommandParamsList.add("cd");
								        			ncCommandParamsList.add(validatedDirLocation);
								        			ncCommandParamsList.add(";");
								        			ncCommandParamsList.add("tar");
								        			ncCommandParamsList.add("-cf");
								        			ncCommandParamsList.add(artifactDirectory.getName());
								        			ncCommandParamsList.add("|");
								        			ncCommandParamsList.add("nc");
								        			ncCommandParamsList.add("-v");
								        			ncCommandParamsList.add(host);
								        			ncCommandParamsList.add("7000");
								        			CommandLineExecutionResponse cer = clei.executeCommand(ncCommandParamsList, false);
								        			if(cer.isComplete())
								        				logger.info("Copy complete " + artifactDirectory.getName());
								        			else
								        				throw new Exception("Copy failed " + ncCommandParamsList);
								        			
								        		} catch (Exception e) {
								        			logger.error(e.getMessage(), e);
								        		}
				        					}
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
			securedCopier.copyTo(session, localFilePath, remoteFilePath, scpBufferSize);
		}catch (Exception e) {
			logger.error("Unable to remote copy " + localFilePath + " to " + remoteFilePath + " " +  e.getMessage(), e);
			throw e;
		}
	}
}
