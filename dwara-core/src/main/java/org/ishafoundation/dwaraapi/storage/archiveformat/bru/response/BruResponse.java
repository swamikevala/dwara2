package org.ishafoundation.dwaraapi.storage.archiveformat.bru.response;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.storage.archiveformat.bru.response.components.ErrorDescription;
import org.ishafoundation.dwaraapi.storage.archiveformat.bru.response.components.File;
import org.ishafoundation.dwaraapi.storage.archiveformat.bru.response.components.FilesProcessed;

public class BruResponse {
	private String archiveId;
	private int bufferSize;
	private String operationType;
	private List<File> fileList = new ArrayList<File>();
	private List<ErrorDescription> errorDescriptionList = new ArrayList<ErrorDescription>();
	private long startedAt;
	private long completedAt;
	private int warningCnt;
	private int errorCnt;
	private int archiveSize;
	private int archiveBlocks;
	private FilesProcessed filesProcessed;
	private int filesSkipped;
	private int softErrorCnt;
	private int hardErrorCnt;
	private int checksumErrorCnt;
	

	public String getArchiveId() {
		return archiveId;
	}
	public void setArchiveId(String archiveId) {
		this.archiveId = archiveId;
	}
	public int getBufferSize() {
		return bufferSize;
	}
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	public String getOperationType() {
		return operationType;
	}
	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}
	public List<File> getFileList() {
		return fileList;
	}
	public void setFileList(List<File> fileList) {
		this.fileList = fileList;
	}
	public List<ErrorDescription> getErrorDescriptionList() {
		return errorDescriptionList;
	}
	public void setErrorDescriptionList(List<ErrorDescription> errorDescriptionList) {
		this.errorDescriptionList = errorDescriptionList;
	}
	public long getStartedAt() {
		return startedAt;
	}
	public void setStartedAt(long startedAt) {
		this.startedAt = startedAt;
	}
	public long getCompletedAt() {
		return completedAt;
	}
	public void setCompletedAt(long completedAt) {
		this.completedAt = completedAt;
	}
	public int getWarningCnt() {
		return warningCnt;
	}
	public void setWarningCnt(int warningCnt) {
		this.warningCnt = warningCnt;
	}
	public int getErrorCnt() {
		return errorCnt;
	}
	public void setErrorCnt(int errorCnt) {
		this.errorCnt = errorCnt;
	}
	public int getArchiveSize() {
		return archiveSize;
	}
	public void setArchiveSize(int archiveSize) {
		this.archiveSize = archiveSize;
	}
	public int getArchiveBlocks() {
		return archiveBlocks;
	}
	public void setArchiveBlocks(int archiveBlocks) {
		this.archiveBlocks = archiveBlocks;
	}
	public FilesProcessed getFilesProcessed() {
		return filesProcessed;
	}
	public void setFilesProcessed(FilesProcessed filesProcessed) {
		this.filesProcessed = filesProcessed;
	}
	public int getFilesSkipped() {
		return filesSkipped;
	}
	public void setFilesSkipped(int filesSkipped) {
		this.filesSkipped = filesSkipped;
	}
	public int getSoftErrorCnt() {
		return softErrorCnt;
	}
	public void setSoftErrorCnt(int softErrorCnt) {
		this.softErrorCnt = softErrorCnt;
	}
	public int getHardErrorCnt() {
		return hardErrorCnt;
	}
	public void setHardErrorCnt(int hardErrorCnt) {
		this.hardErrorCnt = hardErrorCnt;
	}
	public int getChecksumErrorCnt() {
		return checksumErrorCnt;
	}
	public void setChecksumErrorCnt(int checksumErrorCnt) {
		this.checksumErrorCnt = checksumErrorCnt;
	}
	@Override
	public String toString() {
		
		return "operationType : " + operationType + " fileList : " + fileList + " errorDescriptionList : " + errorDescriptionList + " startedAt : " + startedAt + " completedAt : " + completedAt + " warningCnt : " + warningCnt
				+ " errorCnt : " + errorCnt + " archiveSize : " + archiveSize + " archiveBlocks : " + archiveBlocks + " filesProcessed : {" + filesProcessed + "} filesSkipped : " + filesSkipped
				+ " softErrorCnt : " + softErrorCnt + " hardErrorCnt : " + hardErrorCnt + " checksumErrorCnt : " + checksumErrorCnt;
	}
}
