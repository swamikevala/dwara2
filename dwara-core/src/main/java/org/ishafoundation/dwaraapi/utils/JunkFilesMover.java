package org.ishafoundation.dwaraapi.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class JunkFilesMover {

	Logger logger = LoggerFactory.getLogger(JunkFilesMover.class);
	
	@Autowired
	private Configuration config;

////	private String stagingSrcDirRoot = "C:\\data\\deleteThis";	
//	private String[] junkFilesFinderRegexPatternList = {"\\._[^\\/]*$","\\.DS_Store$","\\.fseventsd$","\\..Spotlight-V100$","\\.TemporaryItems$","\\.Trashes$","\\.VolumeIcon.icns$","\\.fcpcache"};
////	private String junkFilesDirRoot = "C:\\data\\movedJunkFilesFromIngestedMediaLibraryDirectory";
//	private String junkFilesDirName = "dwara-ignored";
	
	public void moveJunkFilesFromMediaLibrary(String mediaLibraryFolderLocation) {
		List<Pattern> excludedFileNamesRegexList = new ArrayList<Pattern>();
		String[] junkFilesFinderRegexPatternList = config.getJunkFilesFinderRegexPatternList();
		for (int i = 0; i < junkFilesFinderRegexPatternList.length; i++) {
			Pattern nthJunkFilesFinderRegexPattern = Pattern.compile(junkFilesFinderRegexPatternList[i]);
			excludedFileNamesRegexList.add(nthJunkFilesFinderRegexPattern);
		}
		
		// get the excludedFileNamesRegexList

//		excludedFileNamesRegexList.add("\\._[^\\/]*$");
//		excludedFileNamesRegexList.add("\\.DS_Store$");
//		excludedFileNamesRegexList.add("\\.fseventsd$");
//		excludedFileNamesRegexList.add("\\..Spotlight-V100$");
//		excludedFileNamesRegexList.add("\\.TemporaryItems$");
//		excludedFileNamesRegexList.add("\\.Trashes$");
//		excludedFileNamesRegexList.add("\\.VolumeIcon.icns$");
//		excludedFileNamesRegexList.add("\\.fcpcache");
	
			
		// move the files from the medialibrarydirectory to the junkfolder location.
		Collection<File> fileList = FileUtils.listFilesAndDirs(new File(mediaLibraryFolderLocation), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		for (Iterator<File> iterator = fileList.iterator(); iterator.hasNext();) {
			File nthFile = (File) iterator.next();
			String nthFileName = nthFile.getName();
			String nthFilePath = nthFile.getAbsolutePath();
			if(nthFilePath.contains(config.getJunkFilesStagedDirName()))
				continue;
			
			for (Iterator<Pattern> iterator2 = excludedFileNamesRegexList.iterator(); iterator2.hasNext();) {
				Pattern nthJunkFilesFinderRegexPattern = iterator2.next();
				Matcher m = nthJunkFilesFinderRegexPattern.matcher(nthFileName);
				if(m.matches()) {
					String destPath = nthFilePath.replace(mediaLibraryFolderLocation, mediaLibraryFolderLocation + File.separator + config.getJunkFilesStagedDirName());
					File destDir = new File(destPath);					
					try {
						if(nthFile.isFile())
							Files.createDirectories(Paths.get(FilenameUtils.getFullPathNoEndSeparator(destPath)));		
						else
							Files.createDirectories(Paths.get(destPath));

						Files.move(nthFile.toPath(), destDir.toPath(), StandardCopyOption.ATOMIC_MOVE);
					}catch (Exception e) {
						logger.error("Unable to move file " + nthFilePath + " to " + destPath + " as " + e.getMessage(), e);
					}
					break;
				}
			}
		}
	}
	
	public static void main(String[] args) {
		JunkFilesMover junkFilesMover = new JunkFilesMover();
		junkFilesMover.moveJunkFilesFromMediaLibrary("C:\\data\\user\\pgurumurthy\\ingest\\pub-audio\\14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A.mp3");
	}
}
