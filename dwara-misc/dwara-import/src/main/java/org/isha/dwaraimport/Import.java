package org.isha.dwaraimport;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.FileUtils;

public class Import {
    public static void main (String[] args) throws Exception {
        String bruFileLocation = args[0];
        String artifactToArtifactClassMappingJsonFolderPath = args[1];
        String destinationXMLLocation = args[2];

        DwaraImport dwaraImport = new DwaraImport();
        
        java.io.File file = new java.io.File(bruFileLocation);
		if(file.listFiles(new FileFilter() {
		    @Override
		    public boolean accept(File nthFile) {
		        return nthFile.isDirectory() ? false : true;
		    }
		}).length == 0)
			throw new Exception("No catalog available for xml generation in " + bruFileLocation);

		java.io.File artifactToArtifactClassMappingJsonFile = new java.io.File(artifactToArtifactClassMappingJsonFolderPath);
		if(!artifactToArtifactClassMappingJsonFile.exists())
			throw new Exception("Artifactclass mapping file missing - " + artifactToArtifactClassMappingJsonFolderPath);
		
		java.io.File destination = new java.io.File(destinationXMLLocation);
		if(!destination.exists())
			FileUtils.forceMkdir(destination);
		
		java.io.File destinationFailed = new java.io.File(destinationXMLLocation + "-failed");
		if(!destinationFailed.exists())
			FileUtils.forceMkdir(destinationFailed);
		
		dwaraImport.apacheGetXMLData(bruFileLocation, artifactToArtifactClassMappingJsonFolderPath, destinationXMLLocation);
    }

}
