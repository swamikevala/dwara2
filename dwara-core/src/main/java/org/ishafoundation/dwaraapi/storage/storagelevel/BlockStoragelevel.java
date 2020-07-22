package org.ishafoundation.dwaraapi.storage.storagelevel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Archiveformat;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.IArchiveformatter;
import org.ishafoundation.dwaraapi.storage.model.ArchiveformatJob;
import org.ishafoundation.dwaraapi.storage.model.DiskJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.ishafoundation.dwaraapi.storage.model.TapeJob;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Artifact;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.File;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Volumeindex;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.Volumeinfo;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.label.LabelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Component("block"+DwaraConstants.STORAGELEVEL_SUFFIX)
//@Profile({ "!dev & !stage" })
public class BlockStoragelevel implements IStoragelevel {

	private static final Logger logger = LoggerFactory.getLogger(BlockStoragelevel.class);
	
	@Autowired
	private Map<String, IArchiveformatter> iArchiveformatterMap;

	@Autowired
	private LabelManager labelManager;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Override
	public StorageResponse format(StoragetypeJob storagetypeJob) throws Exception{
		
		boolean status = labelManager.writeVolumeLabel(storagetypeJob);
		logger.debug("Labelling success ? - " + status);
		
		return new StorageResponse();
	}

	@Override
	public StorageResponse write(StoragetypeJob storagetypeJob) throws Exception{
		StorageResponse storageResponse = new StorageResponse();
		StorageJob storageJob = storagetypeJob.getStorageJob();
		logger.debug("Writing blocks");
		Archiveformat archiveformat = storageJob.getVolume().getArchiveformat();
    	IArchiveformatter archiveFormatter = iArchiveformatterMap.get(archiveformat.getId() + DwaraConstants.ARCHIVER_SUFFIX);
    	
    	ArchiveformatJob archiveformatJob = instantiateArchiveJobWithCommonFields(storagetypeJob);
//		String artifactSourcePath = storageJob.getArtifactPrefixPath();
//		String artifactNameToBeWritten = storageJob.getArtifactName();
//		
//		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
//		int archiveBlocksize = archiveformatJob.getArchiveBlocksize();
//		String deviceName = archiveformatJob.getDeviceName();
//		try {
//			 ArchiveResponse archiveResponse = archiveFormatter.write(artifactSourcePath, volumeBlocksize, archiveBlocksize, deviceName, artifactNameToBeWritten);

    	archiveformatJob.setArtifactSourcePath(storageJob.getArtifactPrefixPath());
    	archiveformatJob.setArtifactNameToBeWritten(storageJob.getArtifactName());
		
		try {
			 ArchiveResponse archiveResponse = archiveFormatter.write(archiveformatJob);
	 
			 storageResponse.setArchiveResponse(archiveResponse);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return storageResponse;
	}

	@Override
	public StorageResponse verify(StoragetypeJob storagetypeJob) throws Exception{
		logger.debug("Verifying blocks");
		StorageJob storageJob = storagetypeJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		Archiveformat archiveformat = volume.getArchiveformat();
    	IArchiveformatter archiveFormatter = iArchiveformatterMap.get(archiveformat.getId() + DwaraConstants.ARCHIVER_SUFFIX);
		StorageResponse storageResponse = new StorageResponse();
    	ArchiveformatJob archiveformatJob = instantiateArchiveJobWithCommonFields(storagetypeJob);
    	archiveformatJob.setDestinationPath(storageJob.getDestinationPath());
 		try {		
			 ArchiveResponse archiveResponse = archiveFormatter.verify(archiveformatJob);
	
			 storageResponse.setArchiveResponse(archiveResponse);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		return storageResponse;
	}

	@Override
	public StorageResponse finalize(StoragetypeJob storagetypeJob) throws Exception{
		StorageJob storageJob = storagetypeJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		Domain domain = storageJob.getJob().getRequest().getDomain();
		
		
		
		Volumeinfo volumeinfo = new Volumeinfo();
		volumeinfo.setUid(volume.getUid());
		volumeinfo.setBlocksize(volume.getDetails().getBlocksize());
		volumeinfo.setChecksumalgorithm(volume.getChecksumtype().name());
		//TODO : ??? volumeinfo.setEncryptionalgorithm(encryptionalgorithm);
		
		
		ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
		FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
		ArtifactRepository artifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);
		
		List<ArtifactVolume> artifactVolumeList = domainSpecificArtifactVolumeRepository.findAllByIdVolumeId(volume.getId());
		List<Artifact> artifactList = new ArrayList<Artifact>();
		for (ArtifactVolume artifactVolume : artifactVolumeList) {
			Artifact artifact = new Artifact();
			artifact.setArchivenumber(9999);//TODO : ??? artifactVolume.getDetails().getArchive_id());
			int artifactId = artifactVolume.getId().getArtifactId();
			org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact artifactDbObj = (org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact) artifactRepository.findById(artifactId).get();
			//org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact artifactDbObj = domainUtil.getDomainSpecificArtifact(domain, artifactId);
			
			artifact.setArtifactclassuid(artifactDbObj.getArtifactclass().getUid());
			artifact.setSequencecode(artifactDbObj.getSequenceCode());
			
			List<File> fileList = new ArrayList<File>();
	    	FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
	    	Method fileDaoFindAllBy = domainSpecificFileRepository.getClass().getMethod(FileRepository.FIND_ALL_BY_ARTIFACT_ID.replace("<<DOMAIN_SPECIFIC_ARTIFACT>>", artifactDbObj.getClass().getSimpleName()), int.class);
//	    	Method fileDaoFindAllBy = domainSpecificFileRepository.getClass().getMethod("findAllBy" + artifact.getClass().getSimpleName() + "Id", int.class);
			List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList = (List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File>) fileDaoFindAllBy.invoke(domainSpecificFileRepository, artifactId);
			for (org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile : artifactFileList) {
				File file = new File();
				file.setName(nthFile.getPathname());
				file.setSize(nthFile.getSize());
				byte[] checksum = nthFile.getChecksum();
				if(checksum != null)
					file.setChecksum(Hex.encodeHexString(checksum));
				
				FileVolume fileVolume = domainUtil.getDomainSpecificFileVolume(domain, nthFile.getId(), volume.getId());// lets just let users use the util consistently
				file.setEncrypted(fileVolume.isEncrypted());
				file.setStartblock(fileVolume.getArchiveBlock()); 
				// TODO: ??? volumeblock not needed???
				fileList.add(file);
			}
			artifact.setFile(fileList);
			artifactList.add(artifact);
		}

		Volumeindex volumeindex = new Volumeindex();
		volumeindex.setVolumeinfo(volumeinfo);
		volumeindex.setArtifact(artifactList);
		
	    XmlMapper xmlMapper = new XmlMapper();
	    xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
		String xmlFromJava = xmlMapper.writeValueAsString(volumeindex);
		// TODO - Write this to tape end???
		logger.debug("Indexing success ? - " + xmlFromJava);
		

		return new StorageResponse();
	}
	
	@Override
	public StorageResponse restore(StoragetypeJob storagetypeJob) throws Exception {
		logger.debug("Reading blocks");
		StorageResponse storageResponse = new StorageResponse();
		StorageJob storageJob = storagetypeJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		Archiveformat archiveformat = volume.getArchiveformat();
    	IArchiveformatter archiveFormatter = iArchiveformatterMap.get(archiveformat.getId() + DwaraConstants.ARCHIVER_SUFFIX);
    	
    	ArchiveformatJob archiveformatJob = instantiateArchiveJobWithCommonFields(storagetypeJob);
    	archiveformatJob.setDestinationPath(storageJob.getDestinationPath());
 		try {
			 ArchiveResponse archiveResponse = archiveFormatter.restore(archiveformatJob);

			 storageResponse.setArchiveResponse(archiveResponse);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		return storageResponse;
	}
	
	private ArchiveformatJob instantiateArchiveJobWithCommonFields(StoragetypeJob storagetypeJob) {
		ArchiveformatJob archiveformatJob = new ArchiveformatJob();
		archiveformatJob.setStoragetypeJob(storagetypeJob);
		
		StorageJob storageJob = storagetypeJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		VolumeDetails volumeDetails = volume.getDetails();
		int volumeBlocksize = volumeDetails.getBlocksize();
		int archiveformatBlocksize = volume.getArchiveformat().getBlocksize();
		String deviceName = storagetypeJob.getDeviceUid();
		
		archiveformatJob.setVolumeBlocksize(volumeBlocksize);
		archiveformatJob.setArchiveformatBlocksize(archiveformatBlocksize);
		archiveformatJob.setDeviceName(deviceName);
		return archiveformatJob;	

	}
}
