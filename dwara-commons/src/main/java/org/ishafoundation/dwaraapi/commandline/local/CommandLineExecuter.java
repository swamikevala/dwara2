package org.ishafoundation.dwaraapi.commandline.local;

import java.util.List;

/*
 * Interface to aid unit testing
 * 
 */
public interface CommandLineExecuter {

	public Process createProcess(List<String> commandList) throws Exception;
	
	public CommandLineExecutionResponse executeCommand(String command) throws Exception;
	
	public CommandLineExecutionResponse executeCommand(List<String> commandList) throws Exception;
	
	public CommandLineExecutionResponse executeCommand(List<String> commandList, Process proc) throws Exception;
	
}
