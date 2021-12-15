package org.ishafoundation.dwaraapi.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.xml.stream.XMLOutputFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.req._import.BulkImportRequest;
import org.ishafoundation.dwaraapi.api.req._import.ImportRequest;
import org.ishafoundation.dwaraapi.api.resp._import.ImportResponse;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.SequenceDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.ArtifactVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.FileVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TArtifactVolumeImportDao;
import org.ishafoundation.dwaraapi.db.keys.TArtifactVolumeImportKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional._import.jointables.TArtifactVolumeImport;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.ArtifactVolumeDetails;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.SequenceUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.ArtifactVolumeStatus;
import org.ishafoundation.dwaraapi.enumreferences.ImportStatus;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Storagelevel;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.staged.scan.Error;
import org.ishafoundation.dwaraapi.staged.scan.Errortype;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Artifact;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Volumeindex;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Volumeinfo;
import org.ishafoundation.dwaraapi.storage.storagesubtype.AbstractStoragesubtype;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.ishafoundation.dwaraapi.utils.ImportStatusUtil;
import org.ishafoundation.dwaraapi.utils.StatusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

@Component
public class ImportService extends DwaraService {
	
	private static final Logger logger = LoggerFactory.getLogger(ImportService.class);
	
	@Autowired
	private VolumeDao volumeDao;
	
	@Autowired
	protected SequenceDao sequenceDao;
	
	@Autowired
	protected ArtifactDao artifactDao;
	
	@Autowired
	protected FileDao fileDao;
	
	@Autowired
	protected ArtifactVolumeDao artifactVolumeDao;
	
	@Autowired
	protected FileVolumeDao fileVolumeDao;
	
	@Autowired
	protected TArtifactVolumeImportDao tArtifactVolumeImportDao;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private Map<String, AbstractStoragesubtype> storagesubtypeMap;
	
	@Autowired
	protected SequenceUtil sequenceUtil;

	private List<Pattern> excludedFileNamesRegexList = new ArrayList<Pattern>();
	private String invalidDirName = "invalid"; // invalid xml goes here
	private String completedDirName = "completed"; // successfully completed xmls goes here
	
	private String bruLinkSeparator = Character.toString(Character.MIN_VALUE);
	
	private Map<String, Artifactclass> id_artifactclassMap = null;
	private List<Error> errorList = null;
	private List<org.ishafoundation.dwaraapi.api.resp._import.Artifact> artifacts = null;

	@PostConstruct
	public void getExcludedFileNamesRegexList() {
		String[] junkFilesFinderRegexPatternList = configuration.getJunkFilesFinderRegexPatternList();
		for (int i = 0; i < junkFilesFinderRegexPatternList.length; i++) {
			Pattern nthJunkFilesFinderRegexPattern = Pattern.compile(junkFilesFinderRegexPatternList[i]);
			excludedFileNamesRegexList.add(nthJunkFilesFinderRegexPattern);
		}
	}
	public List<ImportResponse> bulkImport(BulkImportRequest importRequest) throws Exception {
		String importStagingDirLocation = importRequest.getStagingDir(); // /data/dwara/import-staging
		
		Path todoDirPath = Paths.get(importStagingDirLocation);
		Path invalidDirPath = Paths.get(importStagingDirLocation, invalidDirName);
		Path completedDirPath = Paths.get(importStagingDirLocation, completedDirName);
		File importStagingTodoDir = todoDirPath.toFile();
		
		if(!importStagingTodoDir.exists())
			throw new DwaraException(todoDirPath + " does not exist");
			
		File[] toBeImportedXmls = importStagingTodoDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
		
		if(toBeImportedXmls.length == 0)
			throw new DwaraException("No xml files to be imported exist in " + todoDirPath);
		
		List<ImportResponse> irl = new ArrayList<ImportResponse>();
		for (File nthXmlFile : toBeImportedXmls) {
			String volumeName = FilenameUtils.getBaseName(nthXmlFile.getName());
			logger.info("Taking up " + volumeName + " for importing");
			
			ImportResponse importResponse = null;
			Path destDir = null;		
			try {
				ImportRequest ir = new ImportRequest();
				ir.setXmlPathname(nthXmlFile.getPath());
				
				ImportResponse nthImportResponse = importCatalog(ir);
				
				// just pulling the needed info from response object
				importResponse = new ImportResponse();
				importResponse.setUserRequestId(nthImportResponse.getUserRequestId());
				importResponse.setVolumeId(nthImportResponse.getVolumeId());
				importResponse.setVolumeImportStatus(nthImportResponse.getVolumeImportStatus());
				importResponse.setRunCount(nthImportResponse.getRunCount());
				
				destDir = completedDirPath; // move the imported catalog to completed folder - eg., /data/dwara/import-staging/completed
				if(nthImportResponse.getErrors() != null && nthImportResponse.getErrors().size() > 0) {
					destDir = invalidDirPath; // if there are errors move the catalog to invalid folder
					importResponse.setErrors(nthImportResponse.getErrors());
				}
				
				moveFileNLogToOutputFolder(destDir, nthXmlFile, volumeName, nthImportResponse);
			}catch (Exception e) {
				importResponse = new ImportResponse();
				importResponse.setVolumeId(volumeName);
				importResponse.setVolumeImportStatus("failed");
				
				List<Error> errorList = new ArrayList<Error>();
				Error err = new Error();
				err.setType(Errortype.Error);
				err.setMessage(e.getMessage());
				errorList.add(err);
				
				importResponse.setErrors(errorList);

				// move it to failed??? or invalid???
				destDir = invalidDirPath;
				
				moveFileNLogToOutputFolder(destDir, nthXmlFile, volumeName, importResponse);
			}
			irl.add(importResponse);
		}
		
		return irl;
	}

	private void moveFileNLogToOutputFolder(Path destDir, File nthXmlFile, String volumeName, ImportResponse importResponse) throws IOException {
		destDir = Paths.get(destDir.toString(), volumeName); // create a directory and put the source xml file and its logs...

		// write the response to a log file inside the destDir
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(importResponse);
		FileUtils.write(Paths.get(destDir.toString(), volumeName + ".log."+importResponse.getUserRequestId()).toFile(), json);
		
		// move the catalog file to the destDir
		FileUtils.moveFile(nthXmlFile, Paths.get(destDir.toString(), volumeName + ".xml."+importResponse.getUserRequestId()).toFile());
	}
	
	/*
	 * response with all info for auditing...
	 * exception handling
	 * logging
	 * rerun
	 */
	public ImportResponse importCatalog(ImportRequest importRequest) throws Exception{	
		id_artifactclassMap = new HashMap<String, Artifactclass>();
		errorList = new ArrayList<Error>();
		artifacts = new ArrayList<org.ishafoundation.dwaraapi.api.resp._import.Artifact>();
		
		ImportResponse ir = new ImportResponse();
		Request request = null;
		try {
			String xmlPathname = importRequest.getXmlPathname();
			File xmlFile = FileUtils.getFile(xmlPathname);
	
			Volumeindex volumeindex = validateAndGetVolumeindex(xmlFile);	
			Volumeinfo volumeInfo = volumeindex.getVolumeinfo();
			String volumeId = volumeInfo.getVolumeuid();
			
			// TODO - move this to validateAndGetVolumeindex method
			Request alreadyCompletelyImportedVolumeRequest = requestDao.findAlreadyCompletelyImportedVolumeNative(volumeId);
			if(alreadyCompletelyImportedVolumeRequest != null) {
				String msg = volumeId + " already imported successfully";
				Error err = new Error();
				err.setType(Errortype.Error);
				err.setMessage(msg);
				errorList.add(err);
				throw new DwaraException(msg);
			}
			
			request = new Request();
			request.setType(RequestType.user);
			request.setActionId(Action._import);
			request.setStatus(Status.queued);
	    	User user = getUserObjFromContext();
	
	    	LocalDateTime requestedAt = LocalDateTime.now();
			request.setRequestedAt(requestedAt);
			request.setRequestedBy(user);
			
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("xmlPathname", importRequest.getXmlPathname());

			RequestDetails details = new RequestDetails();
			JsonNode postBodyJson = getRequestDetails(data); 
			details.setBody(postBodyJson);
			request.setDetails(details);

			request = requestDao.save(request);
			logger.info(DwaraConstants.USER_REQUEST + request.getId());
			
		    // update ArtifactVolume table with all entries from xml...
			List<Artifact> artifactList = volumeindex.getArtifact();
			for (Artifact nthArtifact : artifactList) {
				TArtifactVolumeImportKey aviKey = new TArtifactVolumeImportKey(volumeId, nthArtifact.getName());
				Optional<TArtifactVolumeImport> aviOptional = tArtifactVolumeImportDao.findById(aviKey);
				TArtifactVolumeImport avi = null;
				if(aviOptional.isPresent()) // for rerun this would be already available
					avi = aviOptional.get();
				else {
					avi = new TArtifactVolumeImport();				
					avi.setId(aviKey);
					avi.setStatus(Status.in_progress);
					tArtifactVolumeImportDao.save(avi);
				}
			}

			
			// updating the volume table
			boolean isRerun = false;
			Volume volume = null; 
			Optional<Volume> volOptional = volumeDao.findById(volumeId);
			
			if(volOptional.isPresent()) {
				volume = volOptional.get();
				isRerun = true;
				logger.info("Volume " + volume.getId() + " already in dwara. Rerun scenario");
			}
			else {
				volume = getVolume(volumeInfo);
				volume = volumeDao.save(volume);
				logger.info("Volume " + volume.getId() + " imported to dwara successfully");
			}
			
			long usedCapacity = 0;
	    	
//			List<Artifact> artifactList = volumeindex.getArtifact();
			for (Artifact nthArtifact : artifactList) {
				String artifactNameAsInCatalog = nthArtifact.getName(); // Name thats in tape
				String artifactNameProposed = nthArtifact.getRename() != null ? nthArtifact.getRename() : artifactNameAsInCatalog; // Proposed new name thats in tape
				String toBeArtifactName = null; // Name that needs to be saved in Artifact/File tables in DB
				logger.debug("Now importing " + artifactNameAsInCatalog);				
				
				ImportStatus artifactImportStatus = ImportStatus.failed; 
				ImportStatus artifactVolumeImportStatus = ImportStatus.failed;
				ImportStatus fileImportStatus = ImportStatus.failed;
				ImportStatus fileVolumeImportStatus = ImportStatus.failed;
				
				org.ishafoundation.dwaraapi.api.resp._import.Artifact respArtifact = new org.ishafoundation.dwaraapi.api.resp._import.Artifact();
				respArtifact.setName(artifactNameAsInCatalog);

				TArtifactVolumeImportKey aviKey = new TArtifactVolumeImportKey(volumeId, artifactNameAsInCatalog);
				TArtifactVolumeImport avi = tArtifactVolumeImportDao.findById(aviKey).get();
				if(avi.getStatus() == Status.completed) { // already completed - so skip it
					// continue;
					artifactImportStatus = ImportStatus.skipped; 
					artifactVolumeImportStatus = ImportStatus.skipped;
					fileImportStatus = ImportStatus.skipped;
					fileVolumeImportStatus = ImportStatus.skipped;
				}
				else {
					try {
						
						Artifactclass artifactclass = id_artifactclassMap.get(nthArtifact.getArtifactclassuid());
						Sequence sequence = artifactclass.getSequence();
						String extractedCode = sequenceUtil.getExtractedCode(sequence, artifactNameProposed);
						boolean isForceMatch = (sequence.getForceMatch() != null && sequence.getForceMatch() >= 1)  ? true : false;
						if(isForceMatch && extractedCode == null) {
							throw new Exception("Missing expected PreviousSeqCode : " + artifactNameProposed);
						}
						String sequenceCode = null;
						String prevSeqCode = null;
						if(extractedCode != null) {
							if(sequence.isKeepCode()) {
								// retaining the same name
								toBeArtifactName = artifactNameProposed;
								sequenceCode = extractedCode;
							}
							else {
								prevSeqCode = extractedCode;
							}
						}
						boolean artifactAlreadyExists = true;
						org.ishafoundation.dwaraapi.db.model.transactional.Artifact artifact = null;
						if(prevSeqCode != null) {
							artifact = artifactDao.findByPrevSequenceCodeAndDeletedIsFalse(prevSeqCode);
						}else if(sequenceCode != null){				
							artifact = artifactDao.findBySequenceCodeAndDeletedIsFalse(sequenceCode);
						}

						if(artifact != null) { // even if artifact extracted code matches - double check for name - and if name differs flag it
							String artifactNameShavedOffPrefix = StringUtils.substringAfter(artifact.getName(),"_");
							
							if(!artifactNameProposed.equals(artifactNameShavedOffPrefix))
								throw new Exception ("Same extractedCode but different artifact names : " + extractedCode + ". Expected - " + artifactNameShavedOffPrefix + " Actual - " + artifactNameProposed);
							
						}

						
						if(artifact == null) { // Some imported artifacts has same name but different extracted codes...
							 List<org.ishafoundation.dwaraapi.db.model.transactional.Artifact> artifactsEndingWithSameName = artifactDao.findByNameEndsWithAndArtifactclassId(artifactNameProposed,artifactclass.getId());
							 for (org.ishafoundation.dwaraapi.db.model.transactional.Artifact nthArtifactEndingWithSameName : artifactsEndingWithSameName) {
								 String artifactNameShavedOffPrefix = StringUtils.substringAfter(nthArtifactEndingWithSameName.getName(),"_");
								 if(artifactNameShavedOffPrefix.equals(artifactNameProposed)) {
									 artifact = nthArtifactEndingWithSameName;
									 throw new Exception ("Different extractedCodes but same artifact name : " + artifactNameProposed + ". Existing code " + artifact.getPrevSequenceCode() !=null ? artifact.getPrevSequenceCode() : artifact.getSequenceCode() + " Actual " + extractedCode);
								 }
							}
						}
		
						
						//TODO - should we double check with size too??? domainSpecificArtifactRepository.findAllByTotalSizeAndDeletedIsFalse(size);
						if(artifact == null) {
							artifactAlreadyExists = false;
							if(sequenceCode == null) {
//								String overrideSequenceRefId = null;
//								if(artifactclass.getId().startsWith("video") && !artifactclass.getId().startsWith("video-digi")) {
//				    				Sequence importSequenceGrp = sequenceDao.findById("video-imported-grp").get();
//									if(importSequenceGrp.getCurrrentNumber() <= 27000) 
//										overrideSequenceRefId = "video-imported-grp";
//								}
//								sequenceCode = sequenceUtil.getSequenceCode(sequence, artifactName, overrideSequenceRefId);	
								sequenceCode = sequenceUtil.getSequenceCode(sequence, artifactNameProposed);
								org.ishafoundation.dwaraapi.db.model.transactional.Artifact alreadyExistingArtifactWithSameSequenceCode = artifactDao.findBySequenceCode(sequenceCode);// findBySequenceCodeAndDeletedIsFalse(sequenceCode);
								if(alreadyExistingArtifactWithSameSequenceCode != null)
									throw new Exception("An artifact already exists with sequenceCode : " + sequenceCode + ". Already existing artifactId with same sequenceCode - " + alreadyExistingArtifactWithSameSequenceCode.getId());
								
								if(extractedCode != null && sequence.isReplaceCode())
									toBeArtifactName = artifactNameProposed.replace(extractedCode, sequenceCode);
								else
									toBeArtifactName = sequenceCode + "_" + artifactNameProposed;
							}
				
							/*
							 * Creating artifact if not already in DB
							 * 
							  
							  `id` int(11) NOT NULL, *** - *** auto 
							  `deleted` bit(1) DEFAULT NULL, *** - *** 0?
							  `file_count` int(11) DEFAULT NULL, *** - *** filecount
							  `file_structure_md5` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL, *** - *** null
							  `name` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL, *** - *** name with new sequence_code (logic is all same as ingest but sequence value is from different new column/table)
							  `prev_sequence_code` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL, *** - *** extracted sequence_code (logic is all same as ingest)
							  `sequence_code` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL, *** - *** generated sequence_code
							  `total_size` bigint(20) DEFAULT NULL, *** - *** size
							  `artifactclass_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL, *** - *** video-pub
							  `q_latest_request_id` int(11) DEFAULT NULL, *** - *** null
							  `write_request_id` int(11) DEFAULT NULL, *** - *** null
							  `artifact_ref_id` int(11) DEFAULT NULL, *** - *** null
							 */
				
							
							artifact = new org.ishafoundation.dwaraapi.db.model.transactional.Artifact();
			//				artifact1.setFileCount(fileCount);
							artifact.setName(toBeArtifactName);
							artifact.setPrevSequenceCode(prevSeqCode);
							artifact.setSequenceCode(sequenceCode);
			//				artifact1.setTotalSize(size);
							artifact.setArtifactclass(artifactclass);
							artifact.setqLatestRequest(request);
							
							artifact = (org.ishafoundation.dwaraapi.db.model.transactional.Artifact) artifactDao.save(artifact);
							artifactImportStatus = ImportStatus.completed;
							logger.debug("Artifact " + artifact.getId() + " imported to dwara succesfully");
						}else {
							toBeArtifactName = artifact.getName();
							artifactImportStatus = ImportStatus.skipped;
							logger.debug("Artifact " + artifact.getId() + " already exists, so skipping updating DB");  // artifact nth copy / rerun scenario
						}
						logger.info("*** Artifact " + artifact.getId() + " ***");
						logger.info("Artifact - " + artifactImportStatus);
						if(avi.getArtifactId() == null)
							avi.setArtifactId(artifact.getId());
						
						if(avi.getArtifactId() != artifact.getId())
							throw new Exception("Something wrong with avi.artifactId : Expected - " + artifact.getId() + " actual - " + avi.getArtifactId());
						/*
						 * creating artifact_volume
						 * 
						  
						  `artifact_id` int(11) NOT NULL,
						  `details` json DEFAULT NULL, *** - ***  {"archive_id": "5f76113cacf1", "end_volume_block": 79866, "start_volume_block": 2}
						  `name` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
						  `volume_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
						  `job_id` int(11) DEFAULT NULL, *** - *** null
						  `status` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL, *** - *** current
						 */
						
					    ArtifactVolume artifactVolume = artifactVolumeDao.findByIdArtifactIdAndIdVolumeId(artifact.getId(), volume.getId());
					    
					    if(artifactVolume == null) {
					    	artifactVolume = new ArtifactVolume(artifact.getId(), volume);
					    
						    artifactVolume.setName(artifactNameAsInCatalog); // NOTE : Dont be tempted to change this to toBeArtifactName - whatever in volume needs to go here...
						    if(volume.getStoragelevel() == Storagelevel.block) {
							    ArtifactVolumeDetails artifactVolumeDetails = new ArtifactVolumeDetails();
							    
							    // artifactVolumeDetails.setArchiveId(archiveId);
							    artifactVolumeDetails.setStartVolumeBlock(nthArtifact.getStartblock());
							    artifactVolumeDetails.setEndVolumeBlock(nthArtifact.getEndblock());
							    
							    artifactVolume.setDetails(artifactVolumeDetails);
						    }
						    // updating this upon all updates are successful - artifactVolume.setStatus(ArtifactVolumeStatus.current);
						    artifactVolume = artifactVolumeDao.save(artifactVolume);
						    artifactVolumeImportStatus = ImportStatus.completed;
						    
					    }else {
					    	artifactVolumeImportStatus = ImportStatus.skipped;
					    	logger.debug("ArtifactVolume for " + artifact.getId() + ":" + volume.getId() + " already exists, so skipping updating DB"); // rerun scenario
					    }
					    logger.info("ArtifactVolume - " + artifactVolumeImportStatus);
					    long artifactTotalSize = 0;
					    
					    List<org.ishafoundation.dwaraapi.storage.storagelevel.block.index.File> artifactFileList = nthArtifact.getFile();
					    ArrayList<ImportStatus> fileRecordsImportStatus = new ArrayList<ImportStatus>();
					    ArrayList<ImportStatus> fileVolumeRecordsImportStatus = new ArrayList<ImportStatus>();
					    ArrayList<String> missingFilepathnameList = new ArrayList<String>();
					    ArrayList<String> differingSizeList = new ArrayList<String>();
					    ArrayList<String> junkFilepathnameList = new ArrayList<String>();
					    int fileCount = artifactFileList.size();
					    for (org.ishafoundation.dwaraapi.storage.storagelevel.block.index.File nthFile : artifactFileList) {
					    	if(nthFile.getName().equals(artifactNameAsInCatalog))
					    		artifactTotalSize = nthFile.getSize(); 
					    	
					    	boolean fileChildOfJunkFolder = false;
					    	for (String junkFilepathname : junkFilepathnameList) { // if files belong to a junk folder skip it too
						    	if(nthFile.getName().startsWith(junkFilepathname)) {
						    		fileCount--;
						    		fileChildOfJunkFolder = true;
						    		break;
						    	}
							}
					    	
					    	if (fileChildOfJunkFolder)
					    		continue;
					    	
					    	if(isJunk(nthFile.getName())) {
					    		junkFilepathnameList.add(nthFile.getName());
					    		fileCount--;
					    		continue;
					    	}
							String filePathname = nthFile.getName().replace(artifactNameAsInCatalog, toBeArtifactName);
							String linkName = null;
							if(filePathname.contains(bruLinkSeparator)) {
								linkName = StringUtils.substringAfter(filePathname, bruLinkSeparator);
								filePathname = StringUtils.substringBefore(filePathname, bruLinkSeparator);
								
								logger.trace("filePathName "+ filePathname);
								logger.trace("linkName "+ linkName);
							}
							
							if(artifactclass.getConfig() != null) {
								String pathnameRegex = artifactclass.getConfig().getPathnameRegex();
								
								String filePathnameMinusArtifactName = filePathname.replace(toBeArtifactName, "");
								if(StringUtils.isNotBlank(filePathnameMinusArtifactName)) { // artifact folder needed to be added - hence the notblank condition
									if(filePathnameMinusArtifactName.startsWith(FilenameUtils.separatorsToUnix(File.separator))) {
										filePathnameMinusArtifactName = filePathnameMinusArtifactName.substring(1);
									}
									if(!filePathnameMinusArtifactName.matches(pathnameRegex)) {
										logger.trace("Doesnt match " + pathnameRegex + " regex for " + filePathnameMinusArtifactName);
										continue;
									}
								}
							}
							
							byte[] filePathnameChecksum = ChecksumUtil.getChecksum(filePathname);
							org.ishafoundation.dwaraapi.db.model.transactional.File file = null;
							if(artifactAlreadyExists) { // if artifactAlreadyExists - file would also exist already - copy / rerun scenario
								file = fileDao.findByPathnameChecksum(filePathnameChecksum);
								// Maybe we should import oldest tapes first
								// for eg., if P16539L6 is imported first followed by CA4485L4 which is the oldest of 2 then we would face this situation as
								// sequence codes 6028/9 and 30 has differences in the file count...
								// junk files difference...
								if(file == null) { 
									// Artifact exists means files should also tally - if there is a mismatch in files then it needs investigation...
									// Pls check out https://jira.isha.in/browse/DU-872 for more observations documented...
									missingFilepathnameList.add(filePathname);
									fileRecordsImportStatus.add(ImportStatus.failed);
									logger.error("Already existing artifact " + artifact.getId() + " supposed to have but missing " + filePathname);
								}else if (file.getSize() != nthFile.getSize()){
									differingSizeList.add(filePathname);
									fileRecordsImportStatus.add(ImportStatus.failed);
									logger.error("File size differs for " + filePathname + " Expected " + file.getSize() + " Actual " + nthFile.getSize());
								}
								else {
									fileRecordsImportStatus.add(ImportStatus.skipped);
									logger.debug("File " + filePathname + " already exists, so skipping updating DB");
								}
							}
							
							if(file == null) { // if artifactAlready doesnt Exists or if artifactAlreadyExists but file supposed to be there but not there then create the file...
								/*
								 * creating file1
								 * 
								  
								  `id` int(11) NOT NULL,
								  `checksum` varbinary(32) DEFAULT NULL, *** - *** null
								  `deleted` bit(1) DEFAULT NULL,
								  `pathname` varchar(4096) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
								  `pathname_checksum` varbinary(20) DEFAULT NULL,
								  `size` bigint(20) DEFAULT NULL,
								  `artifact_id` int(11) DEFAULT NULL,
								  `file_ref_id` int(11) DEFAULT NULL, *** - *** null
								  `directory` bit(1) DEFAULT NULL,
								  `symlink_file_id` int(11) DEFAULT NULL, *** - *** null
								  `symlink_path` varchar(4096) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL, *** - *** null
								*/  
								file = new org.ishafoundation.dwaraapi.db.model.transactional.File();
				
				
								file.setPathname(filePathname);
								file.setPathnameChecksum(filePathnameChecksum);
								file.setSize(nthFile.getSize()); // TODO Note junk file size is not reduced. 
								//file.setSymlinkFileId();
								file.setSymlinkPath(linkName);
								file.setArtifact(artifact);
								if(Boolean.TRUE.equals(nthFile.getDirectory())) {// if(StringUtils.isBlank(FilenameUtils.getExtension(filePathname))) {  // TODO - change it to - if(nthFile.isDirectory()) 
									file.setDirectory(true);
									fileCount--;
								}	
								
								file = fileDao.save(file);
								fileRecordsImportStatus.add(ImportStatus.completed);
						    	logger.debug("File " + filePathname + "  created successfully");
							}
							
							if(file == null) {
								fileVolumeRecordsImportStatus.add(ImportStatus.failed);
							}
							else {
								FileVolume fileVolume = fileVolumeDao.findByIdFileIdAndIdVolumeId(file.getId(), volume.getId());
								if(fileVolume == null) {
									/*
									 * file1_volume
									 *
					
									  `file_id` int(11) NOT NULL,
									  `archive_block` bigint(20) DEFAULT NULL, 
									  `deleted` bit(1) DEFAULT NULL, *** - *** 0
									  `encrypted` bit(1) DEFAULT NULL, *** - *** null
									  `verified_at` datetime(6) DEFAULT NULL, *** - *** null
									  `volume_start_block` int(11) DEFAULT NULL, *** - *** dynamic
									  `volume_end_block` int(11) DEFAULT NULL, *** - *** dynamic
									  `volume_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL, 
									  `header_blocks` int(11) DEFAULT NULL, *** - *** null
									  `hardlink_file_id` int(11) DEFAULT NULL, *** - *** null???
									 */
									fileVolume = new FileVolume(file.getId(), volume);// lets just let users use the util consistently
									fileVolume.setArchiveBlock(nthFile.getArchiveblock());
									fileVolume.setVolumeStartBlock(nthFile.getVolumeStartBlock());
									fileVolume.setVolumeEndBlock(nthFile.getVolumeEndBlock());
					
									//fileVolume.setHardlinkFileId(file.getId());
									
							    	fileVolumeDao.save(fileVolume);
							    	fileVolumeRecordsImportStatus.add(ImportStatus.completed);
							    	logger.debug("FileVolume for " + file.getId() + ":" + volume.getId() + " record created successfully");
							    }
								else {
									logger.debug("FileVolume for " + file.getId() + ":" + volume.getId() + " already exists, so skipping updating DB"); // rerun scenario
									fileVolumeRecordsImportStatus.add(ImportStatus.skipped);
									// break; // This means this is a rerun scenario and so the rest of the files can be skipped....
								}
							}
						}
					    
					    fileImportStatus = ImportStatusUtil.getStatus(fileRecordsImportStatus);
					    fileVolumeImportStatus = ImportStatusUtil.getStatus(fileVolumeRecordsImportStatus);
					    if(fileImportStatus == ImportStatus.failed) {
					    	avi.setMessage("Investigate missing files : " + missingFilepathnameList.toString()); 
					    	logger.error("Investigate " + missingFilepathnameList.toString());
					    }
					    else
					    	logger.info("File - " + fileImportStatus);
						logger.info("FileVolume - " + fileVolumeImportStatus);
						
						// UPDATING THE artifactvolumestatus only when everything is OK, so its used for restoring   
						if(artifactVolumeImportStatus == ImportStatus.completed) {
							ArtifactVolumeStatus artifactVolumeStatus = ArtifactVolumeStatus.current;
	
							// If already an entry for this pool/group is available (eg. 68*[C16805L6] is migration of 4*[C14023L4]) for this artifact - retire the oldest generation
							ArtifactVolume alreadyExistingArtifactVolume = artifactVolumeDao.findByIdArtifactIdAndVolumeGroupRefCopyIdAndStatus(artifact.getId(), volume.getGroupRef().getCopy().getId(), ArtifactVolumeStatus.current);
							if(alreadyExistingArtifactVolume != null) {
								int alreadyExistingArtifactVolumeGen = Integer.parseInt(StringUtils.substringAfter(alreadyExistingArtifactVolume.getVolume().getStoragesubtype(), "-"));
								int currentVolumeGen =  Integer.parseInt(StringUtils.substringAfter(volume.getStoragesubtype(), "-"));
	
								// check out the latest generation and use the most latest
								if(currentVolumeGen > alreadyExistingArtifactVolumeGen) { // if current volume is the latest - delete the oldest generation
									artifactVolumeStatus = ArtifactVolumeStatus.current;
									// flagging the older generation as deleted
									alreadyExistingArtifactVolume.setStatus(ArtifactVolumeStatus.deleted);
									artifactVolumeDao.save(alreadyExistingArtifactVolume);
								}
								else
									artifactVolumeStatus = ArtifactVolumeStatus.deleted;
							}
							 
						    artifactVolume.setStatus(artifactVolumeStatus);
						    artifactVolume = artifactVolumeDao.save(artifactVolume);
						}
						
						// updating artifact.filecount and size
						if(!artifactAlreadyExists) {
							artifact.setFileCount(fileCount);
							artifact.setTotalSize(artifactTotalSize);
							artifact = (org.ishafoundation.dwaraapi.db.model.transactional.Artifact) artifactDao.save(artifact);
						}
	
						respArtifact.setId(artifact.getId());
						respArtifact.setName(artifact.getName());
						
						usedCapacity += artifact.getTotalSize();
						if(fileImportStatus == ImportStatus.failed || fileVolumeImportStatus == ImportStatus.failed)
							avi.setStatus(Status.failed);
						else 
							avi.setStatus(Status.completed); // TODO - should we add - artifact id, DB artifact name,  artifactImportStatus, artifactVolumeImportStatus, fileImportStatus, fileVolumeImportStatus (needs rerun id)
						
						tArtifactVolumeImportDao.save(avi);
					}catch (Exception e) {
						logger.error("Unable to import completely " + artifactNameAsInCatalog, e);
						avi.setMessage(e.getMessage()); 
						avi.setStatus(Status.failed);
						tArtifactVolumeImportDao.save(avi);
					}
				}
				respArtifact.setArtifactStatus(artifactImportStatus);
				respArtifact.setArtifactVolumeStatus(artifactVolumeImportStatus);
				respArtifact.setFileStatus(fileImportStatus);
				respArtifact.setFileVolumeStatus(fileVolumeImportStatus);
				artifacts.add(respArtifact);
			}
			
			volume.setUsedCapacity(usedCapacity);
			volume = volumeDao.save(volume);
			
			Map<String,Integer> sameErrorMsg_Cnt = new HashMap<String, Integer>();
			List<TArtifactVolumeImport> aviList = tArtifactVolumeImportDao.findAllByIdVolumeId(volumeId);
			List<Status> statusList = new ArrayList<Status>();
			for (TArtifactVolumeImport nthAvi : aviList) {
				Status aviStatus = nthAvi.getStatus();
				statusList.add(aviStatus);
				
				if(aviStatus == Status.failed) {
					String failureMessage = StringUtils.substringBefore(nthAvi.getMessage(), ":");
					Integer cnt = 1;
					if(sameErrorMsg_Cnt.containsKey(failureMessage)) {
						cnt = sameErrorMsg_Cnt.get(failureMessage) + 1;
					}
					sameErrorMsg_Cnt.put(failureMessage, cnt);
				}
			}
			Status tapeImportStatus = StatusUtil.getStatus(statusList); // NOTE If there is any update in iva.status then we need to update the Request status appropriately
			
			if(tapeImportStatus == Status.completed)
				tArtifactVolumeImportDao.deleteAll(aviList);

			if(sameErrorMsg_Cnt.size() > 0) {
				StringBuffer sb = new StringBuffer();
				for (String failureMessage : sameErrorMsg_Cnt.keySet()) {
					sb.append("Has " + sameErrorMsg_Cnt.get(failureMessage) + " " + failureMessage + " errors . ");
				}
				request.setMessage(sb.toString());
			}
			
			request.setStatus(tapeImportStatus);
			request.setCompletedAt(LocalDateTime.now());
			requestDao.save(request);
		
			ir.setUserRequestId(request.getId());
			ir.setVolumeId(volumeId);
			ir.setVolumeImportStatus(tapeImportStatus.toString());
			ir.setArtifacts(artifacts);

		}
		catch (DwaraException e) {
			logger.error("Unable to import xml " + e.getMessage(), e);
			throw e; // handled in controller
		}
		catch (Exception e) {
			logger.error("Unable to import xml " + e.getMessage(), e);
			if(request != null) {
				request.setStatus(Status.failed);
				requestDao.save(request);
				ir.setUserRequestId(request.getId());
			}
			
			ir.setErrors(errorList);
		}
		return ir;
	}

	private Volumeindex validateAndGetVolumeindex(File xmlFile) throws Exception{
		XmlMapper xmlMapper = new XmlMapper();
		//Get XMLOutputFactory instance.
		XMLOutputFactory xmlOutputFactory = xmlMapper.getFactory().getXMLOutputFactory();
	    String propName = com.ctc.wstx.api.WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL;
	    xmlOutputFactory.setProperty(propName, true);
	    xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
	    xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
	    Volumeindex volumeindex =  null;
	    try {
	    	volumeindex = xmlMapper.readValue(xmlFile, Volumeindex.class);
		} catch (Exception e) {
			logger.error(xmlFile.getAbsolutePath() + " not a valid xml", e);
			throw new DwaraException(xmlFile.getAbsolutePath() + " not a valid xml. " + e.getMessage());
		}
	    
	    // More validation on values...
		List<Artifactclass> artifactclassList = configurationTablesUtil.getAllArtifactclasses();
		for (Artifactclass nthArtifactclass : artifactclassList) {
			id_artifactclassMap.put(nthArtifactclass.getId(), nthArtifactclass);
		}

		// validate volumeInfo
		Volumeinfo volumeinfo = volumeindex.getVolumeinfo();
		
	    // validate barcode
		String regEx = "(C|P)([A-Z])?([0-9]*)L[0-9]";
		Pattern regExPattern = Pattern.compile(regEx);
		String volumeBarcode = volumeinfo.getVolumeuid();
		Matcher regExMatcher = regExPattern.matcher(volumeBarcode);
		if(!regExMatcher.matches()) {
			throw new DwaraException("Volume barcode should be in " + regEx + " format");
		}
		
		String volumeGroupId = StringUtils.substring(volumeBarcode, 0, 2); 
		Optional<Volume> volOptional = volumeDao.findById(volumeGroupId);
		
		if(!volOptional.isPresent()) {
			throw new DwaraException("Volume Group " + volumeGroupId + " not supported. Either add the volume group in DB or fix the barcode in xml");
		}

	    
	    // validate artifactclass in all artifacts - get size too
		List<Artifact> artifactList = volumeindex.getArtifact();
		for (Artifact artifact : artifactList) {
			Artifactclass artifactclass = id_artifactclassMap.get(artifact.getArtifactclassuid());
			if(artifactclass == null) {
				Error err = new Error();
				err.setType(Errortype.Error);
				err.setMessage(artifact.getName() + " has invalid artifactclass " + artifact.getArtifactclassuid());
				errorList.add(err);
			}
		}
		
		if(errorList.size() > 0)
			throw new Exception("XML has artifacts with invalid artifactclasses");
		
	    // 
		return volumeindex;
	}
	
	
	
	/**
	 *   
  
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL, *** - *** Barcode
  `capacity` bigint(20) DEFAULT NULL, *** - ***  4/5/6 gen based size in bytes
  `checksumtype` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL, *** - ***  null
  `details` json DEFAULT NULL, *** - *** {"barcoded": true, "blocksize": ???}
  `finalized` bit(1) DEFAULT NULL, *** - *** ?
  `imported` bit(1) DEFAULT NULL, *** - *** true
  `initialized_at` datetime(6) DEFAULT NULL, *** - *** null
  `storagelevel` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL, *** - *** HC block
  `storagesubtype` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL, *** - *** 4/5/6
  `storagetype` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL, *** - *** tape
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL, *** - *** physical
  `uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL, *** - *** null ???
  `archiveformat_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL, *** - *** bru
  `copy_id` int(11) DEFAULT NULL,*** - *** null
  `group_ref_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,*** - *** Dynamic Based on nth copy and artifactclass  will use the group
  `location_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL, *** - *** Null
  `sequence_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL, *** - *** null
  `finalized_at` datetime(6) DEFAULT NULL, *** - *** null
  `used_capacity` bigint(20) DEFAULT NULL, *** - *** null
  `healthstatus` varchar(255) DEFAULT NULL, *** - *** normal
  `lifecyclestage` varchar(255) DEFAULT NULL, *** - *** active
  
	 * @return
	 * @throws Exception 
	 */
	private Volume getVolume(Volumeinfo volumeinfo) throws Exception {
		Volume volume = new Volume();
		
		String volumeBarcode = volumeinfo.getVolumeuid();
		volume.setId(volumeBarcode);
		volume.setUuid(UUID.randomUUID().toString());
		volume.setType(Volumetype.physical);
		String volumeGroupId = StringUtils.substring(volumeBarcode, 0, 2); 
		Optional<Volume> volOptional = volumeDao.findById(volumeGroupId);
		
		if(volOptional.isPresent()) {
			Volume volumeGroup = volOptional.get();
			volume.setGroupRef(volumeGroup);
		}else {
			throw new Exception("Volume Group " + volumeGroupId + " not supported. Either add the volume group in DB or fix the barcode in xml");
		}
/*
		String checksumalgorithm = configuration.getChecksumType();

		volume.setChecksumtype(Checksumtype.valueOf(checksumalgorithm));//Checksumtype.getChecksumtype(checksumalgorithm));
		volume.setFinalized(false);
		*/
		volume.setImported(true);
		String storagesubtype = null;
		String storagesubtypeSuffix = StringUtils.substring(volumeBarcode, volumeBarcode.length()-2, volumeBarcode.length());
		Set<String> storagesubtypeSet = storagesubtypeMap.keySet();
		for (String nthStoragesubtypeImpl : storagesubtypeSet) {
			if(storagesubtypeSuffix.equals(storagesubtypeMap.get(nthStoragesubtypeImpl).getSuffixToEndWith())) {
				storagesubtype = nthStoragesubtypeImpl;
				break;
			}
		}
		if(storagesubtype == null)
			throw new Exception(storagesubtypeSuffix + " storagesubtype not supported in dwara");
		volume.setStoragesubtype(storagesubtype);
		
		AbstractStoragesubtype storagesubtypeImpl = storagesubtypeMap.get(storagesubtype);
		volume.setCapacity(storagesubtypeImpl.getCapacity());
	
		// setting to default location
		Location location = configurationTablesUtil.getDefaultLocation();
		volume.setLocation(location);

		// Inherited from group
		volume.setStoragetype(Storagetype.tape);
		volume.setStoragelevel(Storagelevel.block);
		volume.setArchiveformat(configurationTablesUtil.getArchiveformat(volumeinfo.getArchiveformat()));
		
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
//		LocalDateTime writtenAt = LocalDateTime.parse(volumeinfo.getImported().getWrittenAt(), formatter);
		
		VolumeDetails volumeDetails = new VolumeDetails();
		volumeDetails.setBarcoded(true);
		volumeDetails.setBlocksize(volumeinfo.getVolumeblocksize());
		volumeDetails.setWrittenAt(volumeinfo.getImported().getWrittenAt());

		volume.setDetails(volumeDetails);
		return volume;
	}
	
	
	private boolean isJunk(String filePathname) {
		boolean isJunk=false;
		for (Iterator<Pattern> iterator2 = excludedFileNamesRegexList.iterator(); iterator2.hasNext();) {
			// TODO : See if we can use PathMatcher than regex.Matcher
			Pattern nthJunkFilesFinderRegexPattern = iterator2.next();
			Matcher m = nthJunkFilesFinderRegexPattern.matcher(FilenameUtils.getBaseName(filePathname));
			if(m.matches()) {
				isJunk=true;
			}
		}
		return isJunk;
	}
}
