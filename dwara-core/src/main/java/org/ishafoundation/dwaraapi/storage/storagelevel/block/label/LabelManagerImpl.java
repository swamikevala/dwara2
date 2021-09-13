package org.ishafoundation.dwaraapi.storage.storagelevel.block.label;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.commandline.local.RetriableCommandLineExecutorImpl;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.DeviceLockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

@Component
@Primary
@Profile({ "!dev & !stage" })
public class LabelManagerImpl implements LabelManager{
	
	private static Logger logger = LoggerFactory.getLogger(LabelManagerImpl.class);
	
	private static final Pattern BRU_RELEASE_REGEX_PATTERN = Pattern.compile("release:\\s+(.*)");
	private static final Pattern BRU_VARIANT_REGEX_PATTERN = Pattern.compile("variant:\\s+(.*)");
	private static final Pattern OS_REGEX_PATTERN = Pattern.compile("(.[^ ]*)\\s+(.*)");


	// TODO : Hardcoded stuff - Configure it...
	private Double volumeLabelVersion = 1.0;
	private Double artifactLabelVersion = 1.0;
	
	@Autowired
	private CommandLineExecuter commandLineExecuter;

	@Autowired
	private RetriableCommandLineExecutorImpl retriableCommandLineExecutorImpl;
	
	@Value("${volume.label.ownerId}")
	private String ownerId;

	@Value("${filesystem.temporarylocation}")
	private String filesystemTemporarylocation;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private DeviceLockFactory deviceLockFactory;


	public Volumelabel readVolumeLabel(String deviceName, int blocksize) throws Exception{
		String label = getLabel(deviceName, blocksize);
		XmlMapper xmlMapper = new XmlMapper();
		return xmlMapper.readValue(label, Volumelabel.class);
	}

	public InterArtifactlabel readArtifactLabel(String deviceName, int blocksize) throws Exception{
		String label = getLabel(deviceName, blocksize);
		XmlMapper xmlMapper = new XmlMapper();
		return xmlMapper.readValue(label, InterArtifactlabel.class);
	}
	
	private String getLabel(String dataTransferElementName, int blocksize) throws Exception {
		logger.info("Now reading label from " + dataTransferElementName + ":" + blocksize);
//		synchronized (deviceLockFactory.getDeviceLock(dataTransferElementName)) {
//			logger.trace("Executing read label command synchronized-ly " + dataTransferElementName + ":" + blocksize);
			CommandLineExecutionResponse cler = retriableCommandLineExecutorImpl.executeCommandWithRetriesOnSpecificError("dd if=" + dataTransferElementName + " bs=" + blocksize + " count=1", DwaraConstants.DRIVE_BUSY_ERROR);
			String resp = cler.getStdOutResponse();
			logger.trace("read label - " + resp);
//			String label = StringUtils.substring(resp, 0, blocksize);
//			logger.trace("substringed label - " + label);
//			return label;
			return resp;
//		}
	}

	@Override
	public boolean writeVolumeLabel(SelectedStorageJob selectedStorageJob) throws Exception {
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		String deviceName = selectedStorageJob.getDeviceWwnId();
		
		Volume volume = storageJob.getVolume();
		
		String volumeId = volume.getId();
		String archiveformat = volume.getArchiveformat().getId();
		String checksumalgorithm = volume.getChecksumtype().name();
		
		
		VolumeDetails volumeDetails = volume.getDetails();
		int blocksize = volumeDetails.getBlocksize();
		
		String volumeGroup = volume.getGroupRef().getId();
		
//		Request request = storageJob.getJob().getRequest();
//		RequestDetails requestDetails = request.getDetails();
//		String encryptionalgorithm = requestDetails.getEncryption_algorithm();
		String encryptionalgorithm = configuration.getEncryptionAlgorithm();
		
		Volumelabel volumelabel = createVolumeLabel(volume.getUuid(), volumeId, blocksize, archiveformat, checksumalgorithm, encryptionalgorithm);
		String label = labelXmlAsString(volumelabel, true);
		logger.trace(label);
		return writeLabel(label, volumeId + "_label", deviceName, blocksize);
	}
	
	/*
	 * 
		[root@dev-ingest archived]# uname -sr
		Linux 3.10.0-1062.4.3.el7.x86_64
		[root@dev-ingest archived]# uname
		Linux
		[root@dev-ingest archived]# bru -h | grep release
		    release:         18.1
		[root@dev-ingest archived]# bru -h | grep variant
		    variant:         0.14
		[root@dev-ingest archived]# tar --version | head -n1
		tar (GNU tar) 1.26
	 * 
	 */
	private Volumelabel createVolumeLabel(String volumeUuid, String barcode, int blocksize, String archiveformat, String checksumalgorithm, String encryptionalgorithm) throws Exception {
		
		Volumelabel volumelabel = new Volumelabel();
		volumelabel.setVersion(volumeLabelVersion);
		
		volumelabel.setUuid(volumeUuid);
		volumelabel.setBarcode(barcode);
		volumelabel.setBlocksize(blocksize);
		volumelabel.setOwner(ownerId);
		
		ZonedDateTime zdt = LocalDateTime.now().atZone(ZoneId.of("UTC"));
		String labeltime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(zdt);
		volumelabel.setInitializedAt(labeltime);
		
		volumelabel.setArchiveformat(archiveformat);
		
		
		ArchiveCreator archivecreatorObj = new ArchiveCreator();
		String archiveCreatorVersion = null;
		if(archiveformat.equals("tar")) {
			CommandLineExecutionResponse tarArchiveCreatorVersionCler = commandLineExecuter.executeCommand("tar --version"); // NOTE - tar --version | head -n1 doesnt work 
			archiveCreatorVersion = tarArchiveCreatorVersionCler.getStdOutResponse().trim();  //(GNU tar) 1.26
			archiveCreatorVersion = StringUtils.substringBefore(archiveCreatorVersion, System.getProperty("line.separator"));
		}
		else if(archiveformat.equals("bru")) {
			CommandLineExecutionResponse bruCler = commandLineExecuter.executeCommand("bru -h"); 
			String bruResponse = bruCler.getStdOutResponse();  
			
			// regex to get the version 18.1 from above response
			Matcher releaseRegExMatcher = BRU_RELEASE_REGEX_PATTERN.matcher(bruResponse); //release:         18.1
			String bruReleaseVer = null;
			if(releaseRegExMatcher.find())
				bruReleaseVer = releaseRegExMatcher.group(1).trim();
	
			// regex to get the version 0.14 from above response
			Matcher variantRegExMatcher = BRU_VARIANT_REGEX_PATTERN.matcher(bruResponse); //variant:         0.14
			String bruVariantVer = null;
			if(variantRegExMatcher.find())
				bruVariantVer = variantRegExMatcher.group(1).trim();
	
			archiveCreatorVersion = "release " + bruReleaseVer + "; variant " + bruVariantVer;
		}
		archivecreatorObj.setVersion(archiveCreatorVersion);
		archivecreatorObj.setText(archiveformat);
		volumelabel.setArchiveCreator(archivecreatorObj);
		
		volumelabel.setChecksumalgorithm(checksumalgorithm);
		volumelabel.setEncryptionalgorithm(encryptionalgorithm);
		

		CommandLineExecutionResponse osCler = commandLineExecuter.executeCommand("uname -sr");
		String operatingSystemVersion = osCler.getStdOutResponse().trim();  //Linux 3.10.0-1062.4.3.el7.x86_64
		
		Matcher osRegExMatcher = OS_REGEX_PATTERN.matcher(operatingSystemVersion);
		String version = null;
		String text = null;
		if(osRegExMatcher.matches()) {
			text = osRegExMatcher.group(1);
			version = osRegExMatcher.group(2);
		}
		
		OperatingSystem os = new OperatingSystem();
		os.setVersion(version);
		os.setText(text);
		volumelabel.setOperatingSystem(os);
		
		return volumelabel;
	}

	// volume uuid - save it - use it across - 
	// - get Name check - extra check 
	public boolean writeArtifactLabel(SelectedStorageJob selectedStorageJob) throws Exception {
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		String deviceName = selectedStorageJob.getDeviceWwnId();
		
		Artifact artifact = storageJob.getArtifact();

		Volume volume = storageJob.getVolume();
		Integer svb = selectedStorageJob.getArtifactStartVolumeBlock();
		Integer evb = selectedStorageJob.getArtifactEndVolumeBlock();
		

		InterArtifactlabel artifactlabel = generateArtifactLabel(artifact, volume, svb, evb);
		String label = labelXmlAsString(artifactlabel); // TODO Find out why not indent???
		logger.trace("artifactLabel" + label);
		return writeLabel(label, storageJob.getJob().getId() + "_label", deviceName, artifactlabel.getBlocksize());
	}
	
	// This method saves a placeholder file to occupy disk space before any write happens to avoid - "a tape with artifact data written, but failing at the time of artifact label generation due to no free disk space"
	public void writeArtifactLabelTemporarilyOnDisk(SelectedStorageJob selectedStorageJob) throws Exception {
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		
		InputStream inputStream = getClass().getResourceAsStream("/template/PlaceholderArtifactLabelTemplate.txt");
		String placeholderLabel = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
	
		writeLabelTemporarilyOnDisk(placeholderLabel, storageJob.getJob().getId() + "_label");
	}
	
	public InterArtifactlabel generateArtifactLabel(Artifact artifact, Volume volume, Integer svb, Integer evb) throws Exception {

		InterArtifactlabel artifactlabel = new InterArtifactlabel();
		artifactlabel.setVersion(artifactLabelVersion);
		
		String artifactName = artifact.getName();
		artifactlabel.setArtifact(artifactName);
		
		artifactlabel.setSequenceCode(artifact.getSequenceCode());
		VolumeDetails volumeDetails = volume.getDetails();
		
		String volumeId = volume.getId();
		int blocksize = volumeDetails.getBlocksize();
		
		artifactlabel.setVolumeUuid(volume.getUuid());
		
		ZonedDateTime zdt = LocalDateTime.now().atZone(ZoneId.of("UTC"));
		String labeltime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(zdt);
		artifactlabel.setWrittenAt(labeltime);
		artifactlabel.setFileCount(artifact.getFileCount());		
		artifactlabel.setTotalSize(artifact.getTotalSize());
		artifactlabel.setBlocksize(blocksize);
		
		Blocks blocks = new Blocks();
		
		blocks.setStart(svb);
		blocks.setEnd(evb);
		blocks.setText((evb - svb) + "");
		artifactlabel.setBlocks(blocks);
		
		return artifactlabel;
	}
	
	public String labelXmlAsString(Object label) throws Exception {
		return labelXmlAsString(label, false);
	}

	public String labelXmlAsString(Object label, boolean withIndent) throws Exception {
		XmlMapper xmlMapper = new XmlMapper();
		String propName = com.ctc.wstx.api.WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL;
		xmlMapper.getFactory().getXMLOutputFactory().setProperty(propName, true);
		xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
		if(withIndent)
			xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
		return xmlMapper.writeValueAsString(label);
	}

	private void writeLabelTemporarilyOnDisk(String label, String tempFileName) throws Exception {
		File file = new File(filesystemTemporarylocation + File.separator + tempFileName + ".xml");
		FileUtils.writeStringToFile(file, label);
		logger.trace(file.getAbsolutePath() + " created with placeholder data");
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
	private boolean writeLabel(String label, String tempFileName, String deviceName, int blocksize) throws Exception {
		boolean isSuccess = false;
		logger.trace("Now writing label on " + deviceName);
		File file = new File(filesystemTemporarylocation + File.separator + tempFileName + ".xml");
		FileUtils.writeStringToFile(file, label);
		logger.trace(file.getAbsolutePath() + " created ");
		
		// Option 2 doesnt work well for xml - CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("echo \"" + label + "\" | dd of=" + deviceName + " bs="+blocksize);
		
		// Option 3
//		synchronized (deviceLockFactory.getDeviceLock(deviceName)) {
//			logger.trace("Executing writing label command synchronized-ly " + deviceName + ":" + blocksize);
			CommandLineExecutionResponse cler = retriableCommandLineExecutorImpl.executeCommandWithRetriesOnSpecificError("dd if=" + file.getAbsolutePath()  + " of=" + deviceName + " bs=" + blocksize, DwaraConstants.DRIVE_BUSY_ERROR);
			FileUtils.forceDelete(file);
			logger.trace(file.getAbsolutePath() + " deleted ok.");
			
			if(cler.isComplete()) 
				isSuccess = true;
//		}
		return isSuccess;
	}
}
