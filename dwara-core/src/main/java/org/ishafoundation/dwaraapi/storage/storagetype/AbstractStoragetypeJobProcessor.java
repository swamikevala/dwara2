package org.ishafoundation.dwaraapi.storage.storagetype;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.DomainAttributeConverter;
import org.ishafoundation.dwaraapi.db.dao.master.DestinationDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobMapDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepositoryUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Destination;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.JobMap;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.ArtifactVolumeDetails;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.Storagelevel;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchivedFile;
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagelevel.IStoragelevel;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.VolumeFinalizer;
import org.ishafoundation.dwaraapi.utils.VolumeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractStoragetypeJobProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractStoragetypeJobProcessor.class);
	
	@Autowired
	private JobDao jobDao; 
	
	@Autowired
	private JobMapDao jobMapDao; 

	@Autowired
	private VolumeDao volumeDao;
	
	@Autowired
	private DestinationDao destinationDao;
	
	@Autowired
	private Map<String, IStoragelevel> storagelevelMap;
		
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@Autowired
	private FileRepositoryUtil fileRepositoryUtil;
	
	@Autowired
	private VolumeUtil volumeUtil;
	
	@Autowired
	private VolumeFinalizer volumeFinalizer;
	
	@Autowired
	private DomainAttributeConverter domainAttributeConverter;

	@Autowired
	private Configuration configuration;

	public AbstractStoragetypeJobProcessor() {
		logger.debug(this.getClass().getName());
	}
	
    protected void beforeInitialize(SelectedStorageJob selectedStorageJob) throws Exception {
    	
    	
    }
    
	public StorageResponse initialize(SelectedStorageJob selectedStorageJob) throws Throwable{
		StorageResponse storageResponse = null;
    	beforeInitialize(selectedStorageJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(selectedStorageJob);
    	storageResponse = iStoragelevel.initialize(selectedStorageJob);
    	
    	afterInitialize(selectedStorageJob);
    	return storageResponse; 
   	
    }
	
	protected void afterInitialize(SelectedStorageJob selectedStorageJob) {
		
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		
		Volume volume = storageJob.getVolume();
		volume = volumeDao.save(volume);
		logger.trace("Volume " + volume.getId() + " attached to dwara succesfully");
		
		Job job = storageJob.getJob();
		job.setVolume(volume);
		jobDao.save(job);
		logger.trace("Job " + job.getId() + " updated with the formatted Volume " + volume.getId() + " succesfully");
	}

	
    protected void beforeWrite(SelectedStorageJob selectedStorageJob) throws Exception {}
    
    public StorageResponse write(SelectedStorageJob selectedStorageJob) throws Throwable{
    	logger.info("Writing job " + selectedStorageJob.getStorageJob().getJob().getId());
    	StorageResponse storageResponse = null;
    	beforeWrite(selectedStorageJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(selectedStorageJob);
    	storageResponse = iStoragelevel.write(selectedStorageJob);

    	afterWrite(selectedStorageJob, storageResponse);
    	return storageResponse; 
    }
    
    protected void afterWrite(SelectedStorageJob selectedStorageJob, StorageResponse storageResponse) throws Exception {
		List<ArchivedFile> archivedFileList = null;

    	StorageJob storagejob = selectedStorageJob.getStorageJob();
    	
		Artifact artifact = storagejob.getArtifact();
		int artifactId = artifact.getId();
		
		Volume volume = storagejob.getVolume();
		
		Domain domain = storagejob.getDomain();
		
		// Get a map of Paths and their File object
		HashMap<String, ArchivedFile> filePathNameToArchivedFileObj = new LinkedHashMap<String, ArchivedFile>();
		if(volume.getStoragelevel() == Storagelevel.block) { //could use if(storageResponse != null && storageResponse.getArchiveResponse() != null) { but archive and block are NOT mutually exclusive
			archivedFileList = storageResponse.getArchiveResponse().getArchivedFileList();
			for (Iterator<ArchivedFile> iterator = archivedFileList.iterator(); iterator.hasNext();) {
				ArchivedFile archivedFile = (ArchivedFile) iterator.next();
				String filePathName = archivedFile.getFilePathName();
				filePathNameToArchivedFileObj.put(filePathName, archivedFile);
			}
		}
		
//    	FileRepository<File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
//    	Method fileDaoFindAllBy = domainSpecificFileRepository.getClass().getMethod(FileRepository.FIND_ALL_BY_ARTIFACT_ID.replace("<<DOMAIN_SPECIFIC_ARTIFACT>>", artifact.getClass().getSimpleName()), int.class);
////    	Method fileDaoFindAllBy = domainSpecificFileRepository.getClass().getMethod("findAllBy" + artifact.getClass().getSimpleName() + "Id", int.class);
//		List<File> artifactFileList = (List<File>) fileDaoFindAllBy.invoke(domainSpecificFileRepository, artifactId);
		List<File> artifactFileList = fileRepositoryUtil.getArtifactFileList(artifact, domain);

		// NOTE: We need filevolume entries even when response from storage layer is null(Only archiveformats return the file breakup storage details... Other non archive writes dont...)
		// So we need to iterate on the files than on the archived file response...
		// OBSERVATION: The written file order on volume and the listed file varies...
		Integer artifactStartVolumeBlock = null;
		List<FileVolume> toBeAddedFileVolumeTableEntries = new ArrayList<FileVolume>();
		for (Iterator<File> iterator = artifactFileList.iterator(); iterator.hasNext();) {
			File nthFile = iterator.next();
			String filePathname = FilenameUtils.separatorsToUnix(nthFile.getPathname());
//			if(nthFile.getChecksum() != null && StringUtils.isNotBlank(FilenameUtils.getExtension(filePathname))) { //if file is not folder
//				String readyToIngestPath =  "C:\\data\\ingested"; // TODO Hardcoded
//				java.io.File file = new java.io.File(readyToIngestPath + java.io.File.separator + nthFile.getPathname());
//				nthFile.setChecksum(Md5Util.getChecksum(file, volume.getChecksumtype()));
//			}
			
			FileVolume fileVolume = domainUtil.getDomainSpecificFileVolumeInstance(nthFile.getId(), volume, domain);// lets just let users use the util consistently
			
			// TODO
			//fileVolume.setVerifiedAt(verifiedAt);
			//fileVolume.setEncrypted(encrypted);

			ArchivedFile archivedFile = filePathNameToArchivedFileObj.get(filePathname);
			if(archivedFile != null) { // if(volume.getStoragelevel() == Storagelevel.block) { - need to check if the file is archived anyway even if its block, so going with the archivedFile check alone
				Integer volumeBlock = archivedFile.getVolumeBlock();
				if(volumeBlock == null) {
					// need to look the previous job's last file end block and append it with current job's - volumeBlock = 
				}
				if(filePathname.equals(artifact.getName())) {
					artifactStartVolumeBlock = volumeBlock;
				}
				fileVolume.setVolumeBlock(volumeBlock);
				fileVolume.setArchiveBlock(archivedFile.getArchiveBlock());
			}
			toBeAddedFileVolumeTableEntries.add(fileVolume);
		}
		
	    if(toBeAddedFileVolumeTableEntries.size() > 0) {
	    	FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
	    	domainSpecificFileVolumeRepository.saveAll(toBeAddedFileVolumeTableEntries);
	    	logger.info("FileVolume records created successfully");
	    }

	    ArtifactVolume artifactVolume = domainUtil.getDomainSpecificArtifactVolumeInstance(artifact.getId(), volume, domain); // lets just let users use the util consistently
	    artifactVolume.setName(artifact.getName());
	    artifactVolume.setJob(storagejob.getJob());
	    if(volume.getStoragelevel() == Storagelevel.block) {
		    ArtifactVolumeDetails artifactVolumeDetails = new ArtifactVolumeDetails();
		    
		    ArchiveResponse archiveResponse = storageResponse.getArchiveResponse();
			String archiveId = archiveResponse.getArchiveId();// For tar it will not be available...;
			artifactStartVolumeBlock = archiveResponse.getArtifactStartVolumeBlock();
		    Integer artifactEndVolumeBlock = archiveResponse.getArtifactEndVolumeBlock();
		    artifactVolumeDetails.setArchive_id(archiveId);
		    artifactVolumeDetails.setStart_volume_block(artifactStartVolumeBlock);
		    artifactVolumeDetails.setEnd_volume_block(artifactEndVolumeBlock);
		    
		    artifactVolume.setDetails(artifactVolumeDetails);
	    }
	    ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
	    artifactVolume = domainSpecificArtifactVolumeRepository.save(artifactVolume);
	    
    	logger.info("ArtifactVolume - " + artifactVolume.getId().getArtifactId() + " " + artifactVolume.getName() + " " + artifactVolume.getId().getVolumeId() + " " + artifactVolume.getDetails().getStart_volume_block() + " " + artifactVolume.getDetails().getEnd_volume_block());
    	int lastArtifactOnVolumeEndVolumeBlock = artifactVolume.getDetails().getEnd_volume_block();
    	logger.trace("lastArtifactOnVolumeEndVolumeBlock " + lastArtifactOnVolumeEndVolumeBlock);
    	logger.trace("volume.getDetails().getBlocksize() - " + volume.getDetails().getBlocksize());
    	long usedCapacity = (long) volume.getDetails().getBlocksize() * lastArtifactOnVolumeEndVolumeBlock;
    	logger.trace("usedCapacity - " + usedCapacity);
    	boolean isVolumeNeedToBeFinalized = volumeUtil.isVolumeNeedToBeFinalized(domain, volume, usedCapacity);
    	if(isVolumeNeedToBeFinalized) {
    		logger.info("Triggering a finalization request for volume - " + volume.getId());
    		
			// TODO Need to set the system as the user - How?
    		volumeFinalizer.finalize(volume.getId(), null, domain);
    	}
    }
    
    // TODO Should we force this to be implemented or let it be overwritten
    protected void beforeVerify(SelectedStorageJob selectedStorageJob) throws Exception {
    	StorageJob storageJob = selectedStorageJob.getStorageJob();
		
		Job job = storageJob.getJob();
		Volume volume = storageJob.getVolume();
    	
		Job writeJobToBeVerified = null;
		List<JobMap> preReqJobRefs = jobMapDao.findAllByIdJobId(job.getId());// getting all prerequisite jobss
		for (JobMap nthPreReqJobRef : preReqJobRefs) {
			// means a dependent job.
			Job preReqJobRef  = jobDao.findById(nthPreReqJobRef.getId().getJobRefId()).get();
			if(preReqJobRef != null && preReqJobRef.getStoragetaskActionId() == Action.write) {
				writeJobToBeVerified = preReqJobRef;
			}
		}		
		
		String artifactclassId = job.getRequest().getDetails().getArtifactclassId();
		Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(artifactclassId);
		Domain domain = artifactclass.getDomain();
		storageJob.setDomain(domain);
		
		Integer inputArtifactId = writeJobToBeVerified.getInputArtifactId();
		Artifact artifact = domainUtil.getDomainSpecificArtifact(domain, inputArtifactId);		
		
		storageJob.setArtifact(artifact);

		
		ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
		ArtifactVolume artifactVolume = domainUtil.getDomainSpecificArtifactVolume(domain, artifact.getId(), volume.getId());
		
		selectedStorageJob.setArtifactStartVolumeBlock(artifactVolume.getDetails().getStart_volume_block());
		selectedStorageJob.setArtifactEndVolumeBlock(artifactVolume.getDetails().getEnd_volume_block());
		
		// to where
		String targetLocationPath = configuration.getRestoreTmpLocationForVerification();
		storageJob.setTargetLocationPath(targetLocationPath);
		
		List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileList = fileRepositoryUtil.getArtifactFileList(artifact, domain);
		selectedStorageJob.setArtifactFileList(fileList);
		selectedStorageJob.setFilePathNameToChecksum(getSourceFilesChecksum(fileList));

		for (File nthFile : fileList) {
			if(nthFile.getPathname().equals(artifact.getName())) {
				selectedStorageJob.setFile(nthFile);
				break;
			}
		}
    }
    
	public StorageResponse verify(SelectedStorageJob selectedStorageJob) throws Throwable{
		logger.info("Verifying job " + selectedStorageJob.getStorageJob().getJob().getId());
		StorageResponse storageResponse = null;
    	beforeVerify(selectedStorageJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(selectedStorageJob);
    	storageResponse = iStoragelevel.verify(selectedStorageJob);

    	afterVerify(selectedStorageJob);
    	return storageResponse; 
   	
    }
	
	protected void afterVerify(SelectedStorageJob selectedStorageJob) {
		// update the verified date here...
		updateFileVolumeVerifiedDate(selectedStorageJob);
	}

	private void updateFileVolumeVerifiedDate(SelectedStorageJob selectedStorageJob) {
    	StorageJob storageJob = selectedStorageJob.getStorageJob();
		
		Volume volume = storageJob.getVolume();
		Domain domain = storageJob.getDomain();
		List<FileVolume> toBeAddedFileVolumeTableEntries = new ArrayList<FileVolume>();
		
		org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileToBeRestored = selectedStorageJob.getFile();
		List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileList = selectedStorageJob.getArtifactFileList();
		for (File nthFile : fileList) {
			if(nthFile.getPathname().startsWith(fileToBeRestored.getPathname())) {
				FileVolume fileVolume = domainUtil.getDomainSpecificFileVolume(domain, nthFile.getId(), volume.getId());
				
				fileVolume.setVerifiedAt(LocalDateTime.now());
				toBeAddedFileVolumeTableEntries.add(fileVolume);
			}
		}
	    if(toBeAddedFileVolumeTableEntries.size() > 0) {
	    	FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
	    	domainSpecificFileVolumeRepository.saveAll(toBeAddedFileVolumeTableEntries);
	    	logger.info("FileVolume records updated with verifiedat successfully");
	    }
	}
	
    protected void beforeFinalize(SelectedStorageJob selectedStorageJob) throws Exception {}
    
	public StorageResponse finalize(SelectedStorageJob selectedStorageJob) throws Throwable{
		StorageResponse storageResponse = null;
    	beforeFinalize(selectedStorageJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(selectedStorageJob);
    	storageResponse = iStoragelevel.finalize(selectedStorageJob);

    	afterFinalize(selectedStorageJob);
    	return storageResponse; 
    }
	
	protected void afterFinalize(SelectedStorageJob selectedStorageJob) {
		Volume volume = selectedStorageJob.getStorageJob().getVolume();
		volume.setFinalized(true);
		volumeDao.save(volume);
		logger.trace("Volume " + volume.getId() + " finalized succesfully");
	}


    protected void beforeRestore(SelectedStorageJob selectedStorageJob) throws Exception {
    	StorageJob storageJob = selectedStorageJob.getStorageJob();
    	storageJob.setTargetLocationPath(storageJob.getTargetLocationPath() + java.io.File.separator + configuration.getRestoreInProgressFileIdentifier());
    	Domain domain = storageJob.getDomain();
    	int fileIdToBeRestored = storageJob.getFileId();
		
		FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
		org.ishafoundation.dwaraapi.db.model.transactional.domain.File file = domainSpecificFileRepository.findById(fileIdToBeRestored).get();
		selectedStorageJob.setFile(file);
		
		// TODO : Not sure if we need to pass the destination id or path -- Destination destination = configurationTablesUtil.getDestination(storageJob.getDestination());
		Destination destination = destinationDao.findByPath(storageJob.getDestinationPath());
		selectedStorageJob.setUseBuffering(destination.isUseBuffering());
		
    	Method getArtifact = file.getClass().getMethod("getArtifact"+domainAttributeConverter.convertToDatabaseColumn(domain));
    	Artifact artifact = (Artifact) getArtifact.invoke(file);
		storageJob.setArtifact(artifact);
		
		List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileList = fileRepositoryUtil.getArtifactFileList(artifact, domain);
		selectedStorageJob.setArtifactFileList(fileList);
		selectedStorageJob.setFilePathNameToChecksum(getSourceFilesChecksum(fileList));
		
//		org.ishafoundation.dwaraapi.db.model.transactional.domain.File file = null;
//		for (File nthFile : fileList) {
//			if(nthFile.getId() == fileIdToBeRestored) {
//				file = nthFile;
//				break;
//			}
//		}
//		
//		selectedStorageJob.setFile(file);
//    	Method getArtifact = file.getClass().getMethod("getArtifact"+domainAttributeConverter.convertToDatabaseColumn(domain));
//    	Artifact artifact = (Artifact) getArtifact.invoke(file);
//		storageJob.setArtifact(artifact);
    }
    
	private HashMap<String, byte[]> getSourceFilesChecksum(List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileList){
		// caching the source file' checksum...
		HashMap<String, byte[]> filePathNameToChecksumObj = new LinkedHashMap<String, byte[]>();
		for (org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile : fileList) {
			String filePathName = nthFile.getPathname();
			byte[] checksum = nthFile.getChecksum();
			filePathNameToChecksumObj.put(filePathName, checksum);
		}
		return filePathNameToChecksumObj;
	}
    
	public StorageResponse restore(SelectedStorageJob selectedStorageJob) throws Throwable{
		logger.info("Restoring job " + selectedStorageJob.getStorageJob().getJob().getId());
		StorageResponse storageResponse = null;
    	beforeRestore(selectedStorageJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(selectedStorageJob);
    	storageResponse = iStoragelevel.restore(selectedStorageJob);

    	afterRestore(selectedStorageJob);
    	return storageResponse; 
    }
	
	protected void afterRestore(SelectedStorageJob selectedStorageJob) throws IOException {
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		if(storageJob.isRestoreVerify())
			updateFileVolumeVerifiedDate(selectedStorageJob); // update the verified date here...
		
		// upon completion moving the file to the original requested dest path
		String srcPath = storageJob.getTargetLocationPath() + java.io.File.separator + storageJob.getArtifact().getName();
		String destPath = srcPath.replace(java.io.File.separator + configuration.getRestoreInProgressFileIdentifier(), "");
		
		logger.trace("src " + srcPath);
		logger.trace("dest " + destPath);
		FileUtils.moveDirectory(new java.io.File(srcPath), new java.io.File(destPath));
	}
	
	private IStoragelevel getStoragelevelImpl(SelectedStorageJob selectedStorageJob){
		Storagelevel storagelevel = selectedStorageJob.getStorageJob().getVolume().getStoragelevel();
		return storagelevelMap.get(storagelevel.name()+DwaraConstants.STORAGELEVEL_SUFFIX);//+"Storagelevel");
	}
	
//	protected abstract void afterRestore(StorageTypeJob selectedStorageJob);
//
//	protected abstract void beforeRestore(StorageTypeJob selectedStorageJob);

}
