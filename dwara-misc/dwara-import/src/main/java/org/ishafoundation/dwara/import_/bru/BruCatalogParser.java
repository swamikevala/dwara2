package org.ishafoundation.dwara.import_.bru;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

public class BruCatalogParser {

	private String archiveIdRegEx = "archive ID = (.*)";
	private Pattern archiveIdRegExPattern = Pattern.compile(archiveIdRegEx);
	private String labelRegEx = "label = (.*)";
	private Pattern labelRegExPattern = Pattern.compile(labelRegEx);
	
	public BruResponseCatalog parseBruCatalog(File bruCatalogFile, Map<String, String> artifactToArtifactClassMapping) throws Exception {
		String bruArchiveId = null;
		String volumeId = null;
		List<BruFile> bruFileList = new ArrayList<>();
		Long fileCount = 0L;
		// This is starting volume block of the entire tape. Some time it starts with 0 and some starts with -1
		// e.g., -1 start === VL:c|0|1|136|-1|10751_21-Foot-Adiyogi_Face-And-Neck-Seperation-For-Packing_Helipad-Near-Chamundi-IYC_30-Aug-2014/ === PA4749L4_15-Oct-2016-16-36-45_15-Oct-2016-19-17-30
		// e.g., 0 start === VL:c|0|1|102|0|1Day-Sathsang_The-Decorum-London_13-March-10_Session1-Cam1/ === CA4065L4_02-Jun-2010-04-55-55_02-Jun-2010-07-28-39
		int startVolumeBlock = 0;  
		 
		LineIterator it = FileUtils.lineIterator(bruCatalogFile, "UTF-8");
		while (it.hasNext()) {
			String line = it.nextLine();
			if(line.startsWith("archive ID = ")) {
				Matcher archiveIdRegExMatcher = archiveIdRegExPattern.matcher(line);
				if(archiveIdRegExMatcher.matches()) {
					bruArchiveId = archiveIdRegExMatcher.group(1);
				}
			}
			if(line.startsWith("label = ")) {
				Matcher labelRegExMatcher = labelRegExPattern.matcher(line);
				if(labelRegExMatcher.matches()) {
					volumeId = labelRegExMatcher.group(1);
				}
			}			
			if (line.contains("VL:c")) {
				String[] arrValues = line.split("\\|");
				BruFile b = new BruFile();
				if (arrValues[0].equals("VL:c")) {
					String temp = arrValues[5]; //.replaceAll("\\P{Print}", "");
					if (temp.endsWith("/")) {
						temp = StringUtils.substring(temp, 0, -1);
					}
					if (temp.startsWith("./")) {
						temp = temp.replace("./", "");
					}
	
					
					if(fileCount == 0)
						startVolumeBlock = Integer.parseInt(arrValues[4]);
					
					b.startVolumeBlock = Long.parseLong(arrValues[4]) + (startVolumeBlock == -1 ? 1 : 0);
					b.size = Long.parseLong(arrValues[3]);
	
					if (!temp.contains("/")) {
						b.name = temp;
						b.isArtifact = true;
						b.isDirectory = true;
					} else {
						b.name = temp;
						b.isArtifact = false;
						// temp.substring(temp.lastIndexOf("/") + 1);
						
						// extra 4096 check to address entries like 
						// VL:c|385671168|1|4096|376631|Z7424_Class_IEO_Tamil-Day2-Desire_FCP7-And-FCPX/XMLs/FCP X/._IEO Tamil Day 5 - Acceptance II.fcpxml 
						// which is a folder. 
						// Check still doest not guarantee the classification of a file vs folder as there are folders like these too
						// VL:c|385671168|1|93265|376631|Z7424_Class_IEO_Tamil-Day2-Desire_FCP7-And-FCPX/XMLs/FCP X/Misc Videos - Yatra + Mystic.fcpxml
						if (!temp.substring(temp.lastIndexOf("/") + 1).contains(".") || b.size == 4096) { 
							b.isDirectory = true;
						}
					}
	
					b.category = artifactToArtifactClassMapping.get(temp);
					b.archiveBlock = arrValues[1];
					b.archiveId = "";
	
					bruFileList.add(b);
						
					fileCount++;
				}

			}
		}
		LineIterator.closeQuietly(it);
		
		BruResponseCatalog brc = new BruResponseCatalog();
		brc.setArchiveId(bruArchiveId);
		brc.setVolumeId(volumeId);
		brc.setBruFileList(bruFileList);
		
		return brc;
	}
}
