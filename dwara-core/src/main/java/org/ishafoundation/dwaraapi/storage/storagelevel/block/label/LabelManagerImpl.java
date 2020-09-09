package org.ishafoundation.dwaraapi.storage.storagelevel.block.label;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
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
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

@Component
@Primary
@Profile({ "!dev & !stage" })
public class LabelManagerImpl implements LabelManager{
	
	private static Logger logger = LoggerFactory.getLogger(LabelManagerImpl.class);
	
	private static final Pattern RELEASE_REGEX_PATTERN = Pattern.compile("release:\\s+(.*)");
	private static final Pattern VARIANT_REGEX_PATTERN = Pattern.compile("variant:\\s+(.*)");
	private static final Pattern OS_REGEX_PATTERN = Pattern.compile("(.[^ ]*)\\s+(.*)");


	// TODO : Hardcoded stuff - Configure it...
	private Double volumeLabelVersion = 1.0;
	private Double artifactLabelVersion = 1.0;
	
	@Autowired
	private CommandLineExecuter commandLineExecuter;
	
	@Value("${volume.label.ownerId}")
	private String ownerId;

	@Value("${filesystem.temporarylocation}")
	private String filesystemTemporarylocation;
	
	@Autowired
	private Configuration configuration;

	@Override
	public boolean isRightVolume(SelectedStorageJob selectedStorageJob, boolean fromVolumelabel) throws Exception {
		boolean isRightVolume = false;
		
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		
		String volumeId = volume.getUuid();

		VolumeDetails volumeDetails = volume.getDetails();
		int blocksize = volumeDetails.getBlocksize();
		

		String deviceName = selectedStorageJob.getDeviceWwnId();
		String volIdFromLabel = null;
		String artifactNameToCompare = selectedStorageJob.getStorageJob().getArtifactName(); // Will be null for finalizing scenario
		String artifactNameFromLabel = null; // Will be null for first write scenario
		if(fromVolumelabel){
			Volumelabel volumelabel = readVolumeLabel(deviceName, blocksize);
			volIdFromLabel = volumelabel.getUuid();
		}
		else { // fromArtifactlabel
			InterArtifactlabel artifactlabel = readArtifactLabel(deviceName, blocksize);
			volIdFromLabel = artifactlabel.getVolumeUuid();
			artifactNameFromLabel = artifactlabel.getArtifact();
		}
		
		if(volIdFromLabel.equals(volumeId)) {// && (artifactNameFromLabel == null || artifactNameToCompare == null || (artifactNameFromLabel != null && artifactNameToCompare != null && artifactNameFromLabel.equals(artifactNameToCompare)))) {
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

	private InterArtifactlabel readArtifactLabel(String deviceName, int blocksize) throws Exception{
		String label = getLabel(deviceName, blocksize);
		XmlMapper xmlMapper = new XmlMapper();
		return xmlMapper.readValue(label, InterArtifactlabel.class);
	}
	
	private String getLabel(String dataTransferElementName, int blocksize) throws Exception {
		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("dd if=" + dataTransferElementName + " bs=" + blocksize);
		String resp = cler.getStdOutResponse();
		String label = StringUtils.substring(resp, 0, blocksize);
		return label;
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
		
		String label = createVolumeLabel(volume.getUuid(), volumeId, blocksize, archiveformat, checksumalgorithm, encryptionalgorithm);
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
	private String createVolumeLabel(String volumeUuid, String barcode, int blocksize, String archiveformat, String checksumalgorithm, String encryptionalgorithm) throws Exception {
		
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
		
		CommandLineExecutionResponse bruReleaseCler = commandLineExecuter.executeCommand("bru -h | grep release"); 
		String bruRelease = bruReleaseCler.getStdOutResponse();  //release:         18.1
		
		// regex to get the version 18.1 from above response
		Matcher releaseRegExMatcher = RELEASE_REGEX_PATTERN.matcher(bruRelease);
		String bruReleaseVer = null;
		if(releaseRegExMatcher.matches())
			bruReleaseVer = releaseRegExMatcher.group(1);


		CommandLineExecutionResponse bruVariantCler = commandLineExecuter.executeCommand("bru -h | grep variant");
		String bruVariant = bruVariantCler.getStdOutResponse(); //variant:         0.14

		// regex to get the version 0.14 from above response
		Matcher variantRegExMatcher = VARIANT_REGEX_PATTERN.matcher(bruVariant);
		String bruVariantVer = null;
		if(variantRegExMatcher.matches())
			bruReleaseVer = variantRegExMatcher.group(1);

		archivecreatorObj.setVersion("release " + bruReleaseVer + "; variant " + bruVariantVer);
		archivecreatorObj.setText(archiveformat);
		volumelabel.setArchiveCreator(archivecreatorObj);
		
		volumelabel.setChecksumalgorithm(checksumalgorithm);
		volumelabel.setEncryptionalgorithm(encryptionalgorithm);
		

		CommandLineExecutionResponse osCler = commandLineExecuter.executeCommand("uname -sr");
		String operatingSystemVersion = osCler.getStdOutResponse();  // TODO regex
		
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
		
		XmlMapper xmlMapper = new XmlMapper();
		String propName = com.ctc.wstx.api.WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL;
		xmlMapper.getFactory().getXMLOutputFactory() .setProperty(propName, true);
		xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
		return xmlMapper.writeValueAsString(volumelabel);
	}

	// volume uuid - save it - use it across - 
	// - get Name check - extra check 
	public boolean writeArtifactLabel(SelectedStorageJob selectedStorageJob) throws Exception {
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		String deviceName = selectedStorageJob.getDeviceWwnId();
		
		Artifact artifact = storageJob.getArtifact();

		InterArtifactlabel artifactlabel = new InterArtifactlabel();
		artifactlabel.setVersion(artifactLabelVersion);
		
		String artifactName = artifact.getName();
		artifactlabel.setArtifact(artifactName);
		
		artifactlabel.setSequenceCode(artifact.getSequenceCode());
		Volume volume = storageJob.getVolume();
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
		Integer svb = selectedStorageJob.getArtifactStartVolumeBlock();
		Integer evb = selectedStorageJob.getArtifactEndVolumeBlock();
		
		blocks.setStart(svb);
		blocks.setEnd(evb);
		blocks.setText((evb - svb) + "");
		artifactlabel.setBlocks(blocks);
		
		XmlMapper xmlMapper = new XmlMapper();
		String propName = com.ctc.wstx.api.WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL;
		xmlMapper.getFactory().getXMLOutputFactory().setProperty(propName, true);
		xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
		String label = xmlMapper.writeValueAsString(artifactlabel);
		logger.trace(label);

		return writeLabel(label, artifactName + "_label", deviceName, blocksize);
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
		
		File file = new File(filesystemTemporarylocation + File.separator + tempFileName + ".xml");
		FileUtils.writeStringToFile(file, label);
		logger.trace(file.getAbsolutePath() + " created ");
		
		// Option 2 doesnt work well for xml - CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("echo \"" + label + "\" | dd of=" + deviceName + " bs="+blocksize);
		
		// Option 3
		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("dd if=" + file.getAbsolutePath()  + " of=" + deviceName + " bs=" + blocksize);
		FileUtils.forceDelete(file);
		logger.trace(file.getAbsolutePath() + " deleted ok.");
		
		if(cler.isComplete()) 
			isSuccess = true;
		
		return isSuccess;
	}
}
