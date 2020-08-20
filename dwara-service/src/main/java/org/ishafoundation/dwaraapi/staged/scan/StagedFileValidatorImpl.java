package org.ishafoundation.dwaraapi.staged.scan;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ishafoundation.dwaraapi.api.resp.staged.scan.StagedFileDetails;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.ExtensionDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StagedFileValidatorImpl implements IStagedFileValidator{
    
	private static final Logger logger = LoggerFactory.getLogger(StagedFileValidatorImpl.class);
	
	@Autowired
    private ExtensionDao extensionDao;
	
	@Autowired
    private CommandLineExecuter commandLineExecuter;
	
	@Autowired
    private Configuration configuration;
	
    private String regexAllowedChrsInFileName = null;
	Pattern allowedChrsInFileNamePattern = null;
	private List<Pattern> excludedFileNamesRegexList = null;
	
	@PostConstruct
	private void loadConfigEntries() {
		regexAllowedChrsInFileName = configuration.getRegexAllowedChrsInFileName();
		allowedChrsInFileNamePattern = Pattern.compile(regexAllowedChrsInFileName);
		excludedFileNamesRegexList = new ArrayList<Pattern>();
		for (int i = 0; i < configuration.getJunkFilesFinderRegexPatternList().length; i++) {
			Pattern nthJunkFilesFinderRegexPattern = Pattern.compile(configuration.getJunkFilesFinderRegexPatternList()[i]);
			excludedFileNamesRegexList.add(nthJunkFilesFinderRegexPattern);
		}
	}

	@Override
	public List<Error> validate(StagedFileDetails stagedFileDetails) {
		List<Error> errorList = new ArrayList<Error>();
		
		// validateCount
		if(stagedFileDetails.getFileCount() == 0) {
			Error error = new Error();
			error.setType(Errortype.Error);
			error.setMessage("Artifact Folder has no files inside");
			errorList.add(error);
		}
		
		// validateSize
		// TODO whats the size we need to compare against?
		long configuredSize = 1048576; // 1MB
		if(stagedFileDetails.getTotalSize() < configuredSize) {
			Error error = new Error();
			error.setType(Errortype.Warning);
			error.setMessage("Artifact is less than 1 MiB");
			errorList.add(error);
		};
		
		//messageBuilder = validateName(stagedFileDetails.getName(), messageBuilder);
		errorList.addAll(validateName(stagedFileDetails.getName()));
		
		errorList.add(checkUnsupportedExtensions(stagedFileDetails));
		
		errorList.add(setPermissions(stagedFileDetails));
		
		// TODO dupe check on size 
		
		return errorList;
	}

	private List<Error> validateName(String fileName) {
		List<Error> errorList = new ArrayList<Error>();
		if(fileName.length() > 150) {
			Error error = new Error();
			error.setType(Errortype.Warning);
			error.setMessage("Artifact Name gt 150 characters");
			errorList.add(error);
		}

		Matcher m = allowedChrsInFileNamePattern.matcher(fileName);
		if(!m.matches()) {
			Error error = new Error();
			error.setType(Errortype.Warning);
			error.setMessage("Artifact Name contains special characters");
			errorList.add(error);
		}
		return errorList;
	}


	private Error checkUnsupportedExtensions(StagedFileDetails stagedFileDetails) {
		Iterable<Extension> extensionList = extensionDao.findAll();
		Set<String> supportedExtns =  new TreeSet<String>();
		for (Extension extension : extensionList) {
			supportedExtns.add(extension.getId().toUpperCase());
			supportedExtns.add(extension.getId().toLowerCase());
		}

    	// Step 2 - Iterate through all Files under the artifact Directory and check if their extensions are supported in our system.
    	String artifactName = stagedFileDetails.getName();
    	String originFolderPath = stagedFileDetails.getPath();
    	
    	File mediaLibraryFile = FileUtils.getFile(originFolderPath, artifactName);
    	
		Collection<File> allFilesInTheSystem = null;
        if(mediaLibraryFile.isDirectory()) {
        	allFilesInTheSystem = FileUtils.listFiles(mediaLibraryFile, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
	    }else {
	    	allFilesInTheSystem = new ArrayList<File>();
	    	allFilesInTheSystem.add(mediaLibraryFile);
	    }
        
		Set<String> unSupportedExtns =  new TreeSet<String>();
		// iterate the files and get extensions
		for (Iterator<File> iterator = allFilesInTheSystem.iterator(); iterator.hasNext();) {
			File nthFile = (File) iterator.next();
			String nthFileName = nthFile.getName();
			String nthFileExtn = FilenameUtils.getExtension(nthFileName);
			
//			// excluding known useless files
//			String[] excludedExtns = config.getFileExtensionsToBeExcludedFromValidation();
//			List<String> supportedVideoExtnsAsList =  new ArrayList<String>(Arrays.asList(excludedExtns));
//			if (supportedVideoExtnsAsList.contains(nthFileExtn.toUpperCase())) 
//				continue;
			
			// skipping junk files...
			boolean isJunkFile = false;
			for (Iterator<Pattern> iterator2 = excludedFileNamesRegexList.iterator(); iterator2.hasNext();) {
				Pattern nthJunkFilesFinderRegexPattern = iterator2.next();
				Matcher m = nthJunkFilesFinderRegexPattern.matcher(nthFileName);
				if(m.matches()) {
					isJunkFile = true;
					break;
				}
			}
			if(isJunkFile) 
				continue;
			
			// validate against the supported list of extensions in the system we have
			if(!supportedExtns.contains(nthFileExtn))
				unSupportedExtns.add(nthFileExtn);
		}
		
    	// Step 3 - Throw exception with unsupported extns list	
		if(unSupportedExtns.size() > 0) {
			Error error = new Error();
			error.setType(Errortype.Warning);
			error.setMessage("There are unsupported file extensions in the list. Please review them..." + unSupportedExtns);
			return error;
		}
		return null;
	}
	
	private Error setPermissions(StagedFileDetails stagedFileDetails) {
		if(configuration.isLibraryFileSystemPermissionsNeedToBeSet()) {
			String script = configuration.getLibraryFile_ChangePermissionsScriptPath();
			String artifactName = stagedFileDetails.getName();
			String sourcePath = stagedFileDetails.getPath();// holds something like /data/user/pgurumurthy/ingest/pub-video
			File toBeIngestedFile = FileUtils.getFile(sourcePath, artifactName);
			
			CommandLineExecutionResponse setPermsCommandLineExecutionResponse = null;
			try {
				setPermsCommandLineExecutionResponse = changeFilePermissions(script, sourcePath, artifactName);
				
				if(!setPermsCommandLineExecutionResponse.isComplete()) {					
					String message = "Unable to set permissions to " + toBeIngestedFile + ". " + setPermsCommandLineExecutionResponse.getFailureReason() + ". Please contact Admin";
					Error error = new Error();
					error.setType(Errortype.Error);
					error.setMessage(message);
					logger.warn(message);
				}

			} catch (Exception e) {
				String message = "Unable to set permissions to " + toBeIngestedFile + " : " + e.getMessage() + ". Please contact Admin";
				Error error = new Error();
				error.setType(Errortype.Error);
				error.setMessage(message);

				logger.warn(message);
			}
		}
		return null;
	}
	
    private CommandLineExecutionResponse changeFilePermissions(String script, String sourcePath, String artifactName) throws Exception {
		String parts[] = sourcePath.split("/");
		String user = parts[3];
		String artifactclassName = parts[5];

		List<String> setFilePermissionsCommandParamsList = new ArrayList<String>();
		setFilePermissionsCommandParamsList.add("sudo");
		setFilePermissionsCommandParamsList.add(script);
		setFilePermissionsCommandParamsList.add(user);
		setFilePermissionsCommandParamsList.add(artifactclassName);
		setFilePermissionsCommandParamsList.add(artifactName);
		
		CommandLineExecutionResponse setPermsCommandLineExecutionResponse = commandLineExecuter.executeCommand(setFilePermissionsCommandParamsList);
		return setPermsCommandLineExecutionResponse;
    }
}