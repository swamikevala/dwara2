package org.ishafoundation.dwaraapi.storage.storageformat.bru;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.storage.StorageFormatFactory;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storageformat.AbstractStorageFormatArchiver;
import org.ishafoundation.dwaraapi.storage.storageformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.storageformat.ArchivedFile;
import org.ishafoundation.dwaraapi.storage.storageformat.bru.response.BruResponse;
import org.ishafoundation.dwaraapi.storage.storageformat.bru.response.BruResponseParser;
import org.ishafoundation.dwaraapi.storage.storageformat.bru.response.components.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BruArchiver extends AbstractStorageFormatArchiver {
    static {
    	StorageFormatFactory.register("BRU", BruArchiver.class);
    }
    
    private static final Logger logger = LoggerFactory.getLogger(BruArchiver.class);
	
	@Autowired
	protected CommandLineExecuter commandLineExecuter;	    
    
	@Override
	protected ArchiveResponse archive(StorageJob storageJob) {
		String tapeLabel = storageJob.getVolume().getTape().getBarcode();
		int blockSizeInKB = storageJob.getVolume().getTape().getBlocksize()/1000;
		String dataTransferElementName = storageJob.getDeviceWwid();
		String libraryPrefixPath = storageJob.getLibraryPrefixPath();
		String libraryToBeWritten = storageJob.getLibraryToBeCopied();
		
		logger.trace("Framing bru copy command");
		// frames bru command
		List<String> bruCopyCommandParamsList = getBruCopyCommand(tapeLabel, blockSizeInKB, dataTransferElementName, libraryPrefixPath, libraryToBeWritten);		
		// executes the command and parsesitsresponse
		// TODO Handle both success and error scenarios
		logger.trace("Executing the framed bru copy command");
		return executeCommand(bruCopyCommandParamsList);
	}

	@Override
	protected ArchiveResponse restore(StorageJob storageJob) {
		String dataTransferElementName = storageJob.getDeviceWwid();
		String filePathNameToBeRestored = storageJob.getFilePathname();
		String destinationPath = storageJob.getDestinationPath();
		int blockSizeInKB = storageJob.getVolume().getTape().getBlocksize()/1000;

		try {
			logger.trace("Creating the directory " + destinationPath + ", if not already present");
			FileUtils.forceMkdir(new java.io.File(destinationPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// frames bru command for restore, executes, parses its implementation specific response and returns back common archived response
		List<String> bruRestoreCommandParamsList = getBruRestoreCommand(dataTransferElementName, filePathNameToBeRestored, blockSizeInKB, destinationPath);

		// TODO Handle both success and error scenarios
		logger.trace("Executing the framed bru restore command");
		return executeCommand(bruRestoreCommandParamsList);
	}

	protected ArchiveResponse executeCommand(List<String> bruCommandParamsList){
		
		// common method for both ingest and restore
		logger.trace("Archiving using BruArchiver");
		// the methods frame the command and delegate it to this method
		// executes the command, parses the response and returns it back..

		CommandLineExecutionResponse bruCopyCommandLineExecutionResponse = commandLineExecuter.executeCommand(bruCommandParamsList, "/data/tmp/777.out");
		if(bruCopyCommandLineExecutionResponse.isComplete()) {
			logger.trace("b4 brp - " + bruCopyCommandLineExecutionResponse.getStdOutResponse());
			BruResponseParser brp = new BruResponseParser();
			BruResponse br = brp.parseBruResponse(bruCopyCommandLineExecutionResponse.getStdOutResponse());
			logger.trace("br - " + br);
			return convertBruResponseToArchiveResponse(br);
		}else {
			logger.trace("command failed - TODO better this comment");
			return null;
		}
	}
	

	private List<String> getBruCopyCommand(String volumeLabel, int blockSizeInKB, String dataTransferElementName, String libraryPrefixPath, String libraryToBeWritten) {
		
		String bruCopyCommand = "bru -B -clOjvvvvvvvvv -QX -b " + blockSizeInKB + "K -f " + dataTransferElementName + " " + libraryToBeWritten;
		//String bruCopyCommand = "bru -B -clOjvvvvvvvvv -L " + volumeLabel + " -QX -b " + blockSizeInKB + "K -f " + dataTransferElementName + " " + libraryToBeWritten;
		
		List<String> commandList = new ArrayList<String>();
		commandList.add("sh");
		commandList.add("-c");
		commandList.add("cd " + libraryPrefixPath + " ; " + bruCopyCommand);
		/*
		commandList.add("bru");
		commandList.add("-clOjvvvvvvvvv");
		commandList.add("-L");
		commandList.add(volumeLabel);
		commandList.add("-QX");
		commandList.add("-b");
		commandList.add(blockSizeInKB + "K");
		commandList.add("-f");
		commandList.add(dataTransferElementName);
		commandList.add(libraryToBeWritten);*/
		
		return commandList;
	}
	
	

	private List<String> getBruRestoreCommand(String dataTransferElementName, String filePathNameToBeRestored,  int blockSizeInKB, String destinationPath) {

		String bruRestoreCommand = "bru -B -xvvvvvvvvv -QV -b " + blockSizeInKB + "K -f " + dataTransferElementName + " " + filePathNameToBeRestored;
		
		List<String> commandList = new ArrayList<String>();
		commandList.add("sh");
		commandList.add("-c");
		commandList.add("cd " + destinationPath + " ; " + bruRestoreCommand);
		
		/*
		commandList.add("bru");
		commandList.add("-xvvvvvvvvv");
		commandList.add("-f");
		commandList.add(filePathNameToBeRestored);
		commandList.add("-b");
		commandList.add(blockSizeInKB + "K");
		commandList.add("-QV");
		commandList.add("-C");
		*/
		return commandList;
	}	
	
	private ArchiveResponse convertBruResponseToArchiveResponse(BruResponse br){
		ArchiveResponse ar = new ArchiveResponse();
		List<ArchivedFile> archivedFileList = new ArrayList<ArchivedFile>();
		List<File> bruedFileList = br.getFileList();
		for (Iterator<File> iterator = bruedFileList.iterator(); iterator.hasNext();) {
			File bruedFile = (File) iterator.next();
			ArchivedFile af = new ArchivedFile();
			af.setBlockNumber(bruedFile.getBlockNumber() + 1); // +1, because for some reason bru "copy" responds with -1 block for BOT, while bru "t - table of contents"/"x - extraction" shows the block as 0 for same. Also while seek +1 followed by t/x returns faster results...
			af.setFilePathName(bruedFile.getFileName());
			
			archivedFileList.add(af);
		}
		ar.setArchivedFileList(archivedFileList);
		return ar;
	}
	
//	@Override
//	readlabel(){
//		// bru specific label reading
//	}
}
