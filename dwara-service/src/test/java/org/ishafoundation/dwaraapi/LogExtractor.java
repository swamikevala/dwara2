package org.ishafoundation.dwaraapi;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class LogExtractor {
	
	public void prettyPrint(String log, File outputPrettyprintLogFile) throws Exception{
		Map<String, List<String>> threadName_LogEntries_Map = new LinkedHashMap<String, List<String>>();
		Scanner scanner = new Scanner(log);
		Pattern threadRegExPattern = Pattern.compile("\\[(.*)\\]");
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();
			Matcher threadRegExMatcher = threadRegExPattern.matcher(line);
			if(threadRegExMatcher.find()) {
				String threadName = threadRegExMatcher.group(1);
				List<String> logEntries = threadName_LogEntries_Map.get(threadName);
				if(logEntries == null) {
					logEntries = new ArrayList<String>();
					logEntries.add("*");
					logEntries.add("*");
					logEntries.add("*");
					threadName_LogEntries_Map.put(threadName, logEntries);
				}
				logEntries.add(line);
			}
		}
		
		
		Set<String> threadNameSet = threadName_LogEntries_Map.keySet();
		for (String thread : threadNameSet) {
			List<String> logEntries = threadName_LogEntries_Map.get(thread);
			FileUtils.writeLines(outputPrettyprintLogFile, logEntries, true);
		}
		scanner.close();
	}
	
	public String extractLogBlock(String log, String startTs, String endTs){
		String neededLogBlock = null;
		String neededLogBlockTrimmedTillStart = StringUtils.substringAfter(log, startTs);
		if(StringUtils.isNotBlank(endTs))
			neededLogBlock = StringUtils.substringBeforeLast(neededLogBlockTrimmedTillStart, endTs);
		else
			neededLogBlock = neededLogBlockTrimmedTillStart;
		return neededLogBlock;
	}
	
	public static void main(String[] args) throws Exception {
		String log = FileUtils.readFileToString(new File(args[0]));
		String startTs = args[1];
		String endTs = args[2];
		String outputPrettyprintLogFileLocation = args[3];
		
		LogExtractor logExtractor = new LogExtractor();
		String neededLogBlock = logExtractor.extractLogBlock(log, startTs, endTs);
		
		File outputPrettyprintLogFile = new File(outputPrettyprintLogFileLocation);
		outputPrettyprintLogFile.delete();
		logExtractor.prettyPrint(neededLogBlock, outputPrettyprintLogFile);
	}

}
