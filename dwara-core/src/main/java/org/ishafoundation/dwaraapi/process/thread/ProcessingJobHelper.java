package org.ishafoundation.dwaraapi.process.thread;

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
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ArtifactclassTaskDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepositoryUtil;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Filetype;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassTask;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ExtensionFiletype;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
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
	private ArtifactclassTaskDao artifactclassTaskDao;

	@Autowired
	private FileRepositoryUtil fileRepositoryUtil;

	@Autowired
	private	LogicalFileHelper logicalFileHelper;
	
	protected HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.domain.File> getFilePathToFileObj(Domain domain, Artifact artifactDbObj) throws Exception{
		HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.domain.File> filePathTofileObj = new HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.domain.File>();
		
		List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList = fileRepositoryUtil.getArtifactFileList(artifactDbObj, domain);
		for (org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile : artifactFileList) {
			filePathTofileObj.put(nthFile.getPathname(), nthFile);
		}
		//logger.trace("file collection - " + filePathTofileObj.keySet().toString());
		return filePathTofileObj;
	}
	
	protected Collection<LogicalFile> getLogicalFileList(Filetype filetype, String inputArtifactPath, String artifactclassId, String processingtaskId){	
		Collection<LogicalFile> logicalFileCollection =  new ArrayList<LogicalFile>(); 
		
		List<String> extensions = null;
		String[] extensionsArray = null;
		List<String> sidecarExtensions = null;
		String[] sidecarExtensionsArray = null;
		boolean includeSidecarFiles = false;
		
		Set<String> pathsToBeUsed = new TreeSet<String>();
		String pathnameRegex = null;
		if(artifactclassId != null && processingtaskId != null) {
			ArtifactclassTask artifactclassTask = artifactclassTaskDao.findByArtifactclassIdAndProcessingtaskId(artifactclassId, processingtaskId);
			pathnameRegex = artifactclassTask != null ? artifactclassTask.getConfig().getPathnameRegex() : null;
		}
		Set<String> extnsToBeUsed = null; 
		if(pathnameRegex != null) { // if artifactclass_processingtask has a pathregex we need to only get the processable files from that folder path and not from the entire archives directory... e.g., video-pub-edit will have .mov files under output folder
			FiletypePathnameReqexVisitor filetypePathnameReqexVisitor = new FiletypePathnameReqexVisitor(pathnameRegex);
			try {
				Files.walkFileTree(Paths.get(inputArtifactPath), filetypePathnameReqexVisitor);
			} catch (IOException e) {
				// swallow for now
			}
			if(filetypePathnameReqexVisitor != null) {
				pathsToBeUsed.addAll(filetypePathnameReqexVisitor.getPaths());
				if(filetypePathnameReqexVisitor.getExtns().size() > 0) { // if regex contains specific file extns we need to only use processable files with that extn only{
					extnsToBeUsed = filetypePathnameReqexVisitor.getExtns();
				}
			}
		} else { // all processable files from the entire artifact directory
			pathsToBeUsed.add(inputArtifactPath);	
		}
		logger.trace("pathsToBeUsed - " + pathsToBeUsed);
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
		
		logger.trace("extensionsArray - " + extensionsArray);
		logger.trace("sidecarExtensionsArray - " + sidecarExtensionsArray);
		for (String nthPathToBeUsed : pathsToBeUsed) {
			logicalFileCollection.addAll(logicalFileHelper.getFiles(nthPathToBeUsed, extensionsArray, includeSidecarFiles, sidecarExtensionsArray));				
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
