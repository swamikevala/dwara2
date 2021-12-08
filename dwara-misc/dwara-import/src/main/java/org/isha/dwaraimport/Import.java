package org.isha.dwaraimport;

import java.io.File;
import java.io.FileFilter;

public class Import {
    public static void main (String[] args) throws Exception {
        String bruFileLocation = args[0];
        String artifactToArtifactClassMappingJsonFolderPath = args[1];
        String artifactsToBeImportIgnoredJsonFolderPathLocation = args[2];
        String destinationXMLLocation = args[3];

        DwaraImport dwaraImport = new DwaraImport();
        
        java.io.File file = new java.io.File(bruFileLocation);
		if(file.listFiles(new FileFilter() {
		    @Override
		    public boolean accept(File nthFile) {
		        return nthFile.isDirectory() ? false : true;
		    }
		}).length == 0)
			throw new Exception("No catalog available for xml generation in " + bruFileLocation);

		dwaraImport.apacheGetXMLData(bruFileLocation, artifactsToBeImportIgnoredJsonFolderPathLocation, artifactToArtifactClassMappingJsonFolderPath, destinationXMLLocation);
    }

}
