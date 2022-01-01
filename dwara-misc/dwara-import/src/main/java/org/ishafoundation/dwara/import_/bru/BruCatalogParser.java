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
	
	public BruResponseCatalog parseBruCatalog(File bruCatalogFile, Map<String, Object> artifactToArtifactClassMapping) throws Exception {
		String bruArchiveId = null;
		List<BruFile> bruFileList = new ArrayList<>();
		
		LineIterator it = FileUtils.lineIterator(bruCatalogFile, "UTF-8");
		while (it.hasNext()) {
			String line = it.nextLine();
			if(line.startsWith("archive ID = ")) {
				Matcher archiveIdRegExMatcher = archiveIdRegExPattern.matcher(line);
				if(archiveIdRegExMatcher.matches()) {
					bruArchiveId = archiveIdRegExMatcher.group(1);
				}
			}
			if (line.contains("VL:c")) {
	
				String[] arrValues = line.split("\\|");
				BruFile b = new BruFile();
				if (arrValues[0].equals("VL:c")) {
					String temp = arrValues[5].replaceAll("\\P{Print}", "");
					if (arrValues[5].endsWith("/")) {
						temp = StringUtils.substring(arrValues[5], 0, -1);
					}
					if (temp.startsWith("./")) {
						temp = temp.replace("./", "");
					}
	
					b.startVolumeBlock = Long.parseLong(arrValues[4]) + 1;
					b.size = Long.parseLong(arrValues[3]);
	
					if (!temp.contains("/")) {
						b.name = temp;
						b.isArtifact = true;
						b.isDirectory = true;
					} else {
						b.name = temp;
						b.isArtifact = false;
						temp.substring(temp.lastIndexOf("/") + 1);
						// extra 4096 check to address entire like 
						// VL:c|385671168|1|4096|376631|Z7424_Class_IEO_Tamil-Day2-Desire_FCP7-And-FCPX/XMLs/FCP X/._IEO Tamil Day 5 - Acceptance II.fcpxml 
						// which is a folder. 
						// Check still doest not guarantee the classification of a file vs folder as there are folders like these too
						// VL:c|385671168|1|93265|376631|Z7424_Class_IEO_Tamil-Day2-Desire_FCP7-And-FCPX/XMLs/FCP X/Misc Videos - Yatra + Mystic.fcpxml
						if (!temp.substring(temp.lastIndexOf("/") + 1).contains(".") || b.size == 4096) { 
							b.isDirectory = true;
						}
					}
	
					b.category = (String) artifactToArtifactClassMapping.get(temp);
					b.archiveBlock = arrValues[1];
					b.archiveId = "";
	
					bruFileList.add(b);
				}
			}
		}
		LineIterator.closeQuietly(it);
		
		BruResponseCatalog brc = new BruResponseCatalog();
		brc.setArchiveId(bruArchiveId);
		brc.setBruFileList(bruFileList);
		return brc;
	}
}
