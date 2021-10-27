package org.ishafoundation.dwaraapi.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLOutputFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.req._import.ImportRequest;
import org.ishafoundation.dwaraapi.api.resp._import.ImportResponse;
import org.ishafoundation.dwaraapi.api.resp._import.ImportStatus;
import org.ishafoundation.dwaraapi.db.dao.master.SequenceDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ImportDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileEntityUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.ImportVolumeArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
import org.ishafoundation.dwaraapi.db.keys.ImportVolumeArtifactKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional._import.Import;
import org.ishafoundation.dwaraapi.db.model.transactional._import.ImportKey;
import org.ishafoundation.dwaraapi.db.model.transactional._import.jointables.ImportVolumeArtifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.ArtifactVolumeDetails;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.db.utils.SequenceUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.ArtifactVolumeStatus;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
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
	protected ImportDao importDao;
	
	@Autowired
	protected ImportVolumeArtifactDao importVolumeArtifactDao;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@Autowired
	private Map<String, AbstractStoragesubtype> storagesubtypeMap;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	protected SequenceUtil sequenceUtil;

	@Autowired
	private FileEntityUtil fileEntityUtil;
	
	private String bruLinkSeparator = Character.toString(Character.MIN_VALUE);
	
	private Map<String, Artifactclass> id_artifactclassMap = new HashMap<String, Artifactclass>();
	private List<Error> errorList = new ArrayList<Error>();
	private List<org.ishafoundation.dwaraapi.api.resp._import.Artifact> artifacts = new ArrayList<org.ishafoundation.dwaraapi.api.resp._import.Artifact>();
	
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
			throw new DwaraException(xmlFile.getAbsolutePath() + " not a valid xml");
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
	
	/*
	 * TODO - 
	 * response with all info for auditing...
	 * exception handling
	 * logging
	 * rerun
	 */
	public ImportResponse importCatalog(ImportRequest importRequest) throws Exception{	
		ImportResponse ir = new ImportResponse();
		Request request = null;
		try {
			String xmlPathname = importRequest.getXmlPathname();
			File xmlFile = FileUtils.getFile(xmlPathname);
	
			Volumeindex volumeindex = validateAndGetVolumeindex(xmlFile);	
			Volumeinfo volumeInfo = volumeindex.getVolumeinfo();
			String volumeId = volumeInfo.getVolumeuid();
			
			// TODO - move this to validateAndGetVolumeindex method
			List<Import> importList = importDao.findAllByIdVolumeId(volumeId);
			for (Import import1 : importList) {
				if(import1.getStatus() == Status.completed)
					throw new Exception(volumeId + " already imported successfully");
			}
			
			request = new Request();
			request.setType(RequestType.user);
			request.setActionId(Action.import_);
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
	
			// updating the xml payload in DB
			int runId = importList.size() + 1;
			ImportKey importKey = new ImportKey(volumeId, runId);
			
			Import _import = new Import();
			_import.setId(importKey);
			_import.setPayload(FileUtils.readFileToByteArray(xmlFile));
			_import.setRequest(request);
			_import = importDao.save(_import);
			
		    // update ArtifactVolume table with all entries from xml...
			List<Artifact> artifactList = volumeindex.getArtifact();
			for (Artifact artifact : artifactList) {
				ImportVolumeArtifactKey ivaKey = new ImportVolumeArtifactKey(volumeId, artifact.getName());
				Optional<ImportVolumeArtifact> ivaOptional = importVolumeArtifactDao.findById(ivaKey);
				ImportVolumeArtifact iva = null;
				if(ivaOptional.isPresent()) // for rerun this would be already available
					iva = ivaOptional.get();
				else {
					iva = new ImportVolumeArtifact();				
					iva.setId(ivaKey);
					iva.setStatus(Status.in_progress);
					importVolumeArtifactDao.save(iva);
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
			
			Domain domain = Domain.ONE;
			ArtifactRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact> domainSpecificArtifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);
		    ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
		    FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
	    	FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
	    	
//			List<Artifact> artifactList = volumeindex.getArtifact();
			for (Artifact artifact : artifactList) {
				String artifactName = artifact.getName();
				logger.debug("Now importing " + artifactName);
				
				ImportStatus artifactImportStatus = ImportStatus.failed; 
				ImportStatus artifactVolumeImportStatus = ImportStatus.failed;
				ImportStatus fileImportStatus = ImportStatus.failed;
				ImportStatus fileVolumeImportStatus = ImportStatus.failed;
				
				org.ishafoundation.dwaraapi.api.resp._import.Artifact respArtifact = new org.ishafoundation.dwaraapi.api.resp._import.Artifact();
				respArtifact.setName(artifact.getName());

				ImportVolumeArtifactKey ivaKey = new ImportVolumeArtifactKey(volumeId, artifact.getName());
				ImportVolumeArtifact iva = importVolumeArtifactDao.findById(ivaKey).get();
				if(iva.getStatus() == Status.completed) { // already completed - so skip it
					// continue;
					artifactImportStatus = ImportStatus.skipped; 
					artifactVolumeImportStatus = ImportStatus.skipped;
					fileImportStatus = ImportStatus.skipped;
					fileVolumeImportStatus = ImportStatus.skipped;
				}
				else {
					try {
						
						Artifactclass artifactclass = id_artifactclassMap.get(artifact.getArtifactclassuid());
						Sequence sequence = artifactclass.getSequence();
						String extractedCode = sequenceUtil.getExtractedCode(sequence, artifactName);
						Boolean isForceMatch = sequence.getForceMatch();
						if(Boolean.TRUE.equals(isForceMatch) && extractedCode == null) {
							throw new Exception("Missing expected PreviousSeqCode " + artifactName);
						}
						String sequenceCode = null;
						String prevSeqCode = null;
						String toBeArtifactName = null;
						if(sequence.isKeepCode() && extractedCode != null) {
							// retaining the same name
							toBeArtifactName = artifactName;
							sequenceCode = extractedCode;
						}
						else {
							prevSeqCode = extractedCode;
						}
						
						boolean artifactAlreadyExists = true;
						org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact artifact1 = null;
						if(prevSeqCode != null) {
							artifact1 = domainSpecificArtifactRepository.findByPrevSequenceCodeAndDeletedIsFalse(prevSeqCode);
						}else if(sequenceCode != null){				
							artifact1 = domainSpecificArtifactRepository.findBySequenceCodeAndDeletedIsFalse(sequenceCode);
						}
		
						//TODO - should we double check with size too??? domainSpecificArtifactRepository.findAllByTotalSizeAndDeletedIsFalse(size);
						if(artifact1 == null) {
							artifactAlreadyExists = false;
							if(sequenceCode == null) {
								String overrideSequenceRefId = null;
								if(artifactclass.getId().startsWith("video") && !artifactclass.getId().startsWith("video-digi")) {
				    				Sequence importSequenceGrp = sequenceDao.findById("video-imported-grp").get();
									if(importSequenceGrp.getCurrrentNumber() <= 27000) 
										overrideSequenceRefId = "video-imported-grp";
								}
								sequenceCode = sequenceUtil.getSequenceCode(sequence, artifactName, overrideSequenceRefId);	
								
								if(extractedCode != null && sequence.isReplaceCode())
									toBeArtifactName = artifactName.replace(extractedCode, sequenceCode);
								else
									toBeArtifactName = sequenceCode + "_" + artifactName;
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
				
							
							artifact1 = domainUtil.getDomainSpecificArtifactInstance(domain);
			//				artifact1.setFileCount(fileCount);
							artifact1.setName(toBeArtifactName);
							artifact1.setPrevSequenceCode(prevSeqCode);
							artifact1.setSequenceCode(sequenceCode);
			//				artifact1.setTotalSize(size);
							artifact1.setArtifactclass(artifactclass);
							artifact1.setqLatestRequest(request);
							
							artifact1 = (org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact) domainSpecificArtifactRepository.save(artifact1);
							artifactImportStatus = ImportStatus.completed;
							logger.info("Artifact " + artifact1.getId() + " imported to dwara succesfully");
						}else {
							artifactImportStatus = ImportStatus.skipped;
							logger.info("Artifact " + artifact1.getId() + " already exists, so skipping updating DB");  // artifact nth copy / rerun scenario
						}
					
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
						
					    ArtifactVolume artifactVolume = domainSpecificArtifactVolumeRepository.findByIdArtifactIdAndIdVolumeId(artifact1.getId(), volume.getId());
					    
					    if(artifactVolume == null) {
					    	artifactVolume = domainUtil.getDomainSpecificArtifactVolumeInstance(artifact1.getId(), volume, domain);
					    
						    artifactVolume.setName(artifactName); // NOTE : Dont be tempted to change this to toBeArtifactName - whatever in volume needs to go here...
						    if(volume.getStoragelevel() == Storagelevel.block) {
							    ArtifactVolumeDetails artifactVolumeDetails = new ArtifactVolumeDetails();
							    
							    // artifactVolumeDetails.setArchiveId(archiveId);
							    artifactVolumeDetails.setStartVolumeBlock(artifact.getStartblock());
							    artifactVolumeDetails.setEndVolumeBlock(artifact.getEndblock());
							    
							    artifactVolume.setDetails(artifactVolumeDetails);
						    }
						    // updating this upon all updates are successful - artifactVolume.setStatus(ArtifactVolumeStatus.current);
						    artifactVolume = domainSpecificArtifactVolumeRepository.save(artifactVolume);
						    artifactVolumeImportStatus = ImportStatus.completed;
						    logger.info("ArtifactVolume record created successfully");
					    }else {
					    	artifactVolumeImportStatus = ImportStatus.skipped;
					    	logger.info("ArtifactVolume for " + artifact1.getId() + ":" + volume.getId() + " already exists, so skipping updating DB"); // rerun scenario
					    }
					    
					    long artifactTotalSize = 0;
					    int fileCount = 0;
					    List<org.ishafoundation.dwaraapi.storage.storagelevel.block.index.File> artifactFileList = artifact.getFile();
						for (org.ishafoundation.dwaraapi.storage.storagelevel.block.index.File nthFile : artifactFileList) {
							String filePathname = nthFile.getName().replace(artifactName, toBeArtifactName);
							String linkName = null;
							if(filePathname.contains(bruLinkSeparator)) {
								linkName = StringUtils.substringAfter(filePathname, bruLinkSeparator);
								filePathname = StringUtils.substringBefore(filePathname, bruLinkSeparator);
								
								logger.trace("filePathName "+ filePathname);
								logger.trace("linkName "+ linkName);
							}
							
							if(artifactclass.getConfig() != null) {
								String pathnameRegex = artifactclass.getConfig().getPathnameRegex();
								if(!filePathname.matches(pathnameRegex)) {
									logger.trace("Doesnt match " + pathnameRegex + " regex for " + filePathname);
									continue;
								}
							}
							
							byte[] filePathnameChecksum = ChecksumUtil.getChecksum(filePathname);
							org.ishafoundation.dwaraapi.db.model.transactional.domain.File file = null;
							if(!artifactAlreadyExists) { // if artifactAlreadyExists - file would also exist already - copy / rerun scenario
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
								file = domainUtil.getDomainSpecificFileInstance(domain);
				
				
								file.setPathname(filePathname);
								file.setPathnameChecksum(filePathnameChecksum);
								file.setSize(nthFile.getSize());
								//file.setSymlinkFileId();
								file.setSymlinkPath(linkName);
								fileEntityUtil.setDomainSpecificFileArtifact(file, artifact1);
								if(Boolean.TRUE.equals(nthFile.getDirectory())) {// if(StringUtils.isBlank(FilenameUtils.getExtension(filePathname))) {  // TODO - change it to - if(nthFile.isDirectory()) 
									file.setDirectory(true);
								}else {
									fileCount++;
									artifactTotalSize += nthFile.getSize(); 
								}	
								
								file = domainSpecificFileRepository.save(file);
								fileImportStatus = ImportStatus.completed;
						    	logger.debug("File " + filePathname + "  created successfully");
							}
							else {
								file = domainSpecificFileRepository.findByPathnameChecksum(filePathnameChecksum);
								fileImportStatus = ImportStatus.skipped;
								logger.debug("File " + filePathname + " already exists, so skipping updating DB");
							}
		
							FileVolume fileVolume = domainSpecificFileVolumeRepository.findByIdFileIdAndIdVolumeId(file.getId(), volume.getId());
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
								fileVolume = domainUtil.getDomainSpecificFileVolumeInstance(file.getId(), volume, domain);// lets just let users use the util consistently
								fileVolume.setArchiveBlock(nthFile.getArchiveblock());
								fileVolume.setVolumeBlock(nthFile.getVolumeStartBlock());
								//fileVolume.setEndVolumeBlock(nthFile.getVolumeEndBlock());
				
								//fileVolume.setHardlinkFileId(file.getId());
								
						    	domainSpecificFileVolumeRepository.save(fileVolume);
						    	fileVolumeImportStatus = ImportStatus.completed;
						    	logger.debug("FileVolume records created successfully");
						    }
							else {
								logger.debug("FileVolume for " + file.getId() + ":" + volume.getId() + " already exists, so skipping updating DB"); // rerun scenario
								fileVolumeImportStatus = ImportStatus.skipped;
								break; // This means this is a rerun scenario and so the rest of the files can be skipped....
							}
						}	
						logger.info("File and FileVolume dealt with");
						
						// UPDATING THE artifactvolumestatus only when everything is OK, so its used for restoring   
						if(artifactVolumeImportStatus == ImportStatus.completed) {
							ArtifactVolumeStatus artifactVolumeStatus = ArtifactVolumeStatus.current;
	
							// If already an entry for this pool/group is available (eg. 68*[C16805L6] is migration of 4*[C14023L4]) for this artifact - retire the oldest generation
							ArtifactVolume alreadyExistingArtifactVolume = domainSpecificArtifactVolumeRepository.findByIdArtifactIdAndVolumeGroupRefCopyIdAndStatus(artifact1.getId(), volume.getGroupRef().getCopy().getId(), ArtifactVolumeStatus.current);
							if(alreadyExistingArtifactVolume != null) {
								int alreadyExistingArtifactVolumeGen = Integer.parseInt(StringUtils.substringAfter(alreadyExistingArtifactVolume.getVolume().getStoragesubtype(), "-"));
								int currentVolumeGen =  Integer.parseInt(StringUtils.substringAfter(volume.getStoragesubtype(), "-"));
	
								// check out the latest generation and use the most latest
								if(currentVolumeGen > alreadyExistingArtifactVolumeGen) { // if current volume is the latest - delete the oldest generation
									artifactVolumeStatus = ArtifactVolumeStatus.current;
									// flagging the older generation as deleted
									alreadyExistingArtifactVolume.setStatus(ArtifactVolumeStatus.deleted);
									domainSpecificArtifactVolumeRepository.save(alreadyExistingArtifactVolume);
								}
								else
									artifactVolumeStatus = ArtifactVolumeStatus.deleted;
							}
							 
						    artifactVolume.setStatus(artifactVolumeStatus);
						    artifactVolume = domainSpecificArtifactVolumeRepository.save(artifactVolume);
						}
						
						// updating artifact.filecount and size
						if(!artifactAlreadyExists) {
							artifact1.setFileCount(fileCount);
							artifact1.setTotalSize(artifactTotalSize);
							artifact1 = (org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact) domainSpecificArtifactRepository.save(artifact1);
						}
	
						respArtifact.setId(artifact1.getId());
						respArtifact.setName(artifact1.getName());
						
	
						// TODO - should we add - artifact id, DB artifact name,  artifactImportStatus, artifactVolumeImportStatus, fileImportStatus, fileVolumeImportStatus (needs rerun id)
						iva.setStatus(Status.completed);
						importVolumeArtifactDao.save(iva);
					}catch (Exception e) {
						logger.error("Unable to import completely " + artifactName, e);
						iva.setStatus(Status.failed);
						importVolumeArtifactDao.save(iva);
					}
				}
				respArtifact.setArtifactStatus(artifactImportStatus);
				respArtifact.setArtifactVolumeStatus(artifactVolumeImportStatus);
				respArtifact.setFileStatus(fileImportStatus);
				respArtifact.setFileVolumeStatus(fileVolumeImportStatus);
				artifacts.add(respArtifact);
			}
			
			List<ImportVolumeArtifact> ivaAllList = importVolumeArtifactDao.findAllByIdVolumeId(volumeId);
			List<ImportVolumeArtifact> ivaNonCompletedList = importVolumeArtifactDao.findAllByIdVolumeIdAndStatusIsNot(volumeId, Status.completed);
			Status tapeImportStatus = Status.failed;
			if(ivaAllList.size() > 0 && ivaNonCompletedList.size() == 0)
				tapeImportStatus = Status.completed;
			_import.setStatus(tapeImportStatus);
			_import = importDao.save(_import);
			
			request.setStatus(Status.completed);
			requestDao.save(request);
		
			ir.setUserRequestId(request.getId());
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
			}
			
			ir.setErrors(errorList);
		}
		return ir;
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
		
		VolumeDetails volumeDetails = new VolumeDetails();
		volumeDetails.setBarcoded(true);
		volumeDetails.setBlocksize(volumeinfo.getVolumeblocksize());

		volume.setDetails(volumeDetails);
		return volume;
	}

}
