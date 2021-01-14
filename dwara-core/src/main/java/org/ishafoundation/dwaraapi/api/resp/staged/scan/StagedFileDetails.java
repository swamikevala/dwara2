package org.ishafoundation.dwaraapi.api.resp.staged.scan;

import java.util.List;

import org.ishafoundation.dwaraapi.staged.scan.Error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class StagedFileDetails {
	private String path;
	private String name;
	private String suggestedName;
	private Integer fileCount;
	private Long totalSize;
	private Long fileSizeInBytes;
	private String prevSequenceCode;
	private Boolean prevSequenceCodeExpected;
	private List<Error> errors;
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSuggestedName() {
		return suggestedName;
	}
	public void setSuggestedName(String suggestedName) {
		this.suggestedName = suggestedName;
	}
	public Integer getFileCount() {
		return fileCount;
	}
	public void setFileCount(Integer fileCount) {
		this.fileCount = fileCount;
	}
	public Long getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(Long totalSize) {
		this.totalSize = totalSize;
	}
	public Long getFileSizeInBytes() {
		return fileSizeInBytes;
	}
	public void setFileSizeInBytes(Long fileSizeInBytes) {
		this.fileSizeInBytes = fileSizeInBytes;
	}
	public String getPrevSequenceCode() {
		return prevSequenceCode;
	}
	public void setPrevSequenceCode(String prevSequenceCode) {
		this.prevSequenceCode = prevSequenceCode;
	}
	public Boolean getPrevSequenceCodeExpected() {
		return prevSequenceCodeExpected;
	}
	public void setPrevSequenceCodeExpected(Boolean prevSequenceCodeExpected) {
		this.prevSequenceCodeExpected = prevSequenceCodeExpected;
	}
	public List<Error> getErrors() {
		return errors;
	}
	public void setErrors(List<Error> errors) {
		this.errors = errors;
	}
}
