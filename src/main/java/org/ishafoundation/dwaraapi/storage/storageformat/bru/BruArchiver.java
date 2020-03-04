package org.ishafoundation.dwaraapi.storage.storageformat.bru;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
		String fileToBeWritten = storageJob.getLibrarypathToBeCopied();
		// frames bru command
		List<String> bruCopyCommandParamsList = getBruCopyCommand(tapeLabel, blockSizeInKB, dataTransferElementName, fileToBeWritten);		
		// executes the command and parsesitsresponse
		// TODO Handle both success and error scenarios
		return executeCommand(bruCopyCommandParamsList);
	}

	@Override
	protected ArchiveResponse restore(StorageJob storageJob) {
		String filePathNameToBeRestored = storageJob.getFilePathname();
		String destinationPath = storageJob.getDestinationPath();
		int blockSizeInKB = storageJob.getVolume().getTape().getBlocksize();


		List<String> bruCopyCommandParamsList = getBruRestoreCommand(filePathNameToBeRestored, blockSizeInKB, destinationPath);
		// TODO frames bru command for restore, executes, parsesitsresponse and returns it back

		// TODO Handle both success and error scenarios
		return executeCommand(bruCopyCommandParamsList);
	}

	protected ArchiveResponse executeCommand(List<String> bruCommandParamsList){
		
		// common method for both ingest and restore
		logger.trace("Archiving using BruArchiver");
		// the methods frame the command and delegate it to this method
		// executes the command, parses the response and returns it back..

		CommandLineExecutionResponse bruCopyCommandLineExecutionResponse = commandLineExecuter.executeCommand(bruCommandParamsList, "/data/tmp/777.out");
		
		BruResponseParser brp = new BruResponseParser();
		BruResponse br = brp.parseBruResponse(bruCopyCommandLineExecutionResponse.getStdOutResponse());
		
		return convertBruResponseToArchiveResponse(br);
	}
	

	private List<String> getBruCopyCommand(String volumeLabel, int blockSizeInKB, String dataTransferElementName, String fileToBeWritten) {
		List<String> commandList = new ArrayList<String>();
		commandList.add("bru");
		commandList.add("-clOjvvvvvvvvv");
		commandList.add("-L");
		commandList.add(volumeLabel);
		commandList.add("-QX");
		commandList.add("-b");
		commandList.add(blockSizeInKB + "K");
		commandList.add("-f");
		commandList.add(dataTransferElementName);
		commandList.add(fileToBeWritten);
		
		return commandList;
	}
	
	

	private List<String> getBruRestoreCommand(String filePathNameToBeRestored,  int blockSizeInKB, String destinationPath) {
		List<String> commandList = new ArrayList<String>();
		commandList.add("sh");
		commandList.add("-c");
		commandList.add("cd " + destinationPath + " ; " );
		commandList.add("bru");
		commandList.add("-xvvvvvvvvv");
		commandList.add("-f");
		commandList.add(filePathNameToBeRestored);
		commandList.add("-b");
		commandList.add(blockSizeInKB + "K");
		commandList.add("-QV");
		commandList.add("-C");
		
		return commandList;
	}	
	
	private ArchiveResponse convertBruResponseToArchiveResponse(BruResponse br){
		ArchiveResponse ar = new ArchiveResponse();
		List<ArchivedFile> archivedFileList = new ArrayList<ArchivedFile>();
		List<File> bruedFileList = br.getFileList();
		for (Iterator<File> iterator = bruedFileList.iterator(); iterator.hasNext();) {
			File bruedFile = (File) iterator.next();
			ArchivedFile af = new ArchivedFile();
			af.setBlockNumber(bruedFile.getBlockNumber());
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
