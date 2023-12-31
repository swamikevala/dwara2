package org.ishafoundation.dwaraapi.commandline.local;

import java.util.List;

/*
 * Interface to aid unit testing
 * 
 */
public interface CommandLineExecuter {

	public Process createProcess(String command) throws Exception;
	
	public Process createProcess(List<String> commandList) throws Exception;
	
	public CommandLineExecutionResponse executeCommand(String command) throws Exception;
	
	public CommandLineExecutionResponse executeCommand(String command, boolean extractLastLineAsFailureReason) throws Exception;
	
	public CommandLineExecutionResponse executeCommand(List<String> commandList) throws Exception;
	
	public CommandLineExecutionResponse executeCommand(List<String> commandList, boolean extractLastLineAsFailureReason) throws Exception;
	
	public CommandLineExecutionResponse executeCommand(String command, Process proc) throws Exception;
	
	public CommandLineExecutionResponse executeCommand(String command, Process proc, boolean extractLastLineAsFailureReason) throws Exception;
	
	public CommandLineExecutionResponse executeCommand(List<String> commandList, Process proc) throws Exception;
	
	public CommandLineExecutionResponse executeCommand(List<String> commandList, Process proc, boolean extractLastLineAsFailureReason) throws Exception;
}
