package org.ishafoundation.dwaraapi.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.api.req._import.BulkImportRequest;
import org.ishafoundation.dwaraapi.api.req._import.ImportRequest;
import org.ishafoundation.dwaraapi.api.req._import.SetSequenceImportRequest;
import org.ishafoundation.dwaraapi.api.resp._import.ImportResponse;
import org.ishafoundation.dwaraapi.artifact.ArtifactMeta;
import org.ishafoundation.dwaraapi.artifact.ArtifactUtil;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.SequenceDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional._import.jointables.FileVolumeDiffDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.ArtifactVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.FileVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.ImportDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional._import.jointables.FileVolumeDiff;
import org.ishafoundation.dwaraapi.db.model.transactional._import.jointables.Import;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.ArtifactVolumeDetails;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.ArtifactVolumeStatus;
import org.ishafoundation.dwaraapi.enumreferences.DiffValues;
import org.ishafoundation.dwaraapi.enumreferences.ImportStatus;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Storagelevel;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.staged.scan.BasicArtifactValidator;
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
	protected FileVolumeDiffDao fileVolumeDiffDao;
	
	@Autowired
	protected ImportDao importDao;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private Map<String, AbstractStoragesubtype> storagesubtypeMap;
		
	@Autowired
	private ArtifactUtil artifactUtil;
	
	@Autowired
	private BasicArtifactValidator basicArtifactValidator;

	private List<Pattern> excludedFileNamesRegexList = new ArrayList<Pattern>();
	private String invalidDirName = "invalid"; // invalid xml goes here
	private String completedDirName = "completed"; // successfully completed xmls goes here
	
	private String bruLinkSeparator = Character.toString(Character.MIN_VALUE);
	
	private Map<String, Artifactclass> id_artifactclassMap = null;
	private List<Error> errorList = null;
	private List<org.ishafoundation.dwaraapi.api.resp._import.Artifact> artifacts = null;
	private Pattern allowedChrsInFileNamePattern = null;
	
	@PostConstruct
	public void getExcludedFileNamesRegexList() {
		String regexAllowedChrsInFileName = configuration.getRegexAllowedChrsInFileName();
		allowedChrsInFileNamePattern = Pattern.compile(regexAllowedChrsInFileName);
		
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
				importResponse.setVolumeImportFailureReason(nthImportResponse.getVolumeImportFailureReason());
				importResponse.setRunCount(nthImportResponse.getRunCount());
				
				if(nthImportResponse.getVolumeImportStatus().equals(ImportStatus.completed.toString()))
					destDir = completedDirPath; // move the imported catalog to completed folder - eg., /data/dwara/import-staging/completed
				else
					destDir = invalidDirPath; // if there are errors move the catalog to invalid folder

				if(nthImportResponse.getErrors() != null && nthImportResponse.getErrors().size() > 0) {
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

	public ImportResponse importCatalog(ImportRequest importRequest) throws Exception{	
		id_artifactclassMap = new HashMap<String, Artifactclass>();
		errorList = new ArrayList<Error>();
		artifacts = new ArrayList<org.ishafoundation.dwaraapi.api.resp._import.Artifact>();
		
		ImportResponse ir = new ImportResponse();
		Request request = null;
		try {
			String xmlPathname = importRequest.getXmlPathname();
			File xmlFile = FileUtils.getFile(xmlPathname);
	
			Volumeindex volumeindex = getVolumeindex(xmlFile);	
			Volumeinfo volumeInfo = volumeindex.getVolumeinfo();
			String volumeId = volumeInfo.getVolume();
			ir.setVolumeId(volumeId);
			
			validate(volumeindex);
			
			// TODO - move this to validateAndGetVolumeindex method
			Request alreadyCompletelyImportedVolumeRequest = requestDao.findActionIsImportAndStatusIsCompletedAndVolume_Native(volumeId);
			if(alreadyCompletelyImportedVolumeRequest != null) {
				String msg = volumeId + " already imported successfully";
				Error err = new Error();
				err.setType(Errortype.Error);
				err.setMessage(msg);
				errorList.add(err);
				throw new DwaraException(msg);
			}

			List<Request> importRequestList = requestDao.findAllByActionIsImportAndVolume_Native(volumeId);
			int runId = importRequestList.size() + 1;
			
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("xmlPathname", importRequest.getXmlPathname());
			
			request = createUserRequest(Action._import, Status.in_progress, data);
			
		    // update Import table with all entries from xml... // for reruns only delta is added here...
			createImportEntries(volumeindex, volumeId, request, runId);
			
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
			
			iterateCatalogArtifacts(volumeindex, volume, runId, request);
			
			Map<String,Integer> sameErrorMsg_Cnt = new HashMap<String, Integer>();
			List<Import> importList = importDao.findAllByVolumeIdAndRequeueId(volumeId, runId);
			List<Status> statusList = new ArrayList<Status>();
			for (Import nthImport : importList) {
				Status nthImportStatus = nthImport.getStatus();
				statusList.add(nthImportStatus);
				
				if(nthImportStatus == Status.failed || nthImportStatus == Status.completed_failures) {
					String failureMessage = StringUtils.substringBefore(nthImport.getMessage(), ":");
					Integer cnt = 1;
					if(sameErrorMsg_Cnt.containsKey(failureMessage)) {
						cnt = sameErrorMsg_Cnt.get(failureMessage) + 1;
					}
					sameErrorMsg_Cnt.put(failureMessage, cnt);
				}
			}
			Status tapeImportStatus = StatusUtil.getStatus(statusList); // NOTE If there is any update in iva.status then we need to update the Request status appropriately
			
			StringBuffer failureReason = new StringBuffer();
			if(sameErrorMsg_Cnt.size() > 0) {
				for (String failureMessage : sameErrorMsg_Cnt.keySet()) {
					failureReason.append("Has " + sameErrorMsg_Cnt.get(failureMessage) + " " + failureMessage + " errors . ");
				}
				request.setMessage(failureReason.toString());
			}
			
			request.setStatus(tapeImportStatus);
			request.setCompletedAt(LocalDateTime.now());
			requestDao.save(request);
		
			ir.setUserRequestId(request.getId());
			ir.setRunCount(runId);
			ir.setVolumeImportStatus(tapeImportStatus.toString());
			if(failureReason.length() > 0)
				ir.setVolumeImportFailureReason(failureReason.toString());
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
	
	private Volumeindex getVolumeindex(File xmlFile) throws Exception{
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
	    return volumeindex;
	}
	
	private void validate(Volumeindex volumeindex) throws Exception {
	    
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
		String volumeBarcode = volumeinfo.getVolume();
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
			Artifactclass artifactclass = id_artifactclassMap.get(artifact.getArtifactclass());
			if(artifactclass == null) {
				Error err = new Error();
				err.setType(Errortype.Error);
				err.setMessage(artifact.getName() + " has invalid artifactclass " + artifact.getArtifactclass());
				errorList.add(err);
			}
			else {
				String fileName = artifact.getRename() != null ? artifact.getRename() : artifact.getName();
				if(!artifactclass.getId().startsWith("photo")) { // validation only for photo* artifactclass
					// 1- validateName
					List<Error> errorListFromValidator = basicArtifactValidator.validateName(fileName, allowedChrsInFileNamePattern);
					for (Error nthError : errorListFromValidator) {
						nthError.setMessage(fileName + " - " + nthError.getMessage());
						errorList.add(nthError);
					}
				}
				else {
					// 1a - validateName for photo* artifactclass
					//errorList.addAll(basicArtifactValidator.validatePhotoName(fileName, allowedChrsInFileNamePattern, (sfv != null && sfv.getPhotoSeriesFileNameValidationFailedFileNames().size() > 0 ? sfv.getPhotoSeriesFileNameValidationFailedFileNames() : null)));
				}
// not needed				
//				// 2- validateCount
//				errorList.addAll(basicArtifactValidator.validateFileCount(fileCount));
//	
//				// 3- validateSize
//				errorList.addAll(basicArtifactValidator.validateFileSize(size));
			}
		}

		if(errorList.size() > 0)
			throw new Exception("XML has invalid artifacts");

	}
	
	private void createImportEntries(Volumeindex volumeindex, String volumeId, Request request, int runId){	
		List<Artifact> artifactList = volumeindex.getArtifact();
		for (Artifact nthArtifact : artifactList) {
			List<Import> artifactImportList = importDao.findAllByVolumeIdAndArtifactName(volumeId, nthArtifact.getName());
			boolean isArtifactAlreadyComplete = false;
			if(artifactImportList!= null && artifactImportList.size() > 0) {
				for (Import nthArtifactImport : artifactImportList) {
					if(nthArtifactImport.getStatus() == Status.completed || nthArtifactImport.getStatus() == Status.marked_completed) {
						isArtifactAlreadyComplete = true;
						break;
					}
				}
			}
			
			if(!isArtifactAlreadyComplete) { 
				Import importDBRecord = new Import();				
				importDBRecord.setVolumeId(volumeId);
				importDBRecord.setArtifactName(nthArtifact.getName());
				importDBRecord.setRequeueId(runId);
				importDBRecord.setStatus(Status.in_progress);
				importDBRecord.setRequest(request);
				importDao.save(importDBRecord);
			}
		}
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
		
		String volumeBarcode = volumeinfo.getVolume();
		volume.setId(volumeBarcode);
		volume.setUuid(volumeinfo.getVolumeuuid());
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
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		LocalDateTime firstWrittenAt = LocalDateTime.parse(volumeinfo.getFirstWrittenAt(), formatter);
		volume.setInitializedAt(firstWrittenAt);
		
		LocalDateTime lastWrittenAt = LocalDateTime.parse(volumeinfo.getLastWrittenAt(), formatter);
		volume.setFinalizedAt(lastWrittenAt);
		
		VolumeDetails volumeDetails = new VolumeDetails();
		volumeDetails.setBarcoded(true);
		volumeDetails.setBlocksize(volumeinfo.getVolumeblocksize());

		volume.setDetails(volumeDetails);
		return volume;
	}
	
	private void iterateCatalogArtifacts(Volumeindex volumeindex, Volume toBeImportedVolume, int runId, Request request){
		String volumeId = toBeImportedVolume.getId();
		long usedCapacity = 0;
		List<Artifact> artifactList = volumeindex.getArtifact();
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
	
			Import importTable = importDao.findByVolumeIdAndArtifactNameAndRequeueId(volumeId, artifactNameAsInCatalog, runId);

			if(importTable == null || importTable.getStatus() == Status.completed) { // already completed - so skip it
				// continue;
				artifactImportStatus = ImportStatus.skipped; 
				artifactVolumeImportStatus = ImportStatus.skipped;
				fileImportStatus = ImportStatus.skipped;
				fileVolumeImportStatus = ImportStatus.skipped;
			}
			else {
				try {
					
					/*
					 * 
					*** dealing with artifact ***
					*
					*/
					String extractedCodeFromProposedArtifactName = StringUtils.substringBefore(artifactNameProposed, "_");
					if(!artifactNameAsInCatalog.equals(artifactNameProposed) && !StringUtils.substringBefore(artifactNameAsInCatalog, "_").equals(extractedCodeFromProposedArtifactName))
						throw new Exception ("Different sequences in name and rename attributes not supported. @name - " + artifactNameAsInCatalog + " @rename - " + artifactNameProposed);

					Artifactclass artifactclass = id_artifactclassMap.get(nthArtifact.getArtifactclass());
					Sequence sequence = artifactclass.getSequence();
					
					// Checks if there is a custom artifactclass impl, else applies default extraction logic but doesnt generate new sequence
					ArtifactMeta am = artifactUtil.getArtifactMeta(artifactNameProposed, artifactclass.getId(), sequence, false);
					toBeArtifactName = am.getArtifactName();
					String prevSeqCode = am.getPrevSequenceCode();
					String sequenceCode =  am.getSequenceCode();
//					We are doing this on the custom class by figuring out all the missing sequencecodes one and assigning them some values or generating new sequence - check VideoRaw custom Artifactclass 
//					if(sequenceCode == null) {
//						throw new Exception("Missing expected code : " + artifactNameProposed);
//					}

					boolean artifactAlreadyExists = true;
					
					org.ishafoundation.dwaraapi.db.model.transactional.Artifact artifact = null;
					if(sequenceCode != null) {
						artifact = artifactDao.findBySequenceCodeAndDeletedIsFalse(sequenceCode);
					}
					else {
						String artifactNameProposedShavedOffPrefix = StringUtils.substringAfter(artifactNameProposed,"_");
						artifact = artifactDao.findByNameEndsWith(artifactNameProposedShavedOffPrefix);
					}
				
					if(artifact != null) { 
						// check if catalog artifactclass is same as existing artifact's artifactclass
						if(!artifact.getArtifactclass().getId().equals(artifactclass.getId())) {
							 String errMsg = "Different Artifactclasses for same artifact : ArtifactId " + artifact.getId() + " - " + artifactNameAsInCatalog + ". Expected - " + artifact.getArtifactclass().getId() + " Actual - " + artifactclass.getId();
							 throw new Exception (errMsg);
						}	
						
						// even if artifact extracted code matches - double check for name - and if name differs flag it
						String artifactNameShavedOffPrefix = StringUtils.substringAfter(artifact.getName(),"_");
						String artifactNameProposedShavedOffPrefix = StringUtils.substringAfter(artifactNameProposed,"_");
						if(!artifactNameProposedShavedOffPrefix.equals(artifactNameShavedOffPrefix))
							throw new Exception ("Same code but different artifact names : code - " + sequenceCode + " ArtifactId " + artifact.getId() + ". Expected - " + artifactNameShavedOffPrefix + " Actual - " + artifactNameProposedShavedOffPrefix);
					}
					
					if(artifact == null) { // Some imported artifacts has same name but different extracted codes... if so flag it
						String artifactNameProposedShavedOffPrefix = StringUtils.substringAfter(artifactNameProposed,"_");
						 List<org.ishafoundation.dwaraapi.db.model.transactional.Artifact> artifactsEndingWithSameName = artifactDao.findByNameEndsWithAndArtifactclassId(artifactNameProposedShavedOffPrefix,artifactclass.getId());
						 for (org.ishafoundation.dwaraapi.db.model.transactional.Artifact nthArtifactEndingWithSameName : artifactsEndingWithSameName) {
							 String artifactNameShavedOffPrefix = StringUtils.substringAfter(nthArtifactEndingWithSameName.getName(),"_");
							 if(artifactNameShavedOffPrefix.equals(artifactNameProposedShavedOffPrefix)) {
								 artifact = nthArtifactEndingWithSameName;
								 String errMsg = "Different codes but same artifact name : ArtifactId - " + artifact.getId() + " - " + artifactNameProposedShavedOffPrefix + ". Expected code - " + artifact.getSequenceCode() + " Actual - " + sequenceCode;//(prevSeqCode != null ? prevSeqCode : sequenceCode);
								 throw new Exception (errMsg);
							 }
						}
					}


					//TODO - should we double check with size too??? domainSpecificArtifactRepository.findAllByTotalSizeAndDeletedIsFalse(size);
					if(artifact == null) {
						artifactAlreadyExists = false;

//						org.ishafoundation.dwaraapi.db.model.transactional.Artifact alreadyExistingArtifactWithSameSequenceCode = artifactDao.findBySequenceCode(sequenceCode);// findBySequenceCodeAndDeletedIsFalse(sequenceCode);
//						if(alreadyExistingArtifactWithSameSequenceCode != null)
//							throw new Exception("An artifact already exists with sequenceCode : " + sequenceCode + ". Already existing artifactId with same sequenceCode - " + alreadyExistingArtifactWithSameSequenceCode.getId());
						
						if(sequenceCode == null) {
							am = artifactUtil.generateSequenceCodeAndPrefixToName(artifactNameProposed, sequence);
							sequenceCode = am.getSequenceCode();
							toBeArtifactName = am.getArtifactName();
						}
						/*
						 *** Creating artifact if not already in DB ***
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
					if(importTable.getArtifactId() == null)
						importTable.setArtifactId(artifact.getId());
					
					if(importTable.getArtifactId() != artifact.getId())
						throw new Exception("Something wrong with import.artifactId : Expected - " + artifact.getId() + " actual - " + importTable.getArtifactId());
	
					Volume masterVolume = getMasterVolume(artifact, toBeImportedVolume); // Getting artifact's master copy
					logger.info("masterVolume - " + masterVolume.getId());
					boolean isToBeImportedVolumeMaster = true; // the volume we are importing - is it the master volume?
				    if(!masterVolume.getId().equals(toBeImportedVolume.getId()))
				    	isToBeImportedVolumeMaster = false;
					
					/*
					 *** Creating artifact_volume ***
					 * 
					  
					  `artifact_id` int(11) NOT NULL,
					  `details` json DEFAULT NULL, *** - ***  {"archive_id": "5f76113cacf1", "end_volume_block": 79866, "start_volume_block": 2}
					  `name` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
					  `volume_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
					  `job_id` int(11) DEFAULT NULL, *** - *** null
					  `status` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL, *** - *** current
					 */
				    ArtifactVolume artifactVolume = artifactVolumeDao.findByIdArtifactIdAndIdVolumeId(artifact.getId(), toBeImportedVolume.getId());
				    if(artifactVolume == null) {
				    	artifactVolume = new ArtifactVolume(artifact.getId(), toBeImportedVolume);
				    
					    artifactVolume.setName(artifactNameAsInCatalog); // NOTE : Dont be tempted to change this to toBeArtifactName - whatever in volume needs to go here...
					    if(toBeImportedVolume.getStoragelevel() == Storagelevel.block) {
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
				    	logger.debug("ArtifactVolume for " + artifact.getId() + ":" + toBeImportedVolume.getId() + " already exists, so skipping updating DB"); // rerun scenario
				    }
				    logger.info("ArtifactVolume - " + artifactVolumeImportStatus);
				    
				    // collection to hold the extra files in DB but not in catalog
				    Map<Integer, org.ishafoundation.dwaraapi.db.model.transactional.File> fileId_FileObj_Map = new HashMap<Integer, org.ishafoundation.dwaraapi.db.model.transactional.File>();
				    ArrayList<ImportStatus> fileRecordsImportStatus = new ArrayList<ImportStatus>();
				    ArrayList<ImportStatus> fileVolumeRecordsImportStatus = new ArrayList<ImportStatus>();
				    ArrayList<String> missingFilepathnameList = new ArrayList<String>();
				    ArrayList<String> differingSizeList = new ArrayList<String>();


				    FileMeta fileMeta = dealCatalogFiles(nthArtifact, artifact, artifactNameAsInCatalog, toBeArtifactName, artifactclass, toBeImportedVolume, masterVolume, 
				    		isToBeImportedVolumeMaster, artifactAlreadyExists, fileId_FileObj_Map, fileRecordsImportStatus, fileVolumeRecordsImportStatus, missingFilepathnameList, differingSizeList);
	
				    fileImportStatus = ImportStatusUtil.getStatus(fileRecordsImportStatus);
				    fileVolumeImportStatus = ImportStatusUtil.getStatus(fileVolumeRecordsImportStatus);

			    	logger.info("File - " + fileImportStatus);
			    	logger.info("FileVolume - " + fileVolumeImportStatus);
			    	
					boolean hasDiffs = false;
					Set<Integer> extraMasterFileList = fileId_FileObj_Map.keySet();
					if(missingFilepathnameList.size() > 0 || extraMasterFileList.size() > 0 || differingSizeList.size() > 0)
						hasDiffs = true;

					// UPDATING THE artifactvolumestatus only when everything is OK, so its used for restoring
					if(artifactVolumeImportStatus == ImportStatus.completed) {
					    artifactVolume.setStatus(getArtifactVolumeStatus(toBeImportedVolume, isToBeImportedVolumeMaster, artifact.getId(), hasDiffs));
					    artifactVolume = artifactVolumeDao.save(artifactVolume);
					}

					
					// updating artifact.filecount and size
					if(isToBeImportedVolumeMaster && fileMeta != null) {
						artifact.setFileCount(fileMeta.getFileCount());
						artifact.setTotalSize(fileMeta.getTotalSize());
						artifact = (org.ishafoundation.dwaraapi.db.model.transactional.Artifact) artifactDao.save(artifact);
					}
	
					respArtifact.setId(artifact.getId());
					respArtifact.setName(artifact.getName());
					
					usedCapacity += artifact.getTotalSize();
					if(hasDiffs) {
				    	importTable.setMessage("Differences exist between artifact copies : ");
						importTable.setStatus(Status.completed_failures);
				    }
					else 
						importTable.setStatus(Status.completed);
					
					importDao.save(importTable);
				}catch (Exception e) {
					logger.error("Unable to import completely " + artifactNameAsInCatalog, e);
					importTable.setMessage(e.getMessage()); 
					importTable.setStatus(Status.failed);
				
					importDao.save(importTable);
				}
			}
			respArtifact.setArtifactStatus(artifactImportStatus);
			respArtifact.setArtifactVolumeStatus(artifactVolumeImportStatus);
			respArtifact.setFileStatus(fileImportStatus);
			respArtifact.setFileVolumeStatus(fileVolumeImportStatus);
			artifacts.add(respArtifact);
		
		}
		
		toBeImportedVolume.setUsedCapacity(usedCapacity);
		toBeImportedVolume = volumeDao.save(toBeImportedVolume);
	}

	private FileMeta dealCatalogFiles(Artifact nthArtifact, org.ishafoundation.dwaraapi.db.model.transactional.Artifact artifact, 
			String artifactNameAsInCatalog, String toBeArtifactName, Artifactclass artifactclass, 
			Volume volume, Volume masterVolume, boolean isToBeImportedVolumeMaster, boolean artifactAlreadyExists,
	    Map<Integer, org.ishafoundation.dwaraapi.db.model.transactional.File> fileId_FileObj_Map,
	    ArrayList<ImportStatus> fileRecordsImportStatus,
	    ArrayList<ImportStatus> fileVolumeRecordsImportStatus,
	    ArrayList<String> missingFilepathnameList,
	    ArrayList<String> differingSizeList) throws Exception {
		
	    List<org.ishafoundation.dwaraapi.storage.storagelevel.block.index.File> artifactFileList = nthArtifact.getFile();
	    int fileCount = artifactFileList.size();

	    // caching the file entries to avoid repeated queries...
	    List<org.ishafoundation.dwaraapi.db.model.transactional.File> masterFileList = fileDao.findAllByArtifactIdAndDeletedFalseAndDiffIsNull(artifact.getId());
	    // collection to cache the file Objects
	    Map<byte[], org.ishafoundation.dwaraapi.db.model.transactional.File> filePathnameChecksum_FileObj_Map = new HashMap<byte[], org.ishafoundation.dwaraapi.db.model.transactional.File>();
	    for (org.ishafoundation.dwaraapi.db.model.transactional.File nthMasterFile : masterFileList) {
	    	fileId_FileObj_Map.put(nthMasterFile.getId(), nthMasterFile);
	    	filePathnameChecksum_FileObj_Map.put(nthMasterFile.getPathnameChecksum(), nthMasterFile);
		}

	    ArrayList<String> junkFilepathnameList = new ArrayList<String>();
		for (org.ishafoundation.dwaraapi.storage.storagelevel.block.index.File nthFile : artifactFileList) {
				
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
				file = fileDao.findByPathnameChecksum(filePathnameChecksum); // filePathnameChecksum_FileObj_Map.get(filePathnameChecksum);
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
				}else {
					fileId_FileObj_Map.remove(file.getId()); // removing files already there and matches...
					if (file.getSize() != nthFile.getSize() && !file.isDirectory()){
						differingSizeList.add(filePathname);
						fileRecordsImportStatus.add(ImportStatus.failed);
						logger.error("File size differs for " + filePathname + " Expected " + file.getSize() + " Actual " + nthFile.getSize());
					}
					else {
						fileRecordsImportStatus.add(ImportStatus.skipped);
						logger.debug("File " + filePathname + " already exists, so skipping updating DB");
					}
				}	
			}
			
			DiffValues diffValueToBeUsedForFileVolume = null;
			long existingFileSizeInDB = 0L;
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
	
				if(missingFilepathnameList.contains(filePathname) && !isToBeImportedVolumeMaster) { // if file is missing in DB and if the catalog we are importing is not master then set diff = a
					diffValueToBeUsedForFileVolume = DiffValues.a;
					file.setDiff(diffValueToBeUsedForFileVolume);
				}
				file = fileDao.save(file);
				fileRecordsImportStatus.add(ImportStatus.completed);
				logger.debug("File " + filePathname + "  created successfully");
			}else { // file already in DB check if size differs
				existingFileSizeInDB = file.getSize();
				
				if(differingSizeList.contains(filePathname)) { // if file already exists and size differs and if catalog to be imported is master...
					diffValueToBeUsedForFileVolume = DiffValues.c;
					
					if(isToBeImportedVolumeMaster) {
						file.setSize(nthFile.getSize());
						file = fileDao.save(file);
					
						// get all fileVolumes... and update diff here
				    	List<FileVolume> fileVolumeList = fileVolumeDao.findAllByIdFileId(file.getId());
						for (FileVolume nthFileVolume : fileVolumeList) {
							if(!nthFileVolume.getVolume().getId().equals(masterVolume.getId())) {
								if(nthFileVolume.getDiff() != DiffValues.c) { // if not already updated...
									nthFileVolume.setDiff(DiffValues.c);
									fileVolumeDao.save(nthFileVolume); 
									
									FileVolumeDiff fileVolumeDiff = new FileVolumeDiff(file.getId(), nthFileVolume.getId().getVolumeId());
									fileVolumeDiff.setSize(existingFileSizeInDB);
									fileVolumeDiffDao.save(fileVolumeDiff);
								}
							}
						}
					}
				}
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
					if(diffValueToBeUsedForFileVolume != null && !isToBeImportedVolumeMaster) {
						fileVolume.setDiff(diffValueToBeUsedForFileVolume);
						
						if(diffValueToBeUsedForFileVolume == DiffValues.c) {
							FileVolumeDiff fileVolumeDiff = new FileVolumeDiff(file.getId(), volume.getId());
							fileVolumeDiff.setSize(nthFile.getSize());
							fileVolumeDiffDao.save(fileVolumeDiff);
						}
					}
						
	
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
		
	    // update the extra files in DB but not in current catalog appropriately
		dealExtraFilesInDBButNotInCatalog(fileId_FileObj_Map, masterVolume, isToBeImportedVolumeMaster);
		
		// find the missing ones and create filevolume entries
		masterFileList = fileDao.findAllByArtifactIdAndDeletedFalseAndDiffIsNull(artifact.getId());
		dealMissingFilesInDBButPresentInCatalog(artifact.getId(), masterFileList, masterVolume);
		
		FileMeta fm = null;
		if(isToBeImportedVolumeMaster)
			fm = calculateAndSetFolderSizes(artifact);
    	
    	return fm;
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

	
	private Volume getMasterVolume(org.ishafoundation.dwaraapi.db.model.transactional.Artifact artifact, Volume volume){
		Volume masterVolume = null;
		
		List<ArtifactVolume> currentMasterArtifactVolumeListAcrossCopies = artifactVolumeDao.findAllByIdArtifactIdAndStatus(artifact.getId(), ArtifactVolumeStatus.current);
		ArtifactVolume currentMasterArtifactVolume = null;
		int artifactVolumeRunningCount = 0; 
		for (ArtifactVolume nthArtifactVolume : currentMasterArtifactVolumeListAcrossCopies) {
			if(artifactVolumeRunningCount == 0) {
				currentMasterArtifactVolume	= nthArtifactVolume;
			}else {
				if(nthArtifactVolume.getId().getVolumeId().compareTo(currentMasterArtifactVolume.getId().getVolumeId()) < 0)
					currentMasterArtifactVolume	= nthArtifactVolume;	
			}
			artifactVolumeRunningCount++;
		}
		
		if(currentMasterArtifactVolume != null) {// if(!currentMasterArtifactVolume.getVolume().getId().equals(volume.getId())) {
			if(isOlder(volume, currentMasterArtifactVolume.getVolume())) 
				masterVolume = volume;
			else
				masterVolume = currentMasterArtifactVolume.getVolume();
		}
		else
			masterVolume = volume;
		
		return masterVolume;
	}

	private boolean isNewer(Volume volume1, Volume volume2) { // is Volume1 Newer than Volume2
		return !isOlder(volume1, volume2);
	}
	
	private boolean isOlder(Volume volume1, Volume volume2) { // is Volume1 Older than Volume2
		LocalDateTime volume1DateTime = volume1.getFinalizedAt();
		LocalDateTime volume2DateTime = volume2.getFinalizedAt();
	
		if(volume1DateTime.isEqual(volume2DateTime)) {
			if(volume1.getId().compareTo(volume2.getId()) < 0)
				return true;
			else
				return false;
		}
			
		
		if(volume1DateTime.isBefore(volume2DateTime))
			return true;
		
		return false;
	}


	private void dealExtraFilesInDBButNotInCatalog(Map<Integer, org.ishafoundation.dwaraapi.db.model.transactional.File> fileId_FileObj_Map, Volume masterVolume, boolean isToBeImportedVolumeMaster) {
		Set<Integer> extraMasterFileList = fileId_FileObj_Map.keySet();
		ArrayList<org.ishafoundation.dwaraapi.db.model.transactional.File> extraFileList = new ArrayList<org.ishafoundation.dwaraapi.db.model.transactional.File>(); 
		ArrayList<FileVolume> extraFileVolumeList = new ArrayList<FileVolume>(); 
		for (Integer nthFileId : extraMasterFileList) {
			org.ishafoundation.dwaraapi.db.model.transactional.File nthExtraFile = fileId_FileObj_Map.get(nthFileId);
			if(isToBeImportedVolumeMaster) {
				nthExtraFile.setDiff(DiffValues.a);
				extraFileList.add(nthExtraFile);
				
		    	List<FileVolume> fileVolumeList = fileVolumeDao.findAllByIdFileId(nthFileId);
				for (FileVolume nthFileVolume : fileVolumeList) {
					if(!nthFileVolume.getVolume().getId().equals(masterVolume.getId())) {
						if(nthFileVolume.getDiff() != DiffValues.a) { // if not already updated
							nthFileVolume.setDiff(DiffValues.a);
							extraFileVolumeList.add(nthFileVolume);		
						}
					}
				}
	
			}
		}
		fileDao.saveAll(extraFileList);
		fileVolumeDao.saveAll(extraFileVolumeList);
	}
	
	private void dealMissingFilesInDBButPresentInCatalog(int artifactId, List<org.ishafoundation.dwaraapi.db.model.transactional.File> masterFileList, Volume masterVolume) {
		List<ArtifactVolume> alreadyExistingArtifactVolumeList = artifactVolumeDao.findAllByIdArtifactId(artifactId);
		for (ArtifactVolume nthAlreadyExistingArtifactVolume : alreadyExistingArtifactVolumeList) {
			if(nthAlreadyExistingArtifactVolume.getId().getVolumeId().equals(masterVolume.getId()))
				continue;
	
			Set<Integer> fileIdList = new TreeSet<Integer>();
			List<FileVolume> alreadyExistingFileVolumeList = fileVolumeDao.findAllByIdVolumeId(nthAlreadyExistingArtifactVolume.getId().getVolumeId());
			for (FileVolume nthAlreadyExistingFileVolume : alreadyExistingFileVolumeList) {
				fileIdList.add(nthAlreadyExistingFileVolume.getId().getFileId());
			}
			for (org.ishafoundation.dwaraapi.db.model.transactional.File nthMasterFile : masterFileList) {
				if(!fileIdList.contains(nthMasterFile.getId())){
					FileVolume dummyFileVolume = new FileVolume(nthMasterFile.getId(), nthAlreadyExistingArtifactVolume.getVolume());
					dummyFileVolume.setDiff(DiffValues.d);
					fileVolumeDao.save(dummyFileVolume);
				}
			}
		}
	}
	
	
	private FileMeta calculateAndSetFolderSizes(org.ishafoundation.dwaraapi.db.model.transactional.Artifact artifact) {	
	    List<org.ishafoundation.dwaraapi.db.model.transactional.File> masterFileList = fileDao.findAllByArtifactIdAndDeletedFalseAndDiffIsNull(artifact.getId());

		// now lets calculate and collect subfolders size
		Map<String,Long> filePathnameVsSize_Map = new HashMap<String, Long>();
		for (org.ishafoundation.dwaraapi.db.model.transactional.File nthFile : masterFileList) {
			String nthFilepathname = nthFile.getPathname();
			if(Boolean.TRUE.equals(nthFile.isDirectory())){
				filePathnameVsSize_Map.put(nthFilepathname,0L);
			}else {
				String nthFileDirectoryName = FilenameUtils.getFullPathNoEndSeparator(nthFilepathname);
	
				for (String nthArtifactSubDirectory : filePathnameVsSize_Map.keySet()) {
					if(nthFileDirectoryName.contains(nthArtifactSubDirectory)) {
						Long size = filePathnameVsSize_Map.get(nthArtifactSubDirectory);
						size += nthFile.getSize();
						filePathnameVsSize_Map.put(nthArtifactSubDirectory,size);
					}
				}
			}
		}                 
	
		long artifactSize = 0L;
		int fileCount = masterFileList.size();
		List<org.ishafoundation.dwaraapi.db.model.transactional.File> fileListToBeDbUpdated = new ArrayList<org.ishafoundation.dwaraapi.db.model.transactional.File>();
		// now lets make use of the collected subfolder size 
		for (org.ishafoundation.dwaraapi.db.model.transactional.File nthFile : masterFileList) {
			if(Boolean.TRUE.equals(nthFile.isDirectory())){
				Long nthDirectorySize= filePathnameVsSize_Map.get(nthFile.getPathname());
				nthFile.setSize(nthDirectorySize);
	
				if(nthFile.getPathname().equals(artifact.getName())){
					artifactSize = nthDirectorySize;
				}
				fileListToBeDbUpdated.add(nthFile);
				fileCount--;
			}
		}
		
		if(fileListToBeDbUpdated.size() > 0)
			fileDao.saveAll(fileListToBeDbUpdated);
		

    	FileMeta fm = new FileMeta();
    	fm.setFileCount(fileCount);
    	fm.setTotalSize(artifactSize);

    	return fm;
	}
	
	private ArtifactVolumeStatus getArtifactVolumeStatus(Volume volumeToBeImported, boolean isToBeImportedVolumeMaster, int artifactId, boolean hasDiffs) {
		ArtifactVolumeStatus artifactVolumeStatus = ArtifactVolumeStatus.current;
		if(hasDiffs) {
			if(isToBeImportedVolumeMaster) {
				artifactVolumeStatus = ArtifactVolumeStatus.current;
				
				// all other artifact volume should have diffs
				List<ArtifactVolume> alreadyExistingArtifactVolumeList = artifactVolumeDao.findAllByIdArtifactId(artifactId);
				for (ArtifactVolume alreadyExistingArtifactVolume : alreadyExistingArtifactVolumeList) {
					alreadyExistingArtifactVolume.setStatus(ArtifactVolumeStatus.diffs);
					artifactVolumeDao.save(alreadyExistingArtifactVolume);
				}	
			}else {
				artifactVolumeStatus = ArtifactVolumeStatus.diffs;
			}
		}else {
				ArtifactVolume alreadyExistingArtifactVolume = artifactVolumeDao.findByIdArtifactIdAndVolumeGroupRefCopyIdAndStatus(artifactId, volumeToBeImported.getGroupRef().getCopy().getId(), ArtifactVolumeStatus.current);
				if(alreadyExistingArtifactVolume != null) { // same copy pool need to handle current/previous etc
					if(isNewer(volumeToBeImported, alreadyExistingArtifactVolume.getVolume())) {
							artifactVolumeStatus = ArtifactVolumeStatus.current;
							
							// flagging the older generation as previous
							alreadyExistingArtifactVolume.setStatus(ArtifactVolumeStatus.previous);
							artifactVolumeDao.save(alreadyExistingArtifactVolume);
					}else {
							artifactVolumeStatus = ArtifactVolumeStatus.previous;
					}
				}
			}
		return artifactVolumeStatus;
	}
	
	private void moveFileNLogToOutputFolder(Path destDir, File nthXmlFile, String volumeName, ImportResponse importResponse) throws IOException {
		destDir = Paths.get(destDir.toString(), volumeName); // create a directory and put the source xml file and its logs...

		// write the response to a log file inside the destDir
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(importResponse);
		logger.trace("Import log - " + json);
		FileUtils.write(Paths.get(destDir.toString(), volumeName + ".log."+importResponse.getUserRequestId()).toFile(), json);
		
		// move the catalog file to the destDir
		File destFile = Paths.get(destDir.toString(), volumeName + ".xml."+importResponse.getUserRequestId()).toFile();
		if(destFile.exists())
			destFile.delete();
		FileUtils.moveFile(nthXmlFile, destFile);
	}
	
	public class FileMeta{
		private int fileCount;
		private long totalSize;
		public int getFileCount() {
			return fileCount;
		}
		public void setFileCount(int fileCount) {
			this.fileCount = fileCount;
		}
		public long getTotalSize() {
			return totalSize;
		}
		public void setTotalSize(long totalSize) {
			this.totalSize = totalSize;
		}
	}

	public ImportResponse markedCompletedImport(int importId, String reason) {
		ImportResponse importResponse = new ImportResponse();	
		Request userRequest = null;
		Import importTable = null;
		try {		
			importTable = importDao.findById(importId).get();
			if(importTable.getStatus() != Status.failed)
				throw new DwaraException("Import cannot be marked completed. Only failed Import records can be marked_completed"); //

			HashMap<String, Object> data = new HashMap<String, Object>();
	    	data.put("importId", importId);
			
			userRequest = createUserRequest(Action.marked_completed, data);
			
			Import importDBRecord = new Import();				
			importDBRecord.setVolumeId(importTable.getVolumeId());
			importDBRecord.setArtifactName(importTable.getArtifactName());
			importDBRecord.setRequeueId(importTable.getRequeueId() + 1);
			importDBRecord.setArtifactId(importTable.getArtifactId());
			importDBRecord.setMessage(reason);
			importDBRecord.setStatus(Status.marked_completed);
			importDBRecord.setRequest(userRequest);
			
			importDBRecord = importDao.save(importDBRecord);
			logger.info("Import marked as completed successfully " + importDBRecord);
			
			userRequest.setStatus(Status.completed);
			userRequest = requestDao.save(userRequest);
			
//			Request origUserRequest = importTable.getRequest();
//			List<Import> importVolumeList = importDao.findAllByVolumeId(importTable.getVolumeId());
//			 
//			Map<String, Status> artifactName_Status = new HashMap<String, Status>();
//			Map<String, Integer> artifactName_RequeueId = new HashMap<String, Integer>();
//			for (Import nthImport : importVolumeList) {
//				Integer existingRequeueId = artifactName_RequeueId.get(nthImport.getArtifactName());
//				if(existingRequeueId == null || nthImport.getRequeueId() > existingRequeueId) {
//					artifactName_RequeueId.put(nthImport.getArtifactName(), nthImport.getRequeueId());
//					artifactName_Status.put(nthImport.getArtifactName(), nthImport.getStatus());
//				}
//			}
//			
//			Set<String> keySet = artifactName_Status.keySet();
//			List<Status> importStatusList = new ArrayList<Status>();
//			for (String nthArtifactName : keySet) {
//				importStatusList.add(artifactName_Status.get(nthArtifactName));
//			}
//			
//			origUserRequest.setStatus(StatusUtil.getStatus(importStatusList));
			
			
			importResponse.setUserRequestId(userRequest.getId());
			importResponse.setAction(Action.marked_completed.name());
			importResponse.setRequestedBy(userRequest.getRequestedBy().getName());
			importResponse.setRequestedAt(userRequest.getRequestedAt().toString());
			importResponse.setRunCount(importDBRecord.getRequeueId());
			importResponse.setVolumeId(importTable.getVolumeId());
			
			
			
		} catch (Exception e) {
			if(userRequest != null && userRequest.getId() != 0) {
				userRequest.setStatus(Status.failed);
				userRequest = requestDao.save(userRequest);
				importResponse.setUserRequestId(userRequest.getId());
			}
			throw e;
		}
		return importResponse;
	}

	public String setSequence(SetSequenceImportRequest importRequest) throws Exception {
		File xmlFile = FileUtils.getFile(importRequest.getXmlPathname());
		int counter = importRequest.getStartingNumber();
		Volumeindex volumeindex = getVolumeindex(xmlFile);
		List<Artifact> artifactList = volumeindex.getArtifact();
		for (Artifact artifact : artifactList) {
			artifact.setRename(counter + "_" + artifact.getName());
			counter++;
		}
		
	    XmlMapper xmlMapper = new XmlMapper();
		//Get XMLOutputFactory instance.
		XMLOutputFactory xmlOutputFactory = xmlMapper.getFactory().getXMLOutputFactory();
	    String propName = com.ctc.wstx.api.WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL;
	    xmlOutputFactory.setProperty(propName, true);
	    xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
	    xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);

		//Create FileWriter object.
		Writer fileWriter = new FileWriter(xmlFile.getAbsolutePath()+"_new");
		//Create XMLStreamWriter object from xmlOutputFactory.
		XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(fileWriter);

	    xmlMapper.writeValue(xmlStreamWriter, volumeindex);

		return "Done";
	}
}
