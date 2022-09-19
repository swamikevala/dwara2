package org.ishafoundation.dwaraapi.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class Configuration {
	
	private String appMode;
	
	private String regexAllowedChrsInFileName;
	
	private boolean setArtifactFileSystemPermissions;
	
	private String stagingOpsScriptPath;
	
	private String[] junkFilesFinderRegexPatternList;

	private String junkFilesStagedDirName;
	
	private String readyToIngestSrcDirRoot;
	
	private String ingestCompleteDirRoot;
	
	private String sshPrvKeyFileLocation;
	
	private String checksumType;
	
	private boolean checksumTypeSupportsStreamingVerification;

	private String encryptionAlgorithm;

//	private int rightVolumeCheckInterval; // in seconds

	private String restoreTmpLocationForVerification;
	
	private String restoreInProgressFileIdentifier;
	
	private boolean allowForceOptionForTesting;
	
	private int allowedAutoRequeueAttemptsOnFailedStorageJobs;
	
	private String sshSystemUser;
	
	private boolean checksumRsync;
	
	private String bwLimitRsync;
	
	private String restructuredMezzanineForWritingFolderPath; // example /data/dwara/forWriting
	
	private String restructuredMezzanineFolderName; // example restructured
	
	
	public String getAppMode() {
		return appMode;
	}

	public void setAppMode(String appMode) {
		this.appMode = appMode;
	}

	public String getRegexAllowedChrsInFileName() {
		return regexAllowedChrsInFileName;
	}

	public void setRegexAllowedChrsInFileName(String regexAllowedChrsInFileName) {
		this.regexAllowedChrsInFileName = regexAllowedChrsInFileName;
	}

	public boolean isSetArtifactFileSystemPermissions() {
		return setArtifactFileSystemPermissions;
	}

	public void setSetArtifactFileSystemPermissions(boolean setArtifactFileSystemPermissions) {
		this.setArtifactFileSystemPermissions = setArtifactFileSystemPermissions;
	}
	
	public String getStagingOpsScriptPath() {
		return stagingOpsScriptPath;
	}

	public void setStagingOpsScriptPath(String stagingOpsScriptPath) {
		this.stagingOpsScriptPath = stagingOpsScriptPath;
	}

	public String[] getJunkFilesFinderRegexPatternList() {
		return junkFilesFinderRegexPatternList;
	}

	public void setJunkFilesFinderRegexPatternList(String[] junkFilesFinderRegexPatternList) {
		this.junkFilesFinderRegexPatternList = junkFilesFinderRegexPatternList;
	}
	
	public String getJunkFilesStagedDirName() {
		return junkFilesStagedDirName;
	}

	public void setJunkFilesStagedDirName(String junkFilesStagedDirName) {
		this.junkFilesStagedDirName = junkFilesStagedDirName;
	}

	public String getReadyToIngestSrcDirRoot() {
		return readyToIngestSrcDirRoot;
	}

	public void setReadyToIngestSrcDirRoot(String readyToIngestSrcDirRoot) {
		this.readyToIngestSrcDirRoot = readyToIngestSrcDirRoot;
	}

	public String getIngestCompleteDirRoot() {
		return ingestCompleteDirRoot;
	}

	public void setIngestCompleteDirRoot(String ingestCompleteDirRoot) {
		this.ingestCompleteDirRoot = ingestCompleteDirRoot;
	}

	public String getSshPrvKeyFileLocation() {
		return sshPrvKeyFileLocation;
	}

	public void setSshPrvKeyFileLocation(String sshPrvKeyFileLocation) {
		this.sshPrvKeyFileLocation = sshPrvKeyFileLocation;
	}

	public String getChecksumType() {
		return checksumType;
	}

	public void setChecksumType(String checksumType) {
		this.checksumType = checksumType;
	}

	public boolean checksumTypeSupportsStreamingVerification() {
		return checksumTypeSupportsStreamingVerification;
	}

	public void setChecksumTypeSupportsStreamingVerification(boolean checksumTypeSupportsStreamingVerification) {
		this.checksumTypeSupportsStreamingVerification = checksumTypeSupportsStreamingVerification;
	}

	public String getEncryptionAlgorithm() {
		return encryptionAlgorithm;
	}

	public void setEncryptionAlgorithm(String encryptionAlgorithm) {
		this.encryptionAlgorithm = encryptionAlgorithm;
	}

//	public int getRightVolumeCheckInterval() {
//		return rightVolumeCheckInterval;
//	}
//
//	public void setRightVolumeCheckInterval(int rightVolumeCheckInterval) {
//		this.rightVolumeCheckInterval = rightVolumeCheckInterval;
//	}

	public String getRestoreTmpLocationForVerification() {
		return restoreTmpLocationForVerification;
	}

	public void setRestoreTmpLocationForVerification(String restoreTmpLocationForVerification) {
		this.restoreTmpLocationForVerification = restoreTmpLocationForVerification;
	}

	public String getRestoreInProgressFileIdentifier() {
		return restoreInProgressFileIdentifier;
	}

	public void setRestoreInProgressFileIdentifier(String restoreInProgressFileIdentifier) {
		this.restoreInProgressFileIdentifier = restoreInProgressFileIdentifier;
	}

	public boolean isAllowForceOptionForTesting() {
		return allowForceOptionForTesting;
	}

	public void setAllowForceOptionForTesting(boolean allowForceOptionForTesting) {
		this.allowForceOptionForTesting = allowForceOptionForTesting;
	}

	public int getAllowedAutoRequeueAttemptsOnFailedStorageJobs() {
		return allowedAutoRequeueAttemptsOnFailedStorageJobs;
	}

	public void setAllowedAutoRequeueAttemptsOnFailedStorageJobs(int allowedAutoRequeueAttemptsOnFailedStorageJobs) {
		this.allowedAutoRequeueAttemptsOnFailedStorageJobs = allowedAutoRequeueAttemptsOnFailedStorageJobs;
	}

	public String getSshSystemUser() {
		return sshSystemUser;
	}

	public void setSshSystemUser(String sshSystemUser) {
		this.sshSystemUser = sshSystemUser;
	}
	
	public boolean isChecksumRsync() {
		return checksumRsync;
	}

	public void setChecksumRsync(boolean checksumRsync) {
		this.checksumRsync = checksumRsync;
	}

	public String getBwLimitRsync() {
		return bwLimitRsync;
	}

	public void setBwLimitRsync(String bwLimitRsync) {
		this.bwLimitRsync = bwLimitRsync;
	}

	public String getRestructuredMezzanineFolderName() {
		return restructuredMezzanineFolderName;
	}

	public void setRestructuredMezzanineFolderName(String restructuredMezzanineFolderName) {
		this.restructuredMezzanineFolderName = restructuredMezzanineFolderName;
	}

	public String getRestructuredMezzanineForWritingFolderPath() {
		return restructuredMezzanineForWritingFolderPath;
	}

	public void setRestructuredMezzanineForWritingFolderPath(String restructuredMezzanineForWritingFolderPath) {
		this.restructuredMezzanineForWritingFolderPath = restructuredMezzanineForWritingFolderPath;
	}
}