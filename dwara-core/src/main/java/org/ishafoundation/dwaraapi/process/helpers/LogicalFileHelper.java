package org.ishafoundation.dwaraapi.process.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.springframework.stereotype.Component;

@Component
public class LogicalFileHelper {
	
	public Collection<LogicalFile> getFiles(String artifactPath, String[] extensions, boolean needSidecarFiles, String[] sidecarExtensions){
		List<LogicalFile> outputList = new ArrayList<LogicalFile>();
		
		String[] sourceExtensions = null;
		if(extensions != null) {
			List<String> sourceExtensionsList = new ArrayList<String>();
			for (int i = 0; i < extensions.length; i++) {
				sourceExtensionsList.add(extensions[i].toUpperCase());
				sourceExtensionsList.add(extensions[i].toLowerCase());
			}
			
			sourceExtensions = ArrayUtils.toStringArray(sourceExtensionsList.toArray());
		}
		Collection<File> sourceFilesList = FileUtils.listFiles(new File(artifactPath), sourceExtensions, true);
		
		if(needSidecarFiles) {
		
			HashMap<String, LogicalFile> name_To_File = new HashMap<String, LogicalFile>();
			
	
			for (Iterator<File> iterator = sourceFilesList.iterator(); iterator.hasNext();) {
				File file = (File) iterator.next();
				String filepathname = file.getAbsolutePath();
				
				String fullPath = FilenameUtils.getFullPath(filepathname);
				String baseName = FilenameUtils.getBaseName(filepathname);
				String keyName = fullPath + baseName;
	
				LogicalFile logicalFile = new LogicalFile(filepathname);
				name_To_File.put(keyName, logicalFile);
			}	
	
			List<String> sidecarExtensionsList = new ArrayList<String>();
			for (int i = 0; i < sidecarExtensions.length; i++) {
				sidecarExtensionsList.add(sidecarExtensions[i].toUpperCase());
				sidecarExtensionsList.add(sidecarExtensions[i].toLowerCase());
			}
			
			Collection<File> allSidecarFilesList = FileUtils.listFiles(new File(artifactPath),  ArrayUtils.toStringArray(sidecarExtensionsList.toArray()), true);
				
			for (Iterator<File> iterator = allSidecarFilesList.iterator(); iterator.hasNext();) {
				File file = (File) iterator.next();
				
				if(sourceFilesList.contains(file))
					continue;
				
				String filepathname = file.getAbsolutePath();

				String fullPath = FilenameUtils.getFullPath(filepathname);
				String baseName = FilenameUtils.getBaseName(filepathname);
				String keyName = fullPath + baseName;

				LogicalFile logicalFile = name_To_File.get(keyName);
				String extnName = FilenameUtils.getExtension(filepathname);
				if(logicalFile != null) {
					HashMap<String, File> existingSidecarMap = logicalFile.getSidecarFiles();
					if(existingSidecarMap != null) {
						existingSidecarMap.put(extnName, file); 
					}else {
						HashMap<String, File> newSidecarMap = new HashMap<String, File>();
						newSidecarMap.put(extnName, file);
						logicalFile.setSidecarFiles(newSidecarMap);
					}
				}else {
					//logger.trace("something wrong");
				}
			}			
			
			Set<String> name_To_FileKeyset = name_To_File.keySet();
			for (Iterator<String> iterator2 = name_To_FileKeyset.iterator(); iterator2.hasNext();) {
				String key = (String) iterator2.next();
				
				LogicalFile logicalFile = name_To_File.get(key);
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
