package org.ishafoundation.dwaraapi.storage.storagelevel.block.index;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.commandline.local.RetriableCommandLineExecutorImpl;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepositoryUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TFileVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileVolume;
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
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

@Component
public class VolumeindexManager {
	
	Logger logger = LoggerFactory.getLogger(VolumeindexManager.class);
	
	@Autowired
	private TFileDao tFileDao;

	@Autowired
	private TFileVolumeDao tFileVolumeDao;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private FileRepositoryUtil fileRepositoryUtil;

	@Autowired
	private RetriableCommandLineExecutorImpl retriableCommandLineExecutorImpl;

	@Value("${filesystem.temporarylocation}")
	private String filesystemTemporarylocation;
	
	public boolean writeVolumeindex(SelectedStorageJob storagetypeJob) throws Exception {
		boolean isSuccess = false;
		StorageJob storageJob = storagetypeJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		Domain domain = storageJob.getDomain();
		
		String xmlFromJava = createVolumeindex(volume, domain);
		logger.trace(xmlFromJava);
		java.io.File file = new java.io.File(filesystemTemporarylocation + java.io.File.separator + volume.getId() + "_index.gz");
		try (GzipCompressorOutputStream out = new GzipCompressorOutputStream(new FileOutputStream(file))){
            IOUtils.copy(new ByteArrayInputStream(xmlFromJava.getBytes()), out);
        }
	
		logger.trace(file.getAbsolutePath() + " created ");
		String deviceName = storagetypeJob.getDeviceWwnId();
		int blocksize = volume.getDetails().getBlocksize();
		
		// Option 2 doesnt work well for xml - CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("echo \"" + label + "\" | dd of=" + deviceName + " bs="+blocksize);
		
		
		// Option 3
//		synchronized (deviceLockFactory.getDeviceLock(deviceName)) {
			CommandLineExecutionResponse cler = retriableCommandLineExecutorImpl.executeCommandWithRetriesOnSpecificError("dd if=" + file.getAbsolutePath()  + " of=" + deviceName + " bs="+blocksize, DwaraConstants.DRIVE_BUSY_ERROR);
			FileUtils.forceDelete(file);
			logger.trace(file.getAbsolutePath() + " deleted ok.");
			
			if(cler.isComplete()) 
				isSuccess = true;
//		}
	
		return isSuccess;
	}
	
	private String createVolumeindex(Volume volume, Domain domain) throws Exception {
		Volumeinfo volumeinfo = new Volumeinfo();
		volumeinfo.setVolumeuid(volume.getId());
		volumeinfo.setVolumeblocksize(volume.getDetails().getBlocksize());
		volumeinfo.setArchiveformat(volume.getArchiveformat().getId());
		volumeinfo.setArchiveblocksize(volume.getArchiveformat().getBlocksize());
		ZonedDateTime zdt = LocalDateTime.now().atZone(ZoneId.of("UTC"));
		String labeltime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(zdt);
		volumeinfo.setFinalizedAt(labeltime);		
	
		volumeinfo.setChecksumalgorithm(volume.getChecksumtype().name());
		//TODO : ??? volumeinfo.setEncryptionalgorithm(encryptionalgorithm);
		//TODO : How to get this info for a volume??? volumeinfo.setArtifactclassuid(artifactclassuid);
		
		
		ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
//		FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
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
			List<TFile> artifactTFileList = tFileDao.findAllByArtifactId(artifactId);
			if(artifactTFileList != null && artifactTFileList.size() > 0) {
				for (TFile nthFile : artifactTFileList) {
					File file = new File();
					file.setName(nthFile.getPathname());
					file.setSize(nthFile.getSize());
					byte[] checksum = nthFile.getChecksum();
					if(checksum != null)
						file.setChecksum(Hex.encodeHexString(checksum));
					
					TFileVolume fileVolume = tFileVolumeDao.findByIdFileIdAndIdVolumeId(nthFile.getId(), volume.getId());// lets just let users use the util consistently
					file.setVolumeblock(fileVolume.getVolumeBlock());
					file.setArchiveblock(fileVolume.getArchiveBlock());
					file.setEncrypted(fileVolume.isEncrypted() ? true : null);
					fileList.add(file);
				}
			}
//			else { // For derived files use file table as we dont add tfile table entries
//				List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList = fileRepositoryUtil.getAllArtifactFileList(artifactDbObj, domain);
//				for (org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile : artifactFileList) {
//					File file = new File();
//					file.setName(nthFile.getPathname());
//					file.setSize(nthFile.getSize());
//					byte[] checksum = nthFile.getChecksum();
//					if(checksum != null)
//						file.setChecksum(Hex.encodeHexString(checksum));
//					
//					FileVolume fileVolume = domainUtil.getDomainSpecificFileVolume(domain, nthFile.getId(), volume.getId());// lets just let users use the util consistently
//					file.setVolumeblock(fileVolume.getVolumeBlock());
//					file.setArchiveblock(fileVolume.getArchiveBlock());
//					file.setEncrypted(fileVolume.isEncrypted() ? true : null);
//					fileList.add(file);
//				}
//			}
			artifact.setFile(fileList);
			artifactList.add(artifact);
		}
	
		Volumeindex volumeindex = new Volumeindex();
		//volumeindex.setXmlns("https://dwara.io");
		volumeindex.setVolumeinfo(volumeinfo);
		volumeindex.setArtifact(artifactList);

		
	    XmlMapper xmlMapper = new XmlMapper();
	    String propName = com.ctc.wstx.api.WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL;
	    xmlMapper.getFactory().getXMLOutputFactory().setProperty(propName, true);
	    xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
	    xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
		String xmlFromJava = xmlMapper.writeValueAsString(volumeindex);
		return xmlFromJava;

	}
}
