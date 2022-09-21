package org.ishafoundation.poc.binary_reversal;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.poc.binary_reversal.Timecode.Type;

public class LogParser {
	
	
	
	public Map<Integer, String> parseLog(String logFilePathname, Type videoType) throws Exception {
		
		Map<Integer, String> frame_Timecode_Map = new HashMap<Integer, String>();
		
		File logFile = new File(logFilePathname);
		List<String> logLines = FileUtils.readLines(logFile);
		for (String nthLine : logLines) {
			String[] nthLineFields = nthLine.split(",");
			String type = nthLineFields[1];
			if(type.equals("TC")) {
				String event = nthLineFields[2];
				String startFrameAsString = nthLineFields[4];
				String timecodeValue = nthLineFields[5];
				String impactedFrameCntAsString = nthLineFields[6];

				int startFrame = Integer.parseInt(startFrameAsString);
				int totalNoOfImpactedFrames = Integer.parseInt(impactedFrameCntAsString);

				if(event.equals("INVALID")) {
					Timecode convertedTimecode = convertInvalidToValidTimeCode(String.format("%08d", Integer.parseInt(timecodeValue) - 40), videoType);
					for (int i = 0; i < totalNoOfImpactedFrames; i++) {
						String convertedTimecodeString = convertedTimecode.getCode();
						convertedTimecodeString = convertedTimecodeString.substring(0, 9) + (Integer.parseInt(convertedTimecodeString.substring(9, 11)) + 40);
						frame_Timecode_Map.put(startFrame + i, convertedTimecodeString);
						convertedTimecode.addFrame();
					}
				}
				else if(event.equals("FREEZE")) {
					for (int i = 0; i < totalNoOfImpactedFrames; i++) {
						frame_Timecode_Map.put(startFrame + i, timecodeValue);
					}
				}
				else if(event.equals("BREAK")) {
					for (int i = 0; i < totalNoOfImpactedFrames; i++) {
						frame_Timecode_Map.put(startFrame + i, timecodeValue);
					}
				}
				else
					throw new Exception("Unexpected event - " + event);
			}
				
		}
		
		
		return frame_Timecode_Map;
	}
	
	
	private static Timecode convertInvalidToValidTimeCode(String timecode, Type videoType) throws Exception{	
		
	    String timeCode1stN2ndDigit = timecode.substring(0, 2);
	    String timeCode3rdN4thDigit = timecode.substring(2, 4);
	    String timeCode5thN6thDigit = timecode.substring(4, 6);
	    String timeCode7thN8thDigit = timecode.substring(6, 8);
	    
	    String validTimeCode = timeCode1stN2ndDigit + ":" + timeCode3rdN4thDigit + ":" + timeCode5thN6thDigit + ":" + timeCode7thN8thDigit;
	    return new Timecode(validTimeCode, videoType);
	}
	
	public static void main(String[] args) throws Exception {
		LogParser lp = new LogParser();
		Map<Integer, String> frame_Timecode_Map = lp.parseLog(args[0], args[1].equals("PAL") ? Type.TYPE_VIDEO_PAL : Type.TYPE_VIDEO_NTSC);
		Set<Integer> keyset = frame_Timecode_Map.keySet();
		for (Integer nthFrame : keyset) {
			System.out.println(nthFrame + "-=-" + frame_Timecode_Map.get(nthFrame));
		}
		
	}

}
