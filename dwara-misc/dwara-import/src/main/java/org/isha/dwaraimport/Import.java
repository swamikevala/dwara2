package org.isha.dwaraimport;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

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
		if(!destination.exists()) {
			FileUtils.forceMkdir(destination);
			changePermissions(destination, "rwxrwxrwx");
		}
		
		java.io.File destinationFailed = new java.io.File(destinationXMLLocation + "-failed");
		if(!destinationFailed.exists()) {
			FileUtils.forceMkdir(destinationFailed);
			changePermissions(destination, "rwxrwxrwx");
		}
		dwaraImport.apacheGetXMLData(bruFileLocation, artifactToArtifactClassMappingJsonFolderPath, destinationXMLLocation);
    }
    
	private static boolean changePermissions(File file, String permissionsRWXForm) throws Exception {
		Path path = Paths.get(file.getAbsolutePath());

		PosixFileAttributeView posixView = Files.getFileAttributeView(path, PosixFileAttributeView.class);
		if (posixView == null) {
			throw new Exception("POSIX attribute view is not supported. Unable to set permissions to " + file.getAbsolutePath());
		}

		PosixFileAttributes attribs = posixView.readAttributes();
		Set<PosixFilePermission> currentPermissions = attribs.permissions();
		String currentPermissionsIn_rwxForm = PosixFilePermissions.toString(currentPermissions);
		System.out.println("Existing permissions " + currentPermissionsIn_rwxForm);
		
		Set<PosixFilePermission> newPermissions = PosixFilePermissions.fromString(permissionsRWXForm);
		posixView.setPermissions(newPermissions);
		System.out.println(PosixFilePermissions.toString(newPermissions) + " permissions set successfully.");
		
		return true;
	}

}
