package org.ishafoundation.dwaraapi.utils;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePermissionsUtil {
	
	private static Logger logger = LoggerFactory.getLogger(FilePermissionsUtil.class);
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	
	public static boolean changePermissions(File file, String folderPermissionsRWXForm, String filePermissionsRWXForm) throws Exception {
		boolean success = false;
		
		if (file.isDirectory()) {
			success = changePermissions(file, folderPermissionsRWXForm);
			File[] files = file.listFiles();
			for (File nthFile : files) {
				if (!changePermissions(nthFile, folderPermissionsRWXForm, filePermissionsRWXForm)) {
					success = false;
				}
			}
		}
		else
			success = changePermissions(file, filePermissionsRWXForm);
		
		return success;		
	}

	private static boolean changePermissions(File file, String permissionsRWXForm) throws Exception {
		Path path = Paths.get(file.getAbsolutePath());

		PosixFileAttributeView posixView = Files.getFileAttributeView(path, PosixFileAttributeView.class);
		if (posixView == null) {
			logger.debug("POSIX attribute view is not supported.");
			return false;
		}

		PosixFileAttributes attribs = posixView.readAttributes();
		Set<PosixFilePermission> currentPermissions = attribs.permissions();
		String currentPermissionsIn_rwxForm = PosixFilePermissions.toString(currentPermissions);
		logger.debug("Existing permissions " + currentPermissionsIn_rwxForm);
		
		Set<PosixFilePermission> newPermissions = PosixFilePermissions.fromString(permissionsRWXForm);
		posixView.setPermissions(newPermissions);
		logger.debug(PosixFilePermissions.toString(newPermissions) + " permissions set successfully.");
		
		return true;
	}
	
	public static boolean changePermissionsAndOwnership(File file, String filePermissionsRWXForm, String ownerName, String groupName, boolean recursive) throws Exception {
		boolean success = changePermissionsAndOwnership(file, filePermissionsRWXForm, ownerName, groupName);
		if (file.isDirectory() && recursive) {
			File[] files = file.listFiles();
			for (File nthFile : files) {
				if (!changePermissionsAndOwnership(nthFile, filePermissionsRWXForm, ownerName, groupName, recursive)) {
					success = false;
				}
			}
		}
		return success;
	}
	
	private static boolean changePermissionsAndOwnership(File file, String filePermissionsRWXForm, String ownerName, String groupName) throws Exception {
		Path path = Paths.get(file.getAbsolutePath());

		PosixFileAttributeView posixView = Files.getFileAttributeView(path, PosixFileAttributeView.class);
		if (posixView == null) {
			logger.debug("POSIX attribute view is not supported.");
			return false;
		}
		
//		if (OS.indexOf("win") >= 0) {
//			// on windows
//			path.toFile().setReadable(true);
//			path.toFile().setWritable(true);
//		}
		// *** Ownership ***
		UserPrincipal owner = posixView.getOwner();
		logger.debug("Original owner of " + path + "  is " + owner.getName());

		FileSystem fs = FileSystems.getDefault();
		UserPrincipalLookupService upls = fs.getUserPrincipalLookupService();

		UserPrincipal newOwner = upls.lookupPrincipalByName(ownerName);
		posixView.setOwner(newOwner);
		
		GroupPrincipal newGroup = upls.lookupPrincipalByGroupName(groupName);
		posixView.setGroup(newGroup);
		
		UserPrincipal changedOwner = posixView.getOwner();
		logger.debug("New owner of " + path + "  is " + changedOwner.getName());

		// *** Permissions ***
		PosixFileAttributes attribs = posixView.readAttributes();
		Set<PosixFilePermission> currentPermissions = attribs.permissions();
		String currentPermissionsIn_rwxForm = PosixFilePermissions.toString(currentPermissions);
		logger.debug("Existing permissions " + currentPermissionsIn_rwxForm);
		
		Set<PosixFilePermission> newPermissions = PosixFilePermissions.fromString(filePermissionsRWXForm);
		posixView.setPermissions(newPermissions);
		logger.debug(PosixFilePermissions.toString(newPermissions) + " permissions set successfully.");
		
		return true;
	}
}
