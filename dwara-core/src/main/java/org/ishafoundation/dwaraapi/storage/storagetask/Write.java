package org.ishafoundation.dwaraapi.storage.storagetask;


import java.util.List;

import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.model.cache.CacheableTablesList;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.json.JobDetails;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("write")
//@Profile({ "!dev & !stage" })
public class Write extends AbstractStoragetaskAction{

    private static final Logger logger = LoggerFactory.getLogger(Write.class);
    
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private VolumeDao volumeDao;
	
	@SuppressWarnings("rawtypes")
	@Autowired
	private DBMasterTablesCacheManager dBMasterTablesCacheManager;
	
	@Override
	public StorageJob buildStorageJob(Job job){

		Request request = job.getRequest();
		org.ishafoundation.dwaraapi.enumreferences.Action requestedAction = request.getActionId();
		
		Artifact artifact = null;
		String artifactName = null;
		String pathPrefix = null;
		int volumegroupId = 0;
		Volume volume = null;
		Domain domain = null;
		if(requestedAction == org.ishafoundation.dwaraapi.enumreferences.Action.ingest) {
			Integer artifactclassId = job.getRequest().getDetails().getArtifactclass_id();
			Artifactclass artifactclass = (Artifactclass) dBMasterTablesCacheManager
					.getRecord(CacheableTablesList.artifactclass.name(), artifactclassId);
			domain = artifactclass.getDomain();
			pathPrefix = artifactclass.getPath();
			
			Integer inputArtifactId = job.getInputArtifactId();
			artifact = domainUtil.getDomainSpecificArtifact(domain, inputArtifactId);
			artifactName = artifact.getName();			

			volumegroupId = job.getActionelement().getVolumeId();
			
			String artifactpathToBeCopied = pathPrefix + java.io.File.separator + artifactName;
			long sizeOfTheLibraryToBeWritten = 0;//TODO - FileUtils.sizeOfDirectory(new java.io.File(artifactpathToBeCopied)); 			
			volume = getToBeUsedPhysicalVolume(volumegroupId, sizeOfTheLibraryToBeWritten);

		}else if(requestedAction == org.ishafoundation.dwaraapi.enumreferences.Action.rewrite || requestedAction == org.ishafoundation.dwaraapi.enumreferences.Action.migrate) {
			artifactName = request.getDetails().getArtifact_name();
			pathPrefix = "whereverRestoredByThePrecedingRestoreJob";//artifactclass.getPath();
			// TODO have a util for Group Uid to Uid 
//			String volumegroupUid = request.getDetails().getVolume_group_uid();
//			Volume volume = volumeDao.findByUid(volumegroupUid);
//			volumegroupId = volume.getId();
			// TODO domain = ??
			String volumeUid = request.getDetails().getTo_volume_uid();
			volume = volumeDao.findByUid(volumeUid);
		}

		
		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);
		storageJob.setDomain(domain);
		// what needs to be ingested
		storageJob.setArtifact(artifact);
		storageJob.setArtifactPrefixPath(pathPrefix);
		storageJob.setArtifactName(artifactName);

		// to where
		storageJob.setVolume(volume);
		
		return storageJob;
	
	}
	
	private Volume getToBeUsedPhysicalVolume(int volumegroupId, long sizeOfTheLibraryToBeWritten) {
		
		Volume toBeUsedVolume = null;
		List<Volume> volumesList = volumeDao.findAllByVolumeRefIdAndFinalizedIsFalse(volumegroupId);
		for (Volume volume : volumesList) {
			//TODO if(Volume.getFree() > sizeOfTheLibraryToBeWritten) {
				toBeUsedVolume = volume; // for now defaulting to first one...
				break;
			//}
		}
		return toBeUsedVolume;		
	}
	
	@Override
	public void postProcessDbUpdates(Job job, ArchiveResponse archiveResponse){
		// file_volume and artifact_volume updated here...
		
	}
	
//	@Override
//	public ArchiveResponse execute(StoragetypeJob storagetypeJob) throws Throwable {
//		logger.debug(this.getClass().getName() + " write storagetask ");
//		Storagetype storagetype = storagetypeJob.getStorageJob().getVolume().getStoragetype();
//		//ArchiveResponse archiveResponse = stjpMap.get(storagetype.name() + "JobProcessor").write(storagejob);
//		ArchiveResponse archiveResponse = storagetypeJobProcessorMap.get(storagetype.name() + DwaraConstants.StorageTypeJobProcessorSuffix).write(storagetypeJob);
//		return archiveResponse;
//	}




//	@Autowired
//	private FileVolumeDao fileVolumeDao;	
//	
//    @Autowired
//    private FileDao fileDao;	
//    
//    @Autowired
//    private LibraryVolumeDao libraryVolumeDao;
//	
//	@Autowired
//	private ApplicationContext applicationContext;
//
//	public Write() {
////		this.storagetask = Storagetask.write;
//	}
//	
//	@Override
//	public ArchiveResponse execute(StorageJob storagejob) throws Throwable {
//		
//		String storagetype = storagejob.getStoragetype().name();
//		StoragetypeJobProcessor storagetypeJobProcessor = StoragetypeJobProcessorFactory.getInstance(applicationContext, storagetype);
//
//		ArchiveResponse archiveResponse = storagetypeJobProcessor.write(storagejob); 
//
//		
//		// TODO - This should be domain specific
//		logger.trace("Processing job using storage type - " + storagetype);
//		// Update tape specific DB - File_Tape...
//		// TODO : Check If for Mezz copy libraryId = MezzLibraryId coming from input...
//		Library library = storagejob.getLibrary();
//		int libraryId = library.getId();
//		Volume volume = storagejob.getVolume();
//		// Get a map of Paths and their File1 Ids
//		List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File1> libraryFileList = fileDao.findAllByLibraryId(libraryId);
//		
//		HashMap<Integer, String> fileIdToPath = new HashMap<Integer, String>();
//		HashMap<String, Integer> filePathToId = new HashMap<String, Integer>();
//		HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.domain.File1> filePathNameTofileObj = new HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.domain.File1>();
//		for (Iterator<org.ishafoundation.dwaraapi.db.model.transactional.domain.File1> iterator = libraryFileList.iterator(); iterator.hasNext();) {
//			org.ishafoundation.dwaraapi.db.model.transactional.domain.File1 nthFile = iterator.next();
//			filePathNameTofileObj.put(nthFile.getPathname(), nthFile);
//			fileIdToPath.put(nthFile.getId(), nthFile.getPathname());
//		}
//
//		int libraryBlock = 0;
//		//  some tape specific code like updating db file_tape and tapedrive...
//		List<ArchivedFile> archivedFileList = archiveResponse.getArchivedFileList();
//		List<FileVolume> toBeAddedFileVolumeTableEntries = new ArrayList<FileVolume>();
//		for (Iterator<ArchivedFile> iterator = archivedFileList.iterator(); iterator.hasNext();) {
//			ArchivedFile archivedFile = (ArchivedFile) iterator.next();
//			String fileName = archivedFile.getFilePathName(); 
//			
//			// TODO get the file id from File1 table using this fileName...
//			org.ishafoundation.dwaraapi.db.model.transactional.domain.File1 file = null;
//			if(filePathNameTofileObj != null)
//				file = filePathNameTofileObj.get(fileName);
//
//			//logger.trace(filePathNameTofileObj.toString());
//			logger.trace(fileName);
//			if(file == null) {
//				logger.trace("Junk folder not supposed to be written to tape. Filter it. Skipping its info getting added to DB");
//				continue;
//			}
//			
//			if(fileName.equals(library.getName()))
//				libraryBlock = archivedFile.getBlockNumber();
//			
//			FileVolume ft = new FileVolume(file, volume);
//			ft.setBlock(archivedFile.getBlockNumber());
//			//ft.setOffset(offset); // TODO ?? How
//			toBeAddedFileVolumeTableEntries.add(ft);
//		}
//		
//	    if(toBeAddedFileVolumeTableEntries.size() > 0) {
//	    	logger.debug("DB FileVolume entries Creation");   
//	    	fileVolumeDao.saveAll(toBeAddedFileVolumeTableEntries);
//	    	logger.debug("DB FileVolume entries Creation - Success");
//	    }
//	    
//	    LibraryVolume libraryVolume = new LibraryVolume(library, volume);
//	    libraryVolume.setBlock(libraryBlock);
//	    libraryVolume.setEncrypted(storagejob.isEncrypted());
//	    logger.debug("DB LibraryVolume Creation");
//	    libraryVolumeDao.save(libraryVolume);
//	    logger.debug("DB LibraryVolume Creation - Success");
//	    
//	    archiveResponse.setLibraryName(library.getName());
//	    archiveResponse.setLibraryBlockNumber(libraryBlock);
//	    return archiveResponse;
//
//	}
}
