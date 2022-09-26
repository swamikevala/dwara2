package org.ishafoundation.dwaraapi.process.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogicalFileHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(LogicalFileHelper.class);
	
	public Collection<LogicalFile> getFiles(String artifactPath, String[] extensions, boolean needSidecarFiles, String[] sidecarExtensions){
		logger.trace("artifactPath " + artifactPath); 	// /data/ingested/V22204_Test2_MBT20481_01.MP4
														// /data/transcoded/public/V22204_Test2_MBT20481_01
														// /data/ingested/V22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12 
														// /data/transcoded/public/VL22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12
		logger.trace("extensions " + Arrays.toString(extensions));
		logger.trace("needSidecarFiles " + needSidecarFiles);
		logger.trace("sidecarExtensions " + Arrays.toString(sidecarExtensions));
		
		
		
		String[] sourceExtensions = null;
		if(extensions != null) {
			List<String> sourceExtensionsList = new ArrayList<String>();
			for (int i = 0; i < extensions.length; i++) {
				sourceExtensionsList.add(extensions[i]);
				sourceExtensionsList.add(extensions[i].toUpperCase());
				sourceExtensionsList.add(extensions[i].toLowerCase());
			}
			
			sourceExtensions = ArrayUtils.toStringArray(sourceExtensionsList.toArray());
		}
		
		Collection<File> sourceFilesList = new ArrayList<File>();
		
		if(StringUtils.isNotBlank(FilenameUtils.getExtension(artifactPath))) { // If artifact is a file...
			if(sourceExtensions == null || (sourceExtensions != null && Arrays.asList(sourceExtensions).contains(FilenameUtils.getExtension(artifactPath)))) { // s
				sourceFilesList.add(new File(artifactPath));
			}
		}
		else
			sourceFilesList = FileUtils.listFiles(new File(artifactPath), sourceExtensions, true);
		
		return getFiles(sourceFilesList, needSidecarFiles, sidecarExtensions);
	}

	public Collection<LogicalFile> getFiles(Collection<File> sourceFilesList, String[] extensions, boolean needSidecarFiles, String[] sidecarExtensions){
		List<String> sourceExtensionsList = new ArrayList<String>();
		Collection<File> filteredSourceFilesList = new ArrayList<File>();
		
		logger.trace("extensions " + Arrays.toString(extensions));
		logger.trace("needSidecarFiles " + needSidecarFiles);
		logger.trace("sidecarExtensions " + Arrays.toString(sidecarExtensions));
		if(extensions != null) {
			for (int i = 0; i < extensions.length; i++) {
				sourceExtensionsList.add(extensions[i]);
				sourceExtensionsList.add(extensions[i].toUpperCase());
				sourceExtensionsList.add(extensions[i].toLowerCase());
			}
			
			
			for (Iterator<File> iterator = sourceFilesList.iterator(); iterator.hasNext();) {
				File file = (File) iterator.next();
				String filepathname = file.getAbsolutePath();
				String fileExtn = FilenameUtils.getExtension(filepathname);
				
				if(sourceExtensionsList.contains(fileExtn)) {
					filteredSourceFilesList.add(file);
				}
			}
			
			sourceFilesList = filteredSourceFilesList;
		}
		
		return getFiles(sourceFilesList, needSidecarFiles, sidecarExtensions);
	}
		
	public Collection<LogicalFile> getFiles(Collection<File> sourceFilesList, boolean needSidecarFiles, String[] sidecarExtensions){
		List<LogicalFile> outputList = new ArrayList<LogicalFile>();
		if(needSidecarFiles) {		
			List<String> sidecarExtensionsList = new ArrayList<String>();
			for (int i = 0; i < sidecarExtensions.length; i++) {
				sidecarExtensionsList.add(sidecarExtensions[i].toUpperCase());
				sidecarExtensionsList.add(sidecarExtensions[i].toLowerCase());
			}
			
			for (Iterator<File> iterator = sourceFilesList.iterator(); iterator.hasNext();) {
				File file = (File) iterator.next();
				String filepathname = file.getAbsolutePath();
				
				String fullPath = FilenameUtils.getFullPath(filepathname);
				String baseName = FilenameUtils.getBaseName(filepathname);
	
				LogicalFile logicalFile = new LogicalFile(filepathname);
				
				for (String sidecarExtension : sidecarExtensionsList) {
					String sidecarFilePathname = fullPath + baseName + "." + sidecarExtension;
					File sidecarFile = new File(sidecarFilePathname);
					if(sidecarFile.exists()) {
						HashMap<String, File> existingSidecarMap = logicalFile.getSidecarFiles();
						if(existingSidecarMap != null) {
							existingSidecarMap.put(sidecarExtension, sidecarFile); 
						}else {
							HashMap<String, File> newSidecarMap = new HashMap<String, File>();
							newSidecarMap.put(sidecarExtension, sidecarFile);
							logicalFile.setSidecarFiles(newSidecarMap);
						}
					}
				}
				outputList.add(logicalFile);
			}
		} else {
			for (Iterator<File> iterator = sourceFilesList.iterator(); iterator.hasNext();) {
				File file = (File) iterator.next();
				String filepathname = file.getAbsolutePath();
				outputList.add(new LogicalFile(filepathname));
			}
		}
		return outputList;
	}
	
	public static void main(String[] args) {
		LogicalFileHelper fh = new LogicalFileHelper();
		
		String[] sourceFileExtensions = {"mp4"};
		String[] sidecarExtensions = {"jpg","out"};
		
		
		Collection<File> sourceFilesList = new ArrayList<File>();
		sourceFilesList.add(new File("C:\\data\\transcoded\\64450_Sadhguru-Shambho_IYC_18-Nov-1980_Cam1_Sony\\ABC.MP4"));
		sourceFilesList.add(new File("C:\\data\\transcoded\\64450_Sadhguru-Shambho_IYC_18-Nov-1980_Cam1_Sony\\xyz.xml"));
		
		Collection<LogicalFile> logicalFileList2 = fh.getFiles(sourceFilesList, sourceFileExtensions, false, null);
		for (Iterator<LogicalFile> iterator = logicalFileList2.iterator(); iterator.hasNext();) {
			LogicalFile logicalFile = (LogicalFile) iterator.next();
			System.out.println("sourceFile : " + logicalFile.getAbsolutePath());
		}
			
		
		Collection<LogicalFile> //logicalFileList = fh.getFiles("C:\\data\\transcoded\\64450_Sadhguru-Shambho_IYC_18-Nov-1980_Cam1_Sony", null, false, sidecarExtensions); 
		//logicalFileList = fh.getFiles("C:\\data\\transcoded\\64450_Sadhguru-Shambho_IYC_18-Nov-1980_Cam1_Sony", sourceFileExtensions, false, sidecarExtensions);
		//logicalFileList = fh.getFiles("C:\\data\\transcoded\\64450_Sadhguru-Shambho_IYC_18-Nov-1980_Cam1_Sony", sourceFileExtensions, true, sidecarExtensions);
		logicalFileList = fh.getFiles("C:\\data\\transcoded\\64450_Sadhguru-Shambho_IYC_18-Nov-1980_Cam1_Sony", sourceFileExtensions, true, null);

		
		
		for (Iterator<LogicalFile> iterator = logicalFileList.iterator(); iterator.hasNext();) {
			LogicalFile logicalFile = (LogicalFile) iterator.next();
			System.out.println("sourceFile : " + logicalFile.getAbsolutePath());
			
			System.out.println("jpgFile Standalone : " + logicalFile.getSidecarFile("jpg"));
			
			HashMap<String, File> sidecarMap = logicalFile.getSidecarFiles();
			if(sidecarMap != null) {
				Set<String> sidecarKeyset = sidecarMap.keySet();
				int cnt = 1;
				for (Iterator<String> iterator2 = sidecarKeyset.iterator(); iterator2.hasNext();) {
					String key = (String) iterator2.next();
					
					File sidecarFile = sidecarMap.get(key);
					System.out.println("sidecarFile " + cnt + " : " + sidecarFile.getAbsolutePath());
					cnt +=1;
				}
			}
		}
		
	}

}
