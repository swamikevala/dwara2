package org.ishafoundation.dwaraapi.storage.storagelevel.block.label;

import java.io.File;
import java.time.LocalDateTime;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Component
@Primary
@Profile({ "!dev & !stage" })
public class LabelManagerImpl implements LabelManager{
	
	Logger logger = LoggerFactory.getLogger(LabelManagerImpl.class);

	@Autowired
	private CommandLineExecuter commandLineExecuter;

	// TODO : Hardcoded stuff - Configure it...
	private Double version = 1.0;
	private int accesslevel = 1;
	private Double archiveformatVersion = 1.0;
	
	@Value("${volume.label.implementationId}")
	private String implementationId;
	
	@Value("${volume.label.ownerId}")
	private String ownerId;

	@Value("${filesystem.temporarylocation}")
	private String filesystemTemporarylocation;
	
	@Autowired
	private Configuration configuration;

	@Override
	public boolean isRightVolume(SelectedStorageJob selectedStorageJob) throws Exception {
		boolean isRightVolume = false;
		
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		
		String volumeId = volume.getId();

		VolumeDetails volumeDetails = volume.getDetails();
		int blocksize = volumeDetails.getBlocksize();
		

		String deviceName = selectedStorageJob.getDeviceWwnId();

		Volumelabel volumelabel = readVolumeLabel(deviceName, blocksize);
		String volIdFromLabel = volumelabel.getVolumeuid();

		if(volIdFromLabel.equals(volumeId)) {
			isRightVolume = true;
			logger.trace("Right volume");
		}
		else {
			String errorMsg = "Loaded volume " + volumeId + " mismatches with volumeId on label " + volIdFromLabel + ". Needs admin eyes";
			logger.error(errorMsg);
			throw new Exception(errorMsg);
		}

		return isRightVolume;
	}

	private Volumelabel readVolumeLabel(String deviceName, int blocksize) throws Exception{
		String label = getLabel(deviceName, blocksize);
		XmlMapper xmlMapper = new XmlMapper();
		return xmlMapper.readValue(label, Volumelabel.class);
	}
	
	private String getLabel(String dataTransferElementName, int blocksize) throws Exception {
		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("dd if=" + dataTransferElementName + " bs=" + blocksize);
		String resp = cler.getStdOutResponse();
		String label = StringUtils.substring(resp, 0, blocksize);
		return label;
	}

	/*
	 	Writing a label to the tape has multiple options..
			Option 1 - issue multiple commands
			
			dd << EOF of=/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst bs=80
			getLabel(tapeBarcode)
			EOF
	
			
			Option 2 - using echo		
			echo "^<volumelabel version="1.0"^>...THE XML BODY.." | dd of=/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst bs=80
					
			
			Option 3 - using a temp file
			dd if=/data/tmp/V5A001-Label.txt of=/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst bs=80		

	 	We are going with - Option 3
	 * 
	 */
	@Override
	public boolean writeVolumeLabel(SelectedStorageJob selectedStorageJob) throws Exception {
		boolean isSuccess = false;

		StorageJob storageJob = selectedStorageJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		
		String volumeId = volume.getId();
		String archiveformat = volume.getArchiveformat().getId();
		String checksumalgorithm = volume.getChecksumtype().name();
		
		
		VolumeDetails volumeDetails = volume.getDetails();
		int blocksize = volumeDetails.getBlocksize();
		
//		Request request = storageJob.getJob().getRequest();
//		RequestDetails requestDetails = request.getDetails();
//		String encryptionalgorithm = requestDetails.getEncryption_algorithm();
		String encryptionalgorithm = configuration.getEncryptionAlgorithm();
		
		String label = createLabel(volumeId, blocksize, archiveformat, checksumalgorithm, encryptionalgorithm);
		logger.trace(label);
		
		File file = new File(filesystemTemporarylocation + File.separator + volumeId + "_label.xml");
		FileUtils.writeStringToFile(file, label);
		logger.trace(file.getAbsolutePath() + " created ");
		String deviceName = selectedStorageJob.getDeviceWwnId();
		// Option 2 doesnt work well for xml - CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("echo \"" + label + "\" | dd of=" + deviceName + " bs="+blocksize);
		
		// Option 3
		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("dd if=" + file.getAbsolutePath()  + " of=" + deviceName + " bs="+blocksize);
		FileUtils.forceDelete(file);
		logger.trace(file.getAbsolutePath() + " deleted ok.");
		
		if(cler.isComplete()) 
			isSuccess = true;
		return isSuccess;
	}
	
	private String createLabel(String volumeUid, int blocksize, String archiveformat, String checksumalgorithm, String encryptionalgorithm) throws Exception {
		
		Volumelabel volumelabel = new Volumelabel();
		volumelabel.setVersion(version);
		volumelabel.setVolumeuid(volumeUid);
		volumelabel.setBlocksize(blocksize);
		volumelabel.setOwner(ownerId);
		volumelabel.setAccesslevel(accesslevel);
		volumelabel.setLabeltime(LocalDateTime.now().toString());
		
		Archiveformat archiveformatObj = new Archiveformat();
		archiveformatObj.setVersion(archiveformatVersion);
		archiveformatObj.setText(archiveformat);
		
		volumelabel.setArchiveformat(archiveformatObj);
		volumelabel.setChecksumalgorithm(checksumalgorithm);
		volumelabel.setEncryptionalgorithm(encryptionalgorithm);
		
		String systeminfo = SystemUtils.OS_NAME + SystemUtils.OS_VERSION + SystemUtils.OS_ARCH;
		logger.trace(systeminfo);
		volumelabel.setSysteminfo(systeminfo);
		volumelabel.setCreator(implementationId);
		
		XmlMapper xmlMapper = new XmlMapper();
		return xmlMapper.writeValueAsString(volumelabel);
	}
}
