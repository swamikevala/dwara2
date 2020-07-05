package org.ishafoundation.dwaraapi.storage.storagetype;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificArtifactVolumeFactory;
import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificFileVolumeFactory;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.ArtifactVolumeDetails;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.Storagelevel;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchivedFile;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.ishafoundation.dwaraapi.storage.storagelevel.IStoragelevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractStoragetypeJobProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractStoragetypeJobProcessor.class);
	
	@Autowired
	private Map<String, IStoragelevel> storagelevelMap;
		
	@Autowired
	private DomainUtil domainUtil;

	public AbstractStoragetypeJobProcessor() {
		logger.debug(this.getClass().getName());
	}
	
    protected void beforeFormat(StoragetypeJob storagetypeJob) {}
    
	//public ArchiveResponse restore(StorageJob storagetypeJob) throws Throwable{
	public StorageResponse format(StoragetypeJob storagetypeJob) throws Throwable{
		StorageResponse storageResponse = null;
    	beforeFormat(storagetypeJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storagetypeJob);
    	storageResponse = iStoragelevel.format(storagetypeJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storagetypeJob);
//    	ar = storageFormatter.restore(storagetypeJob);
    	afterFormat(storagetypeJob);
    	return storageResponse; 
   	
    }
	
	protected void afterFormat(StoragetypeJob storagetypeJob) {}

	
    protected void beforeWrite(StoragetypeJob storagetypeJob) {}
    
    public StorageResponse write(StoragetypeJob storagetypeJob) throws Throwable{
    	logger.info("Writing job " + storagetypeJob.getStorageJob().getJob().getId());
    	StorageResponse storageResponse = null;
    	beforeWrite(storagetypeJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storagetypeJob);
    	storageResponse = iStoragelevel.write(storagetypeJob);

    	afterWrite(storagetypeJob, storageResponse);
    	return storageResponse; 
    }
    
    protected void afterWrite(StoragetypeJob storagetypeJob, StorageResponse storageResponse) throws Exception {
		List<ArchivedFile> archivedFileList = null;

    	StorageJob storagejob = storagetypeJob.getStorageJob();
    	
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
		
    	FileRepository<File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
    	Method fileDaoFindAllBy = domainSpecificFileRepository.getClass().getMethod(FileRepository.FIND_ALL_BY_ARTIFACT_ID.replace("<<DOMAIN_SPECIFIC_ARTIFACT>>", artifact.getClass().getSimpleName()), int.class);
//    	Method fileDaoFindAllBy = domainSpecificFileRepository.getClass().getMethod("findAllBy" + artifact.getClass().getSimpleName() + "Id", int.class);
		List<File> artifactFileList = (List<File>) fileDaoFindAllBy.invoke(domainSpecificFileRepository, artifactId);

		// NOTE: We need filevolume entries even when response from storage layer is null(Only archiveformats return the file breakup storage details... Other non archive writes dont...)
		// So we need to iterate on the files than on the archived file response...
		// OBSERVATION: The written file order on volume and the listed file varies...
		Integer artifactStartVolumeBlock = null;
		List<FileVolume> toBeAddedFileVolumeTableEntries = new ArrayList<FileVolume>();
		for (Iterator<File> iterator = artifactFileList.iterator(); iterator.hasNext();) {
			File nthFile = iterator.next();
			String filePathname = FilenameUtils.separatorsToUnix(nthFile.getPathname());
			
		    // To get an File*Volume instance dynamically, where * is domain name...
			//FileVolume fileVolume = DomainSpecificFileVolumeFactory.getInstance(domain, nthFile.getId(), volume);
			//OR
			FileVolume fileVolume = domainUtil.getDomainSpecificFileVolumeInstance(domain, nthFile.getId(), volume);// lets just let users use the util consistently
			
			// TODO
			//fileVolume.setVerifiedAt(verifiedAt);
			//fileVolume.setEncrypted(encrypted);

			ArchivedFile archivedFile = filePathNameToArchivedFileObj.get(filePathname);
			if(archivedFile != null) { // if(volume.getStoragelevel() == Storagelevel.block) { - need to check if the file is archived anyway even if its block, so going with the archivedFile check alone
				Integer volumeBlock = archivedFile.getVolumeBlockOffset();
				if(volumeBlock == null) {
					// need to look the previous job's last file end block and append it with current job's - volumeBlock = 
				}
				if(filePathname.equals(artifact.getName())) {
					artifactStartVolumeBlock = volumeBlock;
				}
				fileVolume.setVolumeBlock(volumeBlock);
				fileVolume.setArchiveBlock(archivedFile.getArchiveBlockOffset());
			}
			toBeAddedFileVolumeTableEntries.add(fileVolume);
		}
	
	    if(toBeAddedFileVolumeTableEntries.size() > 0) {
	    	FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
	    	domainSpecificFileVolumeRepository.saveAll(toBeAddedFileVolumeTableEntries);
	    	logger.info("FileVolume records created successfully");
	    }
	    

	    // To get an Artifact*Volume instance dynamically, where * is domain name...
	    //ArtifactVolume artifactVolume = DomainSpecificArtifactVolumeFactory.getInstance(domain, artifact.getId(), volume);
	    // OR
	    ArtifactVolume artifactVolume = domainUtil.getDomainSpecificArtifactVolumeInstance(domain, artifact.getId(), volume); // lets just let users use the util consistently
	    if(volume.getStoragelevel() == Storagelevel.block) {
		    ArtifactVolumeDetails artifactVolumeDetails = new ArtifactVolumeDetails();
		    
		    ArchiveResponse archiveResponse = storageResponse.getArchiveResponse();
			String archiveId = archiveResponse.getArchiveId();// For tar it will not be available...;
			artifactStartVolumeBlock = archiveResponse.getArtifactStartVolumeBlock();// TODO : do we need to overwrite here or let the individual impl set this?
		    Integer artifactTotalVolumeBlocks = archiveResponse.getArtifactTotalVolumeBlocks();
		    artifactVolumeDetails.setArchive_id(archiveId);
		    artifactVolumeDetails.setStart_volume_block(artifactStartVolumeBlock);
		    artifactVolumeDetails.setTotal_volume_blocks(artifactTotalVolumeBlocks);
		    
		    artifactVolume.setDetails(artifactVolumeDetails);
	    }
	    ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
	    artifactVolume = domainSpecificArtifactVolumeRepository.save(artifactVolume);
    	logger.info("ArtifactVolume - " + artifactVolume.getId());

    }
    
    // TODO Should we force this to be implemented or let it be overwritten
//    protected abstract void afterWrite(StorageTypeJob storagetypeJob);
//
//	protected abstract void beforeWrite(StorageTypeJob storagetypeJob);

    protected void beforeVerify(StoragetypeJob storagetypeJob) {}
    
	//public StorageResponse restore(StorageJob storagetypeJob) throws Throwable{
	public StorageResponse verify(StoragetypeJob storagetypeJob) throws Throwable{
		logger.info("Verifying job " + storagetypeJob.getStorageJob().getJob().getId());
		StorageResponse storageResponse = null;
    	beforeVerify(storagetypeJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storagetypeJob);
    	storageResponse = iStoragelevel.verify(storagetypeJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storagetypeJob);
//    	ar = storageFormatter.restore(storagetypeJob);
    	afterVerify(storagetypeJob);
    	return storageResponse; 
   	
    }
	
	protected void afterVerify(StoragetypeJob storagetypeJob) {}
    
    protected void beforeFinalize(StoragetypeJob storagetypeJob) {}
    
	//public StorageResponse restore(StorageJob storagetypeJob) throws Throwable{
	public StorageResponse finalize(StoragetypeJob storagetypeJob) throws Throwable{
		StorageResponse storageResponse = null;
    	beforeFinalize(storagetypeJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storagetypeJob);
    	storageResponse = iStoragelevel.finalize(storagetypeJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storagetypeJob);
//    	ar = storageFormatter.restore(storagetypeJob);
    	afterFinalize(storagetypeJob);
    	return storageResponse; 
   	
    }
	
	protected void afterFinalize(StoragetypeJob storagetypeJob) {}


    protected void beforeRestore(StoragetypeJob storagetypeJob) {}
    
	//public StorageResponse restore(StorageJob storagetypeJob) throws Throwable{
	public StorageResponse restore(StoragetypeJob storagetypeJob) throws Throwable{
		logger.info("Restoring job " + storagetypeJob.getStorageJob().getJob().getId());
		StorageResponse storageResponse = null;
    	beforeRestore(storagetypeJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storagetypeJob);
    	storageResponse = iStoragelevel.restore(storagetypeJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storagetypeJob);
//    	ar = storageFormatter.restore(storagetypeJob);
    	afterRestore(storagetypeJob);
    	return storageResponse; 
   	
    }
	
	protected void afterRestore(StoragetypeJob storagetypeJob) {}
	
	private IStoragelevel getStoragelevelImpl(StoragetypeJob storagetypeJob){
		Storagelevel storagelevel = storagetypeJob.getStorageJob().getVolume().getStoragelevel();
		return storagelevelMap.get(storagelevel.name()+DwaraConstants.STORAGELEVEL_SUFFIX);//+"Storagelevel");
	}
	
//	protected abstract void afterRestore(StorageTypeJob storagetypeJob);
//
//	protected abstract void beforeRestore(StorageTypeJob storagetypeJob);

}
