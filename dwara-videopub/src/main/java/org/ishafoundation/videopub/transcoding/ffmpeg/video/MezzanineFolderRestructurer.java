package org.ishafoundation.videopub.transcoding.ffmpeg.video;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.File;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("restructure-mezz-folder")
public class MezzanineFolderRestructurer implements IProcessingTask {

	private static final Logger logger = LoggerFactory.getLogger(MezzanineFolderRestructurer.class);

	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {

		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();

		LogicalFile logicalFile = processContext.getLogicalFile();
		String logicalFileAbsolutePath = logicalFile.getAbsolutePath();
		String logicalFileName = logicalFile.getName();
		String newFolderPathForMezzanineArtifact = "";
		int jobID = processContext.getJob().getId();

		// 1. Check if the file exists in the path specified.
		if (!logicalFile.exists()) {
			String msg = "Mezzanine Restructurer: The input file doesnt exist -> " + logicalFileAbsolutePath
					+ " jobID -> " + jobID;
			logger.error(msg);
			throw new Exception(msg);
		}

		// 2. Add Dwara ID to Filename
		File fileRowBeingProcessed =  processContext.getFile();
		int originalFileID = 0;
		//		if (fileRowBeingProcessed instanceof TFile) {
		//			String tFilePathName = processContext.getFile().getFileRef().getPathname();
		//			// Get the file id by quering the pathname
		//			originalFileID = fileDao.findByPathname(tFilePathName).getId();
		//		}
		//		else {
		//			originalFileID = processContext.getFile().getFileRef().getId();
		//		}
		originalFileID = processContext.getFile().getFileRef().getId();
		String newFilename = "DwaraID_" + originalFileID + "_" + logicalFileName;

		// 3. Create the path where the file must be moved by finding out the artifactName 
		// (i) Get the artifact name and see if the Input artifact folder actually
		// exists
		String splitter = java.io.File.separator; 
		String artifactName = processContext.getJob().getInputArtifact().getName();
		Pattern pattern = Pattern.compile(artifactName.replaceAll("-", "\\-"), Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(logicalFileAbsolutePath);
		boolean gotArtifactNameInFilePath = matcher.find();
		if (!gotArtifactNameInFilePath) { // Trust but verify
			String msg = "Did not get the artifact name in the filepath shared for restructuring of proxy: Artifact->"
					+ artifactName + " and FilePath shared->" + logicalFileAbsolutePath + " Job ID->" + jobID;
			logger.error(msg);
			throw new Exception(msg);
			// artifact is not there the file path means the wrong path was given. Hope it
			// got the artifact folder.If not then prakash anna will be in soup.Drumstick
			// soup with lemons.
		} else {
			// (ii) Create the parent folder (Mezzanine artifact folder ) if it doesn't
			// exist in Restructured folder
			newFolderPathForMezzanineArtifact = logicalFileAbsolutePath.replaceAll(artifactName + ".*",
					"Restructured" + splitter + artifactName);
			java.io.File newFolderForMezzanineArtifact = new java.io.File(newFolderPathForMezzanineArtifact);

			if (!newFolderForMezzanineArtifact.isDirectory()) {
				// Mezzanine folder does not exist . create it so that the file can happily rest
				// in the shade of the artifact folder.
				try {
					newFolderForMezzanineArtifact.mkdirs();
				} catch (Exception e) {
					String msg = "Could not create mezzanine proxy directory -> " + newFolderPathForMezzanineArtifact
							+ " jobID-> " + jobID;
					logger.error(msg);
					throw new Exception(msg);
				}
			}
		}
		if (newFolderPathForMezzanineArtifact.equals("")) {
			// Something went wrong. New Folder path for mezzanine could not be created.
			// This should never happen bcoz the checks were done in the previous If else !
			String msg = "Something went wrong. New Folder path for mezzanine could not be created. FilePath -> "
					+ logicalFileAbsolutePath + "Job ID -> " + jobID;
			logger.error(msg);
			throw new Exception(msg);
		}
		
		// 4. Move the file to Restructured/<Mezzanine-Folder>/NewFilename
		String newFilePathForMezzanineProxyFile = newFolderPathForMezzanineArtifact + splitter + newFilename;
		try {
			Files.move(Paths.get(logicalFileAbsolutePath), Paths.get(newFilePathForMezzanineProxyFile));
		}
		catch (Exception e) {
			String msg = "Mezzanine restructure File move failed from -> " + logicalFileAbsolutePath + " to "
					+ newFilePathForMezzanineProxyFile + " jobID -> " + jobID;
			logger.error(msg);
			throw new Exception(msg);
		} 
		//****************************************** YES! YES! YES! . File was renamed and moved successfully ****************

		processingtaskResponse.setIsComplete(true);
		return processingtaskResponse;
	}

}
