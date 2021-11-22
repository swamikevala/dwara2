package org.ishafoundation.dwaraapi.process.thread;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.ishafoundation.dwaraapi.db.dao.master.FiletypeDao;
import org.ishafoundation.dwaraapi.db.dao.master.ProcessingtaskDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Filetype;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ExtensionFiletype;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.helpers.FiletypePathnameReqexVisitor;
import org.ishafoundation.dwaraapi.process.helpers.LogicalFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessingJobHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessingJobHelper.class);

	@Autowired
    private ProcessingtaskDao processingtaskDao;
	
	@Autowired
	private FiletypeDao filetypeDao;
		
	@Autowired
	private TFileDao tFileDao;
	
	@Autowired
	private FileDao fileDao;

	@Autowired
	private	LogicalFileHelper logicalFileHelper;
	
	protected HashMap<String, TFile> getFilePathToTFileObj(int artifactId) throws Exception{
		HashMap<String, TFile> filePathToTFileObj = new HashMap<String, TFile>();
		List<TFile> artifactTFileList = tFileDao.findAllByArtifactIdAndDeletedIsFalse(artifactId);
		for (TFile tFile : artifactTFileList) {
			filePathToTFileObj.put(tFile.getPathname(), tFile);
		}
		return filePathToTFileObj;
	}
	
	protected HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.File> getFilePathToFileObj(Artifact artifactDbObj) throws Exception{
		HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.File> filePathTofileObj = new HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.File>();
		
		List<org.ishafoundation.dwaraapi.db.model.transactional.File> artifactFileList = fileDao.findAllByArtifactIdAndDeletedFalse(artifactDbObj.getId());
		for (org.ishafoundation.dwaraapi.db.model.transactional.File nthFile : artifactFileList) {
			filePathTofileObj.put(nthFile.getPathname(), nthFile);
		}
		//logger.trace("file collection - " + filePathTofileObj.keySet().toString());
		return filePathTofileObj;
	}
	
	protected Collection<LogicalFile> getLogicalFileList(Filetype filetype, String inputArtifactPath, String pathnameRegex){	
		Collection<LogicalFile> logicalFileCollection =  new ArrayList<LogicalFile>(); 
		
		List<String> extensions = null;
		String[] extensionsArray = null;
		List<String> sidecarExtensions = null;
		String[] sidecarExtensionsArray = null;
		boolean includeSidecarFiles = false;
		
		Collection<File> filesToBeUsed = null;
		Set<String> pathsToBeUsed = new TreeSet<String>();
		Set<String> extnsToBeUsed = null; 
		if(pathnameRegex != null) { // if flowelement.task_config has pathnameregex configured - we need to only get the processable files that match the pattern and not from the entire archives directory... e.g., video-pub-edit will have .mov files under output folder
			FiletypePathnameReqexVisitor filetypePathnameReqexVisitor = new FiletypePathnameReqexVisitor(inputArtifactPath, pathnameRegex);
			try {
				Files.walkFileTree(Paths.get(inputArtifactPath), filetypePathnameReqexVisitor);
			} catch (IOException e) {
				logger.error("Unable to walkFileTree for " + inputArtifactPath + ":" + e.getMessage(), e);
			}
			if(filetypePathnameReqexVisitor != null) {
				filesToBeUsed = filetypePathnameReqexVisitor.getMatchedFiles();
				if(filetypePathnameReqexVisitor.getExtns().size() > 0) { // if regex contains specific file extns we need to only use processable files with that extn only{
					extnsToBeUsed = filetypePathnameReqexVisitor.getExtns();
				}
				logger.trace("filesToBeUsed - " + filesToBeUsed);
			}
		} else { // all processable files from the entire artifact directory
			pathsToBeUsed.add(inputArtifactPath);
			logger.trace("pathsToBeUsed - " + pathsToBeUsed);
		}
		
		logger.trace("extnsToBeUsed - " + extnsToBeUsed);
		
		if(filetype != null) { // if filetype is null, extensions are set to null - which will get all the files listed - eg., process like checksum-gen
			extensions = new ArrayList<String>();
			sidecarExtensions = new ArrayList<String>();

			List<ExtensionFiletype> extn_Filetype_List = filetype.getExtensions(); //extensionFiletypeDao.findAllByFiletypeId(filetype.getId());
			for (ExtensionFiletype extensionFiletype : extn_Filetype_List) {
				String extensionName = extensionFiletype.getExtension().getId();
				if(extnsToBeUsed == null || (extnsToBeUsed != null && extnsToBeUsed.contains(extensionName))) { // if regex contains specific file extns, we need to only use them and not all the extensions_filetype for that particular processingtasks' filetype
					if(extensionFiletype.isSidecar()) {
						sidecarExtensions.add(extensionName);
						includeSidecarFiles = true;
					}
					else
						extensions.add(extensionName);
				}
			}
			
			extensionsArray = ArrayUtils.toStringArray(extensions.toArray());
			sidecarExtensionsArray = ArrayUtils.toStringArray(sidecarExtensions.toArray());
		}else { 
			if(extnsToBeUsed != null) {
				extensions = new ArrayList<String>();
				extensions.addAll(extnsToBeUsed);
				extensionsArray = ArrayUtils.toStringArray(extensions.toArray());
			}
		}
	
		if(pathnameRegex != null && filesToBeUsed != null) {
			logicalFileCollection.addAll(logicalFileHelper.getFiles(filesToBeUsed, extensionsArray, includeSidecarFiles, sidecarExtensionsArray));
		}else {
			logger.trace("extensionsArray - " + extensions);
			logger.trace("sidecarExtensionsArray - " + sidecarExtensions);
			for (String nthPathToBeUsed : pathsToBeUsed) {
				logicalFileCollection.addAll(logicalFileHelper.getFiles(nthPathToBeUsed, extensionsArray, includeSidecarFiles, sidecarExtensionsArray));				
			}
		}
		
		return logicalFileCollection;
	}
	
	protected Filetype getInputFiletype(Processingtask processingtask) {
		Filetype ft = null;
		if(processingtask != null)
			ft = getFiletype(processingtask.getFiletypeId());
		return ft;
	}
	
	protected Filetype getOutputFiletype(Processingtask processingtask) {
		Filetype ft = null;
		if(processingtask != null)
			ft = getFiletype(processingtask.getOutputFiletypeId());
		return ft;
	}	
	
	protected Filetype getFiletype(String filetypeId) {
		// TODO Cache filetypes...
		Filetype ft = null;
		if(!filetypeId.equals("_all_")) { // if filetype is all means get all files...
			ft = filetypeDao.findById(filetypeId).get();
		}
		return ft;
	}
	
	protected Processingtask getProcessingtask(String processingtaskId) {
		Processingtask processingtask = null;
		Optional<Processingtask> processingtaskOpt = processingtaskDao.findById(processingtaskId);
		if(processingtaskOpt.isPresent())
			processingtask = processingtaskOpt.get();
		return processingtask;
	}
}
