package org.ishafoundation.digitization.preservation;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class CuesFileParser {
	
	private Pattern clusterPositionRegexPattern = Pattern.compile("timestamp=((([0-9]{2}):([0-9]{2}):([0-9]{2})).000000000) duration=- cluster_position=([0-9]*) relative_position=([0-9]*)");
	private Pattern lastLineRegexPattern = Pattern.compile("timestamp=((([0-9]{2}):([0-9]{2}):([0-9]{2})).([0-9]{9})) duration=- cluster_position=([0-9]*) relative_position=([0-9]*)");
	
	public String parseCuesResponse(List<String> response){
		StringBuffer filteredCueEntries = new StringBuffer();
		for (String nthStringLine : response) {
			Matcher clusterPositionRegexMatcher = clusterPositionRegexPattern.matcher(nthStringLine);
			if(clusterPositionRegexMatcher.matches()) {
				filteredCueEntries.append(clusterPositionRegexMatcher.group(2) + "," + clusterPositionRegexMatcher.group(6).trim() + "\n");
			}
		}

		Matcher clusterPositionRegexMatcher = lastLineRegexPattern.matcher(response.get(response.size()-1));
		if(clusterPositionRegexMatcher.matches()) {
			filteredCueEntries.append(clusterPositionRegexMatcher.group(2) + "," + clusterPositionRegexMatcher.group(7).trim() + "\n");
		}
		 
		return filteredCueEntries.toString();
	}

	
	public String getClusterPosition(String cuesFileEntries, String timestamp){
		String clusterPosition = null;
		Pattern timestampLineRegexPattern = Pattern.compile("timestamp=" + timestamp + ".000000000 duration=- cluster_position=([0-9]*) relative_position=([0-9]*)");
		
		
		Matcher timestampLineRegexMatcher = timestampLineRegexPattern.matcher(cuesFileEntries);
		if(timestampLineRegexMatcher.find()) {
			clusterPosition = timestampLineRegexMatcher.group(1);
		}

		return clusterPosition;
	}
	
//	public String parseCuesResponse(String response){
//		StringBuffer filteredCueEntries = new StringBuffer();
//		Matcher clusterPositionRegexMatcher = clusterPositionRegexPattern.matcher(response);
//		while(clusterPositionRegexMatcher.find()) {
//			System.out.println(clusterPositionRegexMatcher.group(1));
//			System.out.println("2 " + clusterPositionRegexMatcher.group(2));
//			System.out.println(clusterPositionRegexMatcher.group(3));
//			System.out.println(clusterPositionRegexMatcher.group(4));
//			System.out.println(clusterPositionRegexMatcher.group(5));
//			System.out.println("6 " + clusterPositionRegexMatcher.group(6));
//			filteredCueEntries.append(clusterPositionRegexMatcher.group(2) + "," + clusterPositionRegexMatcher.group(6) + "\n");
//		}
//
//		return filteredCueEntries.toString();
//	}
//	
//	public List<Cues> parseCuesResponse(List<String> response){
//		List<Cues> cuesList = new ArrayList<Cues>();
//		for (String nthStringLine : response) {
//			Matcher clusterPositionRegexMatcher = clusterPositionRegexPattern.matcher(nthStringLine);
//			if(clusterPositionRegexMatcher.matches()) {
//				Cues cues = new Cues();
//				cues.setTimestamp(clusterPositionRegexMatcher.group(1)); 
//				cues.setClusterPosition(Integer.parseInt(clusterPositionRegexMatcher.group(5).trim()));
//				cuesList.add(cues);
//			}
//		}
//		return cuesList;
//	}
	
	public static void main(String[] args) throws IOException {
		
		
		List<String> response = FileUtils.readLines(new File("C:\\Users\\prakash\\sample_prak.txt"));
		CuesFileParser cfp = new CuesFileParser();
		System.out.println(cfp.parseCuesResponse(response));
		System.out.println("(**********************)");
//		String strResponse = FileUtils.readFileToString(new File("C:\\Users\\prakash\\sample_prak.txt"));
//		System.out.println(cfp.parseCuesResponse(strResponse));
	}
}
