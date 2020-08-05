package org.ishafoundation.dwaraapi.storage.storagelevel.block.index;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Component
public class VolumeindexManager {
	
	Logger logger = LoggerFactory.getLogger(VolumeindexManager.class);
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Value("${filesystem.temporarylocation}")
	private String filesystemTemporarylocation;

	@Autowired
	private CommandLineExecuter commandLineExecuter;
	
	public boolean writeVolumeindex(SelectedStorageJob storagetypeJob) throws Exception {
		boolean isSuccess = false;
		StorageJob storageJob = storagetypeJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		Domain domain = storageJob.getJob().getRequest().getDomain();
		
		String xmlFromJava = createVolumeindex(volume, domain);
		logger.trace(xmlFromJava);
		
		java.io.File file = new java.io.File(filesystemTemporarylocation + java.io.File.separator + volume.getUid() + "_index.xml");
		FileUtils.writeStringToFile(file, xmlFromJava);
		// TODO zip the index file...

		logger.trace(file.getAbsolutePath() + " created ");
		String deviceName = storagetypeJob.getDeviceUid();
		int blocksize = volume.getDetails().getBlocksize();
		
		// Option 2 doesnt work well for xml - CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("echo \"" + label + "\" | dd of=" + deviceName + " bs="+blocksize);
		
		
		// Option 3
		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("dd if=" + file.getAbsolutePath()  + " of=" + deviceName + " bs="+blocksize);
		FileUtils.forceDelete(file);
		logger.trace(file.getAbsolutePath() + " deleted ok.");
		
		if(cler.isComplete()) 
			isSuccess = true;
		
	
		return isSuccess;
	}
	
	private String createVolumeindex(Volume volume, Domain domain) throws Exception {
		Volumeinfo volumeinfo = new Volumeinfo();
		volumeinfo.setVolumeuid(volume.getUid());
		volumeinfo.setVolumeblocksize(volume.getDetails().getBlocksize());
		volumeinfo.setArchiveformat(volume.getArchiveformat().getId());
		volumeinfo.setArchiveblocksize(volume.getArchiveformat().getBlocksize());
		
	
		volumeinfo.setChecksumalgorithm(volume.getChecksumtype().name());
		//TODO : ??? volumeinfo.setEncryptionalgorithm(encryptionalgorithm);
		//TODO : How to get this info for a volume??? volumeinfo.setArtifactclassuid(artifactclassuid);
		
		
		ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
		FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
		ArtifactRepository artifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);
		
		List<ArtifactVolume> artifactVolumeList = domainSpecificArtifactVolumeRepository.findAllByIdVolumeId(volume.getId());
		List<Artifact> artifactList = new ArrayList<Artifact>();
		for (ArtifactVolume artifactVolume : artifactVolumeList) {
			Artifact artifact = new Artifact();
			/* Not needed
			if(artifactVolume.getDetails().getArchive_id() != null)
				artifact.setArchivenumber(artifactVolume.getDetails().getArchive_id());
			*/
			artifact.setStartblock(artifactVolume.getDetails().getStart_volume_block());
			artifact.setEndblock(artifactVolume.getDetails().getEnd_volume_block());
			int artifactId = artifactVolume.getId().getArtifactId();
			org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact artifactDbObj = (org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact) artifactRepository.findById(artifactId).get();
			//org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact artifactDbObj = domainUtil.getDomainSpecificArtifact(domain, artifactId);
			
			artifact.setArtifactclassuid(artifactDbObj.getArtifactclass().getUid());
			artifact.setSequencecode(artifactDbObj.getSequenceCode());
			
			List<File> fileList = new ArrayList<File>();
	    	FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
	    	Method fileDaoFindAllBy = domainSpecificFileRepository.getClass().getMethod(FileRepository.FIND_ALL_BY_ARTIFACT_ID.replace("<<DOMAIN_SPECIFIC_ARTIFACT>>", artifactDbObj.getClass().getSimpleName()), int.class);
	//    	Method fileDaoFindAllBy = domainSpecificFileRepository.getClass().getMethod("findAllBy" + artifact.getClass().getSimpleName() + "Id", int.class);
			List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList = (List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File>) fileDaoFindAllBy.invoke(domainSpecificFileRepository, artifactId);
			for (org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile : artifactFileList) {
				File file = new File();
				file.setName(nthFile.getPathname());
				file.setSize(nthFile.getSize());
				byte[] checksum = nthFile.getChecksum();
				if(checksum != null)
					file.setChecksum(Hex.encodeHexString(checksum));
				
				FileVolume fileVolume = domainUtil.getDomainSpecificFileVolume(domain, nthFile.getId(), volume.getId());// lets just let users use the util consistently
				file.setVolumeblock(fileVolume.getVolumeBlock());
				file.setArchiveblock(fileVolume.getArchiveBlock());
				file.setEncrypted(fileVolume.isEncrypted());
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
		return xmlFromJava;

	}
}
