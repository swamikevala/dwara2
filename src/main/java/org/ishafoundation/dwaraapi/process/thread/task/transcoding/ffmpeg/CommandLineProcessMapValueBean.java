package org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg;

public class CommandLineProcessMapValueBean {
	
	private Process process;
	
	private boolean isKillProcessInitiated;

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

	public boolean isKillProcessInitiated() {
		return isKillProcessInitiated;
	}

	public void setKillProcessInitiated(boolean isKillProcessInitiated) {
		this.isKillProcessInitiated = isKillProcessInitiated;
	}
}
