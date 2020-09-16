package org.ishafoundation.dwaraapi.storage.storagelevel.block.index;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepositoryUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.DeviceLockFactory;
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

	@Autowired
	private FileRepositoryUtil fileRepositoryUtil;

	@Autowired
	private CommandLineExecuter commandLineExecuter;
	
	@Autowired
	private DeviceLockFactory deviceLockFactory;
	
	@Value("${filesystem.temporarylocation}")
	private String filesystemTemporarylocation;
	
	public boolean writeVolumeindex(SelectedStorageJob storagetypeJob) throws Exception {
		boolean isSuccess = false;
		StorageJob storageJob = storagetypeJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		Domain domain = storageJob.getDomain();
		
		String xmlFromJava = createVolumeindex(volume, domain);
		logger.trace(xmlFromJava);
		
		java.io.File file = new java.io.File(filesystemTemporarylocation + java.io.File.separator + volume.getId() + "_index.xml");
		FileUtils.writeStringToFile(file, xmlFromJava);
		// TODO zip the index file...

		logger.trace(file.getAbsolutePath() + " created ");
		String deviceName = storagetypeJob.getDeviceWwnId();
		int blocksize = volume.getDetails().getBlocksize();
		
		// Option 2 doesnt work well for xml - CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("echo \"" + label + "\" | dd of=" + deviceName + " bs="+blocksize);
		
		
		// Option 3
		synchronized (deviceLockFactory.getDeviceLock(deviceName)) {
			CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("dd if=" + file.getAbsolutePath()  + " of=" + deviceName + " bs="+blocksize);
			FileUtils.forceDelete(file);
			logger.trace(file.getAbsolutePath() + " deleted ok.");
			
			if(cler.isComplete()) 
				isSuccess = true;
		}
	
		return isSuccess;
	}
	
	private String createVolumeindex(Volume volume, Domain domain) throws Exception {
		Volumeinfo volumeinfo = new Volumeinfo();
		volumeinfo.setVolumeuid(volume.getId());
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
			artifact.setStartblock(artifactVolume.getDetails().getStartVolumeBlock());
			artifact.setEndblock(artifactVolume.getDetails().getEndVolumeBlock());
			int artifactId = artifactVolume.getId().getArtifactId();
			org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact artifactDbObj = (org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact) artifactRepository.findById(artifactId).get();
			//org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact artifactDbObj = domainUtil.getDomainSpecificArtifact(domain, artifactId);
			
			artifact.setArtifactclassuid(artifactDbObj.getArtifactclass().getId());
			artifact.setSequencecode(artifactDbObj.getSequenceCode());
			
			List<File> fileList = new ArrayList<File>();
			List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList = fileRepositoryUtil.getArtifactFileList(artifactDbObj, domain);
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
