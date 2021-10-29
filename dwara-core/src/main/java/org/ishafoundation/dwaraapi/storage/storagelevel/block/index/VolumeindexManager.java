package org.ishafoundation.dwaraapi.storage.storagelevel.block.index;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.commandline.local.RetriableCommandLineExecutorImpl;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TFileVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ctc.wstx.api.WstxInputProperties;
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
	private RetriableCommandLineExecutorImpl retriableCommandLineExecutorImpl;

	@Value("${filesystem.temporarylocation}")
	private String filesystemTemporarylocation;
	
	private String gzFilenameSuffix = "_index.gz";
	
	private String xmlFilenameSuffix = "_index.xml";
	
	public boolean writeVolumeindex(SelectedStorageJob storagetypeJob) throws Exception {
		boolean isSuccess = false;
		StorageJob storageJob = storagetypeJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		Domain domain = storageJob.getDomain();
		
		String tmpXmlFilepathname = filesystemTemporarylocation + java.io.File.separator + volume.getId() + xmlFilenameSuffix;
		createVolumeindexXml(volume, domain, tmpXmlFilepathname);
		java.io.File file = new java.io.File(filesystemTemporarylocation + java.io.File.separator + volume.getId() + gzFilenameSuffix);
		try (GzipCompressorOutputStream out = new GzipCompressorOutputStream(new FileOutputStream(file))){
			Files.copy(Paths.get(tmpXmlFilepathname), out);
			// Files.copy(new BufferedInputStream(new FileInputStream(filesystemTemporarylocation + java.io.File.separator + volume.getId() + xmlFilenameSuffix)), out);
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
			
			FileUtils.forceDelete(new java.io.File(tmpXmlFilepathname));
			logger.trace(tmpXmlFilepathname + " deleted ok.");
			
			if(cler.isComplete()) 
				isSuccess = true;
//		}
	
		return isSuccess;
	}
	

	public void createVolumeindexXml(Volume volume, Domain domain, String filePath) throws Exception {

		Volumeindex volumeindex = generateVolumeindex(volume, domain);
		
	    XmlMapper xmlMapper = new XmlMapper();
		//Get XMLOutputFactory instance.
		XMLOutputFactory xmlOutputFactory = xmlMapper.getFactory().getXMLOutputFactory();
	    String propName = com.ctc.wstx.api.WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL;
	    xmlOutputFactory.setProperty(propName, true);
	    xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
	    xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);

		//Create FileWriter object.
		Writer fileWriter = new FileWriter(filePath);
		//Create XMLStreamWriter object from xmlOutputFactory.
		XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(fileWriter);
		/*
			xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
			xmlOutputFactory.setProperty(WstxInputProperties.P_RETURN_NULL_FOR_DEFAULT_NAMESPACE, true);
		*/
	    xmlMapper.writeValue(xmlStreamWriter, volumeindex);
		
		volumeindex = null; // Forced quick cleanup rather waiting for GC to clean it up.
		logger.info(filePath + "succesfully created");
	}
	
	private Volumeindex generateVolumeindex(Volume volume, Domain domain) throws Exception {
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
			try {
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
						try {
							File file = new File();
							file.setName(nthFile.getPathname());
							file.setSize(nthFile.getSize());
							byte[] checksum = nthFile.getChecksum();
							if(checksum != null)
								file.setChecksum(Hex.encodeHexString(checksum));
							
							TFileVolume fileVolume = tFileVolumeDao.findByIdFileIdAndIdVolumeId(nthFile.getId(), volume.getId());
							if(nthFile.isDeleted() && fileVolume == null)
								continue; // dont add a deleted file that is not written into tape - Usecase MXF gets deleted even before its written to tape...
							file.setVolumeblock(fileVolume.getVolumeStartBlock());
							file.setArchiveblock(fileVolume.getArchiveBlock());
							file.setEncrypted(fileVolume.isEncrypted() ? true : null);
							fileList.add(file);
						}
						catch (Exception e) {
							logger.error("Skipping Tfile from volume index " + nthFile.getId(), e);
							throw e; // throwing so that we check why finalization not happening and correct data
						}
					}
				}

				artifact.setFile(fileList);
				artifactList.add(artifact);
			}catch (Exception e) {
				logger.error("Skipping artifact from volume index " + artifactVolume.getId().getArtifactId());
				throw e; // throwing so that we check why finalization not happening and correct data or identify rootcause and fix it..
			}
		}
	
		Volumeindex volumeindex = new Volumeindex();
		//volumeindex.setXmlns("https://dwara.io");
		volumeindex.setVolumeinfo(volumeinfo);
		volumeindex.setArtifact(artifactList);

		return volumeindex;
	}
}
