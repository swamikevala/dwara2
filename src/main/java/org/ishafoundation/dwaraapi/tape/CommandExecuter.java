package org.ishafoundation.dwaraapi.tape;

import java.util.Arrays;
import java.util.List;

import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;

public class CommandExecuter {
		
	public static CommandLineExecutionResponse executeCommand(String command){
		String filename = command.replace(" ", "_").replace("/", "_");
		return executeCommand(command, "/data/tmp/" + filename + ".err");
	}
		
	public static CommandLineExecutionResponse executeCommand(String command, String outputFilePath){
		CommandLineExecuter commandLineExecuter = new CommandLineExecuter();
		
		String[] commandArgs = command.split(" ");
		List<String> commandList = Arrays.asList(commandArgs);
		//System.out.println("commandList " + commandList);
		CommandLineExecutionResponse commandLineExecutionResponse = commandLineExecuter.executeCommand(commandList, outputFilePath);
		if(commandLineExecutionResponse.isComplete()) {
			//System.out.println(command + " executed successfully " + commandLineExecutionResponse.getStdOutResponse());
		}
		else
			System.err.println(" execution failed " + commandLineExecutionResponse.getFailureReason() + ". Check " + outputFilePath);
		return commandLineExecutionResponse;
	}
	
	public static CommandLineExecutionResponse executeShellCommand(List<String> commandList, String outputFilePath){
		CommandLineExecuter commandLineExecuter = new CommandLineExecuter();
		//System.out.println("commandList " + commandList);
		CommandLineExecutionResponse commandLineExecutionResponse = commandLineExecuter.executeCommand(commandList, outputFilePath);
		if(commandLineExecutionResponse.isComplete()) {
			//System.out.println(commandList + " executed successfully " + commandLineExecutionResponse.getStdOutResponse());
		}
		else
			System.err.println(" execution failed " + commandLineExecutionResponse.getFailureReason() + ". Check " + outputFilePath);
		return commandLineExecutionResponse;
	}	
}
