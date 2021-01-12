package org.ishafoundation.dwaraapi.staged.ingest;


import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PathnameReqexVisitor extends SimpleFileVisitor<Path> {
	
	private static final Logger logger = LoggerFactory.getLogger(PathnameReqexVisitor.class);

	private String pathPrefix = null;
	private Pattern pathnameRegexPattern = null;
	private String junkFilesStagedDirName = null;
	
	private Collection<java.io.File> fileList = new ArrayList<java.io.File>();
		
	public PathnameReqexVisitor(String pathPrefix, String pathnameRegex, String junkFilesStagedDirName) {
		this.pathPrefix = pathPrefix;
		pathnameRegexPattern = Pattern.compile(pathnameRegex);
		this.junkFilesStagedDirName = junkFilesStagedDirName;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir,
			BasicFileAttributes attrs) {

		String dirPathname = dir.toString().replace(pathPrefix + java.io.File.separator, "");
		Matcher pathnameRegexMatcher = pathnameRegexPattern.matcher(FilenameUtils.separatorsToUnix(dirPathname));
		
		if(!pathnameRegexMatcher.matches() || dir.getFileName().toString().equals(junkFilesStagedDirName)) {
			return SKIP_SUBTREE;
		}
		else
			fileList.add(dir.toFile());

		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file,
			BasicFileAttributes attrs) {
		logger.trace("visited file - " + file.toString());
		Matcher pathnameRegexMatcher = pathnameRegexPattern.matcher(FilenameUtils.separatorsToUnix(file.toString().replace(pathPrefix + java.io.File.separator, "")));
		
		if(pathnameRegexMatcher.matches()) {
			logger.trace("matches regex - " + pathnameRegexPattern);
			fileList.add(file.toFile());
		}
		return CONTINUE;
	}

	public Collection<java.io.File> getFileList() {
		return fileList;
	}

	public void setFileList(Collection<java.io.File> fileList) {
		this.fileList = fileList;
	}

	public static void main(String[] args) {
		String inputArtifactPath = "C:\\data\\staged\\edited_stuff_example";
		
		//String pathnameRegex = ".*\\\\Output";
		//String pathnameRegex = ".*\\\\Video\\\\Output";
		//String pathnameRegex = ".*\\\\Out.*";
		//String pathnameRegex = ".*\\\\Output\\\\[^\\\\]*.mov$";
//		String pathnameRegex = "\\\\.mxf$";
		String pathnameRegex = "^([^\\/]+\\/?){1,2}$|^[^\\/]+\\/Output[s]?\\/.+\\.mov$";
		PathnameReqexVisitor pathnameReqexVisitor = new PathnameReqexVisitor("C:\\data\\staged", pathnameRegex, ".junk");
		try {
			Files.walkFileTree(Paths.get(inputArtifactPath), pathnameReqexVisitor);
		} catch (IOException e) {
			// swallow for now
		}

		if(pathnameReqexVisitor != null) {
			System.out.println(pathnameReqexVisitor.getFileList());
		}
	}
}
