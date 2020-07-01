package org.ishafoundation.dwaraapi.storage.storagetype;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
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
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchivedFile;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.ishafoundation.dwaraapi.storage.storagelevel.IStoragelevel;
import org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractStoragetypeJobProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractStoragetypeJobProcessor.class);
	
	@Autowired
	private Map<String, IStoragelevel> storagelevelMap;
	
	@Autowired
	private Map<String, AbstractStoragetaskAction> storagetaskActionMap;
	
	@Autowired
	private DomainUtil domainUtil;

	public AbstractStoragetypeJobProcessor() {
		logger.debug(this.getClass().getName());
	}
	
    protected void beforeFormat(StoragetypeJob storagetypeJob) {}
    
	//public ArchiveResponse restore(StorageJob storagetypeJob) throws Throwable{
	public ArchiveResponse format(StoragetypeJob storagetypeJob) throws Throwable{
		ArchiveResponse archiveResponse = null;
    	beforeFormat(storagetypeJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storagetypeJob);
    	archiveResponse = iStoragelevel.format(storagetypeJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storagetypeJob);
//    	ar = storageFormatter.restore(storagetypeJob);
    	afterFormat(storagetypeJob);
    	return archiveResponse; 
   	
    }
	
	protected void afterFormat(StoragetypeJob storagetypeJob) {}

	
    protected void beforeWrite(StoragetypeJob storagetypeJob) {}
    
    public ArchiveResponse write(StoragetypeJob storagetypeJob) throws Throwable{
    	logger.info("Writing job " + storagetypeJob.getStorageJob().getJob().getId());
    	ArchiveResponse archiveResponse = null;
    	beforeWrite(storagetypeJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storagetypeJob);
    	archiveResponse = iStoragelevel.write(storagetypeJob);

    	afterWrite(storagetypeJob, archiveResponse);
    	return archiveResponse; 
    }
    
//    protected void afterWrite(StoragetypeJob storagetypeJob, ArchiveResponse ar) {}
    protected ArchiveResponse afterWrite(StoragetypeJob storagetypeJob, ArchiveResponse archiveResponse) throws Exception {
    	// file_volume and artifact_volume updated here...
    	
    	StorageJob storagejob = storagetypeJob.getStorageJob();
    	
		Artifact artifact = storagejob.getArtifact();
		int artifactId = artifact.getId();
		
		Volume volume = storagejob.getVolume();
		
		Domain domain = storagejob.getDomain();
		// Get a map of Paths and their File1 Ids
		
    	FileRepository<File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
    	Method fileDaoFindAllBy = domainSpecificFileRepository.getClass().getMethod(FileRepository.FIND_ALL_BY_ARTIFACT_ID.replace("<<DOMAIN_SPECIFIC_ARTIFACT>>", artifact.getClass().getSimpleName()), int.class);
//    	Method fileDaoFindAllBy = domainSpecificFileRepository.getClass().getMethod("findAllBy" + artifact.getClass().getSimpleName() + "Id", int.class);
    	
		List<File> artifactFileList = (List<File>) fileDaoFindAllBy.invoke(domainSpecificFileRepository, artifactId);
		
		HashMap<Integer, String> fileIdToPath = new HashMap<Integer, String>();
		HashMap<String, File> filePathNameTofileObj = new HashMap<String, File>();
		for (Iterator<File> iterator = artifactFileList.iterator(); iterator.hasNext();) {
			File nthFile = iterator.next();
			filePathNameTofileObj.put(FilenameUtils.separatorsToUnix(nthFile.getPathname()), nthFile);
			fileIdToPath.put(nthFile.getId(), nthFile.getPathname());
		}

		int artifactBlock = 0;
		//  some tape specific code like updating db file_tape and tapedrive...
		List<ArchivedFile> archivedFileList = archiveResponse.getArchivedFileList();
		List<FileVolume> toBeAddedFileVolumeTableEntries = new ArrayList<FileVolume>();
		for (Iterator<ArchivedFile> iterator = archivedFileList.iterator(); iterator.hasNext();) {
			ArchivedFile archivedFile = (ArchivedFile) iterator.next();
			String fileName = archivedFile.getFilePathName(); 
			
			// TODO get the file id from File1 table using this fileName...
			File file = null;
			if(filePathNameTofileObj != null)
				file = filePathNameTofileObj.get(fileName);

			//logger.trace(filePathNameTofileObj.toString());
			logger.trace(fileName);
			if(file == null) {
				logger.trace("Junk folder not supposed to be written to tape. Filter it. Skipping its info getting added to DB");
				continue;
			}
			
			if(fileName.equals(artifact.getName()))
				artifactBlock = archivedFile.getBlockNumber();
			
			
			FileVolume fileVolume = DomainSpecificFileVolumeFactory.getInstance(domain, file.getId(), volume);
			fileVolume.setArchiveBlock(archivedFile.getBlockNumber());
			// TODO
			//fileVolume.setVolumeBlock(volumeBlock);
			//fileVolume.setVerifiedAt(verifiedAt);
			//fileVolume.setEncrypted(encrypted);
			
			toBeAddedFileVolumeTableEntries.add(fileVolume);
		}
		
	    if(toBeAddedFileVolumeTableEntries.size() > 0) {
	    	logger.debug("DB FileVolume entries Creation");   
	    	FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
	    	domainSpecificFileVolumeRepository.saveAll(toBeAddedFileVolumeTableEntries);
	    	logger.debug("DB FileVolume entries Creation - Success");
	    }
	    
	    ArtifactVolume artifactVolume = DomainSpecificArtifactVolumeFactory.getInstance(domain, artifact.getId(), volume);
	    ArtifactVolumeDetails artifactVolumeDetails = new ArtifactVolumeDetails();
	    artifactVolumeDetails.setArchive_id("Some12345"); // TODO Fix this
	    logger.debug("DB ArtifactVolume Creation");
	    ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
    	domainSpecificArtifactVolumeRepository.save(artifactVolume);
	    logger.debug("DB ArtifactVolume Creation - Success");
	    
	    archiveResponse.setArtifactName(artifact.getName());
	    archiveResponse.setArtifactBlockNumber(artifactBlock);
	    return archiveResponse;
    }
    
    // TODO Should we force this to be implemented or let it be overwritten
//    protected abstract void afterWrite(StorageTypeJob storagetypeJob);
//
//	protected abstract void beforeWrite(StorageTypeJob storagetypeJob);

    protected void beforeVerify(StoragetypeJob storagetypeJob) {}
    
	//public ArchiveResponse restore(StorageJob storagetypeJob) throws Throwable{
	public ArchiveResponse verify(StoragetypeJob storagetypeJob) throws Throwable{
		logger.info("Verifying job " + storagetypeJob.getStorageJob().getJob().getId());
		ArchiveResponse ar = null;
    	beforeVerify(storagetypeJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storagetypeJob);
    	ar = iStoragelevel.verify(storagetypeJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storagetypeJob);
//    	ar = storageFormatter.restore(storagetypeJob);
    	afterVerify(storagetypeJob);
    	return ar; 
   	
    }
	
	protected void afterVerify(StoragetypeJob storagetypeJob) {}
    
    protected void beforeFinalize(StoragetypeJob storagetypeJob) {}
    
	//public ArchiveResponse restore(StorageJob storagetypeJob) throws Throwable{
	public ArchiveResponse finalize(StoragetypeJob storagetypeJob) throws Throwable{
		ArchiveResponse ar = null;
    	beforeFinalize(storagetypeJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storagetypeJob);
    	ar = iStoragelevel.finalize(storagetypeJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storagetypeJob);
//    	ar = storageFormatter.restore(storagetypeJob);
    	afterFinalize(storagetypeJob);
    	return ar; 
   	
    }
	
	protected void afterFinalize(StoragetypeJob storagetypeJob) {}


    protected void beforeRestore(StoragetypeJob storagetypeJob) {}
    
	//public ArchiveResponse restore(StorageJob storagetypeJob) throws Throwable{
	public ArchiveResponse restore(StoragetypeJob storagetypeJob) throws Throwable{
		logger.info("Restoring job " + storagetypeJob.getStorageJob().getJob().getId());
		ArchiveResponse archiveResponse = null;
    	beforeRestore(storagetypeJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storagetypeJob);
    	archiveResponse = iStoragelevel.restore(storagetypeJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storagetypeJob);
//    	ar = storageFormatter.restore(storagetypeJob);
    	afterRestore(storagetypeJob);
    	return archiveResponse; 
   	
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
