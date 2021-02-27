package org.ishafoundation.dwaraapi.process.request;

import org.ishafoundation.dwaraapi.process.LogicalFile;

public class ProcessContext {
	
	private String inputDirPath;
	
	private LogicalFile logicalFile;
	
	private File file;
	
	private TFile tFile;
	
	private Job job;
	
	private String outputDestinationDirPath;
	
//	private Integer priority;

	public String getInputDirPath() {
		return inputDirPath;
	}

	public void setInputDirPath(String inputDirPath) {
		this.inputDirPath = inputDirPath;
	}

	public LogicalFile getLogicalFile() {
		return logicalFile;
	}

	public void setLogicalFile(LogicalFile logicalFile) {
		this.logicalFile = logicalFile;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public TFile getTFile() {
		return tFile;
	}

	public void setTFile(TFile file) {
		this.tFile = file;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public String getOutputDestinationDirPath() {
		return outputDestinationDirPath;
	}

	public void setOutputDestinationDirPath(String outputDestinationDirPath) {
		this.outputDestinationDirPath = outputDestinationDirPath;
	}

	/*
	 * Wont be effective for the ProcessingJobProcessor object already created is sent to the executor queue and the threadpoolexecutor.*.priority updated dynamically
	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}*/
}
