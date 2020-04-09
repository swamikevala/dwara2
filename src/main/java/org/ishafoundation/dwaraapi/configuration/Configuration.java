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

	private int pageSize;
	
	private String sshPrvKeyFileLocation;

	
	public String getRegexAllowedChrsInFileName() {
		return regexAllowedChrsInFileName;
	}

	public void setRegexAllowedChrsInFileName(String regexAllowedChrsInFileName) {
		this.regexAllowedChrsInFileName = regexAllowedChrsInFileName;
	}

	public boolean isLibraryFileSystemPermissionsNeedToBeSet() {
		return isLibraryFileSystemPermissionsNeedToBeSet;
	}

	public void setLibraryFileSystemPermissionsNeedToBeSet(boolean isLibraryFileSystemPermissionsNeedToBeSet) {
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

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getSshPrvKeyFileLocation() {
		return sshPrvKeyFileLocation;
	}

	public void setSshPrvKeyFileLocation(String sshPrvKeyFileLocation) {
		this.sshPrvKeyFileLocation = sshPrvKeyFileLocation;
	}
}
