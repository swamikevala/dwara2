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

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class JunkFilesDealer {

	Logger logger = LoggerFactory.getLogger(JunkFilesDealer.class);
	
	@Autowired
	private Configuration config;

	private List<Pattern> excludedFileNamesRegexList = new ArrayList<Pattern>();
	
	@PostConstruct
	public void getExcludedFileNamesRegexList() {
		String[] junkFilesFinderRegexPatternList = config.getJunkFilesFinderRegexPatternList();
		for (int i = 0; i < junkFilesFinderRegexPatternList.length; i++) {
			Pattern nthJunkFilesFinderRegexPattern = Pattern.compile(junkFilesFinderRegexPatternList[i]);
			excludedFileNamesRegexList.add(nthJunkFilesFinderRegexPattern);
		}
	}
	
	public void moveJunkFiles(String artifactSrcPathLocation) {
		iterateOnFiles(artifactSrcPathLocation, "move");
	}

	public Collection<File> getJunkFilesExcludedFileList(String artifactSrcPathLocation) {
		return iterateOnFiles(artifactSrcPathLocation, "excludeJunk");
	}

	private Collection<File> iterateOnFiles(String artifactSrcPathLocation, String action) {
		Collection<File> junkFilesExcludedFileList = new java.util.LinkedList<File>();
		Collection<File> fileList = FileUtils.listFilesAndDirs(new File(artifactSrcPathLocation), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
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
					if(action.equals("move")) { // move the files from the artifact directory to the junkfolder location.
						String destPath = nthFilePath.replace(artifactSrcPathLocation, artifactSrcPathLocation + File.separator + config.getJunkFilesStagedDirName());
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

				} else {
					if(nthFile.isFile())
						junkFilesExcludedFileList.add(nthFile);
				}
			}
		}
		return junkFilesExcludedFileList;
	}

	public static void main(String[] args) {
		JunkFilesDealer junkFilesMover = new JunkFilesDealer();
		junkFilesMover.moveJunkFiles("C:\\data\\user\\pgurumurthy\\ingest\\pub-audio\\14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A.mp3");
	}
}
