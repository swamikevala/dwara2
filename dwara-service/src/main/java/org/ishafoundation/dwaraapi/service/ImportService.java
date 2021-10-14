package org.ishafoundation.dwaraapi.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.stream.XMLOutputFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.req._import.ImportRequest;
import org.ishafoundation.dwaraapi.api.resp._import.ImportResponse;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ImportPayloadDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileEntityUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.ImportPayload;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.ArtifactVolumeDetails;
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
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Artifact;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Volumeindex;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Volumeinfo;
import org.ishafoundation.dwaraapi.storage.storagesubtype.AbstractStoragesubtype;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

@Component
public class ImportService extends DwaraService {
	
	private static final Logger logger = LoggerFactory.getLogger(ImportService.class);
	
	@Autowired
	private VolumeDao volumeDao;

	@Autowired
	protected ImportPayloadDao importPayloadDao;
	
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
		String xmlPathname = importRequest.getXmlPathname();
		File xmlFile = FileUtils.getFile(xmlPathname);

		Volumeindex volumeindex = validateAndGetVolumeindex(xmlFile);	
		
		Request request = new Request();
		request.setType(RequestType.user);
		request.setActionId(Action.import_);
		request.setStatus(Status.queued);
    	User user = getUserObjFromContext();
    	String requestedBy = user.getName();

    	LocalDateTime requestedAt = LocalDateTime.now();
		request.setRequestedAt(requestedAt);
		request.setRequestedBy(user);

		request = requestDao.save(request);
		logger.info(DwaraConstants.USER_REQUEST + request.getId());

		ImportPayload importPayload = new ImportPayload();
		importPayload.setPayload(FileUtils.readFileToByteArray(xmlFile));
		importPayload.setRequest(request);
		importPayloadDao.save(importPayload);
		
		Volumeinfo volumeInfo = volumeindex.getVolumeinfo();
		
		Volume volume = getVolume(volumeInfo);
		volume = volumeDao.save(volume);
		logger.trace("Volume " + volume.getId() + " imported to dwara succesfully");
		
		Domain domain = Domain.ONE;
		ArtifactRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact> domainSpecificArtifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);
	    ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
	    FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
    	FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
    	
		List<Artifact> artifactList = volumeindex.getArtifact();
		for (Artifact artifact : artifactList) {

			List<org.ishafoundation.dwaraapi.storage.storagelevel.block.index.File> artifactFileList = artifact.getFile();
			
			String artifactName = artifact.getName();
			List<Artifactclass> artifactclassList = configurationTablesUtil.getAllArtifactclasses();
			Map<String, Artifactclass> id_artifactclassMap = new HashMap<String, Artifactclass>();
			for (Artifactclass nthArtifactclass : artifactclassList) {
				id_artifactclassMap.put(nthArtifactclass.getId(), nthArtifactclass);
			}
			
			Artifactclass artifactclass = id_artifactclassMap.get(artifact.getArtifactclassuid());
			Sequence sequence = artifactclass.getSequence();
			int fileCount = artifactFileList.size(); // TODO - Should we exclude folders like in ingest or not???
			long size = 0;
			
			String extractedCode = sequenceUtil.getExtractedCode(sequence, artifactName);
			String sequenceCode = null;
			String prevSeqCode = null;
			String toBeArtifactName = null;
			// NOTE : forcematch is taken care of upfront during scan... No need to act on it...
			if(sequence.isKeepCode() && extractedCode != null) {
				// retaining the same name
				toBeArtifactName = artifactName;
				sequenceCode = extractedCode;
			}
			else {
				prevSeqCode = extractedCode;
				sequenceCode = sequenceUtil.getSequenceCode(sequence, artifactName);	
				if(extractedCode != null && sequence.isReplaceCode())
					toBeArtifactName = artifactName.replace(extractedCode, sequenceCode);
				else
					toBeArtifactName = sequenceCode + "_" + artifactName;
			}


			/*
			 * Creating artifact
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

			org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact artifact1 = domainUtil.getDomainSpecificArtifactInstance(domain);
			artifact1.setFileCount(fileCount);
			artifact1.setName(toBeArtifactName);
			artifact1.setPrevSequenceCode(prevSeqCode);
			artifact1.setSequenceCode(sequenceCode);
			artifact1.setTotalSize(size);
			artifact1.setArtifactclass(artifactclass);
			
			artifact1 = (org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact) domainSpecificArtifactRepository.save(artifact1);

		
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
			
		    ArtifactVolume artifactVolume = domainUtil.getDomainSpecificArtifactVolumeInstance(artifact1.getId(), volume, domain);
		    
		    artifactVolume.setName(artifactName); // NOTE : Dont be tempted to change this to toBeArtifactName - whatever in volume needs to go here...
		    if(volume.getStoragelevel() == Storagelevel.block) {
			    ArtifactVolumeDetails artifactVolumeDetails = new ArtifactVolumeDetails();
			    
			    // artifactVolumeDetails.setArchiveId(archiveId);
			    artifactVolumeDetails.setStartVolumeBlock(artifact.getStartblock());
			    artifactVolumeDetails.setEndVolumeBlock(artifact.getEndblock());
			    
			    artifactVolume.setDetails(artifactVolumeDetails);
		    }
		    artifactVolume.setStatus(ArtifactVolumeStatus.current);
		    artifactVolume = domainSpecificArtifactVolumeRepository.save(artifactVolume);

			for (org.ishafoundation.dwaraapi.storage.storagelevel.block.index.File nthFile : artifactFileList) {
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
				org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFileRowToBeInserted = domainUtil.getDomainSpecificFileInstance(domain);

				String filePathname = nthFile.getName().replace(artifactName, toBeArtifactName);
				byte[] filePathChecksum = ChecksumUtil.getChecksum(filePathname);

				nthFileRowToBeInserted.setPathname(filePathname);
				nthFileRowToBeInserted.setPathnameChecksum(filePathChecksum);
				nthFileRowToBeInserted.setSize(nthFile.getSize());
				fileEntityUtil.setDomainSpecificFileArtifact(nthFileRowToBeInserted, artifact1);
				if(StringUtils.isBlank(FilenameUtils.getExtension(filePathname)))  // TODO - change it to - if(nthFile.isDirectory()) 
					nthFileRowToBeInserted.setDirectory(true);

		    	org.ishafoundation.dwaraapi.db.model.transactional.domain.File file1 = domainSpecificFileRepository.save(nthFileRowToBeInserted);
		    	logger.info("File record created successfully");

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
				FileVolume fileVolume = domainUtil.getDomainSpecificFileVolumeInstance(file1.getId(), volume, domain);// lets just let users use the util consistently
				fileVolume.setArchiveBlock(nthFile.getArchiveblock());
				fileVolume.setVolumeBlock(nthFile.getVolumeblock());
				//fileVolume.setEndVolumeBlock(nthFile.getVolumeblock());

				//fileVolume.setHardlinkFileId(file.getId());

		    	domainSpecificFileVolumeRepository.save(fileVolume);
		    	logger.info("FileVolume records created successfully");
		    }
		}		

		request.setStatus(Status.completed);
		requestDao.save(request);
		
		// TODO 
		//request.setStatus(Status.failed);
		//requestDao.save(request);
		
		ImportResponse ir = new ImportResponse();
		ir.setUserRequestId(request.getId());
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
	 */
	private Volume getVolume(Volumeinfo volumeinfo) {
		Volume volume = new Volume();
		
		String volumeBarcode = volumeinfo.getVolumeuid();
		volume.setId(volumeBarcode);
		volume.setUuid(UUID.randomUUID().toString());
		volume.setType(Volumetype.physical);

/*
		String volumeGroupId = "???"; // need to know artifactclass and nth copy - R1 
		Volume volumeGroup = volumeDao.findById(volumeGroupId).get();
		volume.setGroupRef(volumeGroup);

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
