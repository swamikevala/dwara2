package org.ishafoundation.dwaraapi.storage.storagelevel.block.label;

import java.time.LocalDateTime;

import org.apache.commons.lang3.SystemUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Component
@Primary
//@Profile({ "!dev & !stage" })
public class LabelManagerImpl implements LabelManager{
	
	Logger logger = LoggerFactory.getLogger(LabelManagerImpl.class);

//	@Autowired
//	private CommandLineExecuter commandLineExecuter;

	// TODO : Hardcoded stuff - Configure it...
	private Double version = 1.0;
	private int accesslevel = 1;
	private Double archiveformatVersion = 1.0;
	
	@Value("${tape.volumelabel1.implid}")
	private String implId;
	
	@Value("${tape.volumelabel1.ownerid}")
	private String ownerId;



	@Override
	public boolean isRightVolume(StoragetypeJob storagetypeJob) throws Exception {
		boolean isRightVolume = false;
		
		StorageJob storageJob = storagetypeJob.getStorageJob();
		String volumeUid = storageJob.getVolume().getUid();
		String deviceName = storagetypeJob.getDeviceUid();

		Volumelabel volumelabel = readVolumeLabel(deviceName);
		String volIdFromLabel = volumelabel.getVolumeuid();

		if(volIdFromLabel.equals(volumeUid)) {
			isRightVolume = true;
			logger.trace("Right volume");
		}
		else {
			String errorMsg = "Loaded volume " + volumeUid + " mismatches with volumeId on label " + volIdFromLabel + ". Needs admin eyes";
			logger.error(errorMsg);
			//throw new Exception(errorMsg);
		}

		return isRightVolume;
	}

	private Volumelabel readVolumeLabel(String deviceName) throws Exception{
		String label = getLabel(deviceName);
		XmlMapper xmlMapper = new XmlMapper();
		return xmlMapper.readValue(label, Volumelabel.class);
	}
	
	private String getLabel(String dataTransferElementName) {
//		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("dd if=" + dataTransferElementName + " bs=80");
//		String resp = cler.getStdOutResponse();
//		String label = StringUtils.substring(resp, 0, 80);
//		return label;
		return "";
	}
	
	@Override
	public boolean writeVolumeLabel(StoragetypeJob storagetypeJob) throws Exception {
		boolean isSuccess = false;

		// Writing a label to the tape has multiple options..
		//		Option 1 - issue multiple commands
		//		
		//		dd << EOF of=/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst bs=80
		//		getLabel(tapeBarcode) // TODO: should this storageformat specific? anyway provide a way...
		//		EOF
		
				
		//		Option 2 - using echo		
		//		echo "VOL1V5A001L             LTFS                                                  4" | dd of=/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst bs=80
						
				
		//		Option 3 - using a temp file
		//		dd if=/data/tmp/V5A001-Label.txt of=/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst bs=80		
		StorageJob storageJob = storagetypeJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		
		String volumeUid = volume.getUid();
		String archiveformat = volume.getArchiveformat().getId();
		String checksumalgorithm = volume.getChecksumtype().name();
		
		
		VolumeDetails volumeDetails = volume.getDetails();
		int blocksize = volumeDetails.getBlocksize();
		
		Request request = storageJob.getJob().getRequest();
		RequestDetails requestDetails = request.getDetails();
		String encryptionalgorithm = requestDetails.getEncryption_algorithm();
		
		String label = createLabel(volumeUid, blocksize, archiveformat, checksumalgorithm, encryptionalgorithm);
		logger.trace(label);
//		String deviceName = storagetypeJob.getDeviceUid();
//		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("echo \"" + label + "\" | dd of=" + deviceName + " bs=80");
//		if(cler.isComplete()) 
//			isSuccess = true;
//		return isSuccess;
		return true;
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
		volumelabel.setCreator(implId);
		
		XmlMapper xmlMapper = new XmlMapper();
		return xmlMapper.writeValueAsString(volumelabel);
	}
}
