package org.ishafoundation.dwaraapi.commandline.local;

import java.util.List;

import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;

/*
 * Interface to aid unit testing
 * 
 */
public interface CommandLineExecuter {

	public Process createProcess(List<String> commandList) ;
	
	public CommandLineExecutionResponse executeCommand(String command);
	
	public CommandLineExecutionResponse executeCommand(String command, String commandErrorFilePathname);
	
	public CommandLineExecutionResponse executeCommand(List<String> commandList, String commandErrorFilePathname) ;
	
	public CommandLineExecutionResponse executeCommand(List<String> commandList, Process proc, String commandErrorFilePathname);
	
}
