package org.ishafoundation.dwaraapi.storage.storagelevel;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.commandline.remote.sch.RemoteCommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.remote.sch.SshSessionHelper;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.FlowelementDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Destination;
import org.ishafoundation.dwaraapi.db.model.master.jointables.Flowelement;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.process.helpers.LogicalFileHelper;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.fracpete.processoutput4j.output.CollectingProcessOutput;
import com.github.fracpete.rsync4j.RSync;
import com.jcraft.jsch.Session;

@Component("file"+DwaraConstants.STORAGELEVEL_SUFFIX)
//@Profile({ "!dev & !stage" })
public class FileStoragelevel implements IStoragelevel {

	private static final Logger logger = LoggerFactory.getLogger(FileStoragelevel.class);

	@Autowired
	private FlowelementDao flowelementDao;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@Autowired
	private SshSessionHelper sshSessionHelper;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
    private RemoteCommandLineExecuter remoteCommandLineExecuter;
	
	@Autowired
	private	LogicalFileHelper logicalFileHelper;

	@Override
	public StorageResponse copy(SelectedStorageJob selectedStorageJob) throws Exception{
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		Artifact inputArtifact = storageJob.getArtifact();

		String inputArtifactName = inputArtifact.getName();

        String taskConfigPathnameRegex = null;
        String configuredDestinationId = null;
        String flowElementId = storageJob.getJob().getFlowelementId();
        if(!flowElementId.startsWith("C")) {
        	Flowelement fe = flowelementDao.findById(flowElementId).get();
        	taskConfigPathnameRegex = fe.getTaskconfig().getPathnameRegex();
        	configuredDestinationId = fe.getTaskconfig().getDestinationId();
        }

//		Volume volume = storageJob.getVolume();
//		String configuredDestinationId = volume.getId();
		String configuredDestinationPath = null;
		if(configuredDestinationId != null) {
			Destination destination = configurationTablesUtil.getDestination(configuredDestinationId);
			if(destination == null)
				throw new DwaraException("Destination " + configuredDestinationId + " is not configured in DB");
			configuredDestinationPath = destination.getPath();
		}

//		String destinationDirPath = volume.getDetails().getRemoteDestination(); // This includes the host ip
        String destinationDirPath = configuredDestinationPath; // This includes the host ip
		logger.trace("destinationDirPath " + destinationDirPath);

		String sshUser = configuration.getSshSystemUser();
		String host = StringUtils.substringBefore(destinationDirPath, ":");
        
        int jobId = storageJob.getJob().getId();
        
        String artifactPrefixPath = storageJob.getArtifactPrefixPath();
        Path sourceFilePath = Paths.get(artifactPrefixPath, inputArtifactName);
        
		String destination = StringUtils.substringBefore(destinationDirPath, inputArtifactName);
		logger.trace("destination " + destination);
		String destinationFilePathname = null;
		String tmpDestinationFilePathname = destination + ".copying" + File.separator;
		
//        Collection<File> fileCollection =  new ArrayList<File>(); 
//		List<String> extensions = null;
//		String[] extensionsArray = null;
//
//		Set<String> pathsToBeUsed = new TreeSet<String>();
//		Set<String> extnsToBeUsed = null; 
//        if(taskConfigPathnameRegex != null) {
//			FiletypePathnameReqexVisitor filetypePathnameReqexVisitor = new FiletypePathnameReqexVisitor(taskConfigPathnameRegex);
//			try {
//				Files.walkFileTree(sourceFilePath, filetypePathnameReqexVisitor);
//			} catch (IOException e) {
//				// swallow for now
//			}
//			if(filetypePathnameReqexVisitor != null) {
//				pathsToBeUsed.addAll(filetypePathnameReqexVisitor.getPaths());
//				if(filetypePathnameReqexVisitor.getExtns().size() > 0) { // if regex contains specific file extns we need to only use processable files with that extn only{
//					extnsToBeUsed = filetypePathnameReqexVisitor.getExtns();
//				}
//			}
//
//			if(extnsToBeUsed != null) {
//				extensions = new ArrayList<String>();
//				extensions.addAll(extnsToBeUsed);
//				extensionsArray = ArrayUtils.toStringArray(extensions.toArray());
//			}
//
//	        
//			for (String nthPathToBeUsed : pathsToBeUsed) {
//				fileCollection.addAll(FileUtils.listFiles(new File(nthPathToBeUsed), extensionsArray, true));
//			}
//
//			for (File file : fileCollection) {
//				
//				// TODO --- REMOVE THIS AFTER DIGI IS OVER...
//				if(inputArtifact.getArtifactclass().getId().startsWith("video-digi-2020")) {
//					destinationFilePathname = tmpDestinationFilePathname + file.getName(); // Reqmt - No need for the filepathname structur as when job fails, leaves the empty folder structure causing confusion
//				}
//				else {
//					String filepathname = StringUtils.substringAfter(destinationDirPath, destination);
//					logger.trace("filepathname " + filepathname);
//					
//					destinationFilePathname = tmpDestinationFilePathname + filepathname;
//				}
//
//				copy(jobId, host, sshUser, file, destinationFilePathname);
//				
//		        if(inputArtifact.getArtifactclass().getId().startsWith("video-digi-2020")) {
//			        String tmpDestination = StringUtils.substringAfter(destinationFilePathname, ":");
//			        String mvCommand = "mv " + tmpDestination + " " + StringUtils.substringBefore(tmpDestination, ".copying");
//			        
//			        executeCommandRemotely(host, sshUser, mvCommand, jobId);
//			        return new StorageResponse();
//		        }		        
//			}
//
//        }
//        else {
			destinationFilePathname = tmpDestinationFilePathname + inputArtifactName;

			copy(jobId, host, sshUser, sourceFilePath.toFile(), destinationFilePathname);
//        }
        
        // now moving back the file from the .copying to the original destination...
        String tmpDestination = StringUtils.substringAfter(tmpDestinationFilePathname + inputArtifactName, ":"); // /data/photo-proxy/.copying/P123_SadhguruOnMyEnlightenment_2010
        String mvCommand = "mv " + tmpDestination + " " + destination;
        
        executeCommandRemotely(host, sshUser, mvCommand, jobId);
        
		return new StorageResponse();
	}
	
	private void copy(int jobId, String host, String sshUser, File file, String destinationFilePathname) throws Exception {
		String parentDir = FilenameUtils.getFullPathNoEndSeparator(destinationFilePathname);
		String command1 = "mkdir -p \"" + parentDir + "\"";
		
		CommandLineExecutionResponse cler = executeCommandRemotely(host, sshUser, command1, jobId);
		if(!cler.isComplete())
			throw new Exception("Unable to create dir remotely " + cler.getFailureReason());

		
		logger.info("processing rsync copy: " +  file.getAbsolutePath() + ", destination: " + destinationFilePathname);
        
        RSync rsync = new RSync()
        .source(file.getAbsolutePath())
        .destination(sshUser + "@" + destinationFilePathname)
        .recursive(true)
        .checksum(configuration.isChecksumRsync())
        .bwlimit(configuration.getBwLimitRsync());
        //.removeSourceFiles(true); // Reqmt - File gets deleted in downstream job
        
        CollectingProcessOutput output = rsync.execute();
        
        logger.info(output.getStdOut());
        logger.info("Exit code: " + output.getExitCode());
        
        if(output.getExitCode() != 0)
        	throw new Exception("Unable to copy file " + output.getStdErr());

	}
	
	private CommandLineExecutionResponse executeCommandRemotely(String host, String sshUser, String command1, int jobId) throws Exception {
		Session jSchSession = null;
        CommandLineExecutionResponse response = null;
        try {
        	jSchSession = sshSessionHelper.getSession(host, sshUser);
			logger.trace("executing remotely " + command1);
			response = remoteCommandLineExecuter.executeCommandRemotelyOnServer(jSchSession, command1, jobId + ".out_err");
		}finally {
			if (jSchSession != null) 
				sshSessionHelper.disconnectSession(jSchSession);
		}
        return response;
	}

	@Override
	public StorageResponse initialize(SelectedStorageJob job) throws Exception{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public StorageResponse write(SelectedStorageJob selectedStorageJob) throws Exception{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public StorageResponse verify(SelectedStorageJob job) throws Exception{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StorageResponse finalize(SelectedStorageJob job) throws Exception{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public StorageResponse restore(SelectedStorageJob job) throws Exception{
		// TODO Auto-generated method stub
		return null;
	}

}
