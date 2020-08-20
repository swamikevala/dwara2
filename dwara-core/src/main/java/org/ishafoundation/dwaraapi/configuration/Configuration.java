package org.ishafoundation.dwaraapi.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class Configuration {
	
	private String regexAllowedChrsInFileName;
	
	private boolean isLibraryFileSystemPermissionsNeedToBeSet;

	private String libraryFile_ChangePermissionsScriptPath;
	
	private String[] junkFilesFinderRegexPatternList;

	private String junkFilesStagedDirName;
	
	private String readyToIngestSrcDirRoot;
	
	private String sshPrvKeyFileLocation;
	
	private String checksumType;
	
	private boolean checksumTypeSupportsStreamingVerification;

	private String encryptionAlgorithm;

	private int rightVolumeCheckInterval; // in seconds

	private String restoreTmpLocationForVerification;
	
	private String restoreInProgressFileIdentifier;
	
	public String getRegexAllowedChrsInFileName() {
		return regexAllowedChrsInFileName;
	}

	public void setRegexAllowedChrsInFileName(String regexAllowedChrsInFileName) {
		this.regexAllowedChrsInFileName = regexAllowedChrsInFileName;
	}

	public boolean isLibraryFileSystemPermissionsNeedToBeSet() {
		return isLibraryFileSystemPermissionsNeedToBeSet;
	}

	public void setIsLibraryFileSystemPermissionsNeedToBeSet(boolean isLibraryFileSystemPermissionsNeedToBeSet) {
		this.isLibraryFileSystemPermissionsNeedToBeSet = isLibraryFileSystemPermissionsNeedToBeSet;
	}

	public String getLibraryFile_ChangePermissionsScriptPath() {
		return libraryFile_ChangePermissionsScriptPath;
	}

	public void setLibraryFile_ChangePermissionsScriptPath(String libraryFile_ChangePermissionsScriptPath) {
		this.libraryFile_ChangePermissionsScriptPath = libraryFile_ChangePermissionsScriptPath;
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

	public int getRightVolumeCheckInterval() {
		return rightVolumeCheckInterval;
	}

	public void setRightVolumeCheckInterval(int rightVolumeCheckInterval) {
		this.rightVolumeCheckInterval = rightVolumeCheckInterval;
	}

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
}
