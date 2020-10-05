package org.ishafoundation.dwaraapi.staged.scan;


import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.FileVisitResult.TERMINATE;

import java.io.IOException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StagedFileVisitor extends SimpleFileVisitor<Path> {
	
	private static final Logger logger = LoggerFactory.getLogger(StagedFileVisitor.class);
	
	private String stagedFileName = null;
	private String junkFilesStagedDirName = null;
	private List<Pattern> excludedFileNamesRegexList = null;
	private Set<String> supportedExtns =  null;

	private int fileCount = 0;
	private long totalSize = 0;
	private boolean hasUnresolvedSymLink = false;
	private boolean hasSymLinkLoop = false;
	private Set<String> unSupportedExtns = new TreeSet<String>();
	private boolean hasAnyFilePathNameGt3072Chrs = false;

	StagedFileVisitor(String stagedFileName, String junkFilesStagedDirName, List<Pattern> excludedFileNamesRegexList, Set<String> supportedExtns) {
		this.stagedFileName = stagedFileName;
		this.junkFilesStagedDirName = junkFilesStagedDirName;
		this.excludedFileNamesRegexList = excludedFileNamesRegexList;
		this.supportedExtns = supportedExtns;
	}

	public int getFileCount() {
		return fileCount;
	}

	public long getTotalSize() {
		return totalSize;
	}
	
	public boolean hasUnresolvedSymLink() {
		return hasUnresolvedSymLink;
	}

	public boolean hasSymLinkLoop() {
		return hasSymLinkLoop;
	}
	
	public Set<String> getUnSupportedExtns() {
		return unSupportedExtns;
	}

	public boolean hasAnyFilePathNameGt3072Chrs() {
		return hasAnyFilePathNameGt3072Chrs;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir,
			BasicFileAttributes attrs) {
		if(dir.getFileName().toString().equals(this.junkFilesStagedDirName)) {
			logger.trace("Skipped " + junkFilesStagedDirName);
			return SKIP_SUBTREE;
		}
		if(isJunk(dir)) {
			logger.trace("Skipped junk dir " + dir.toString());
			return SKIP_SUBTREE;
		}
		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file,
			BasicFileAttributes attrs) {

		if(isJunk(file)) {
			logger.trace("Skipped junk file " + file.toString());
			return CONTINUE;
		}
		Path fileRealPath = null;
		try {
			fileRealPath = file.toRealPath();
		} catch (IOException e) {
			// swallow it
		}

		// unresolved links
		//if(Files.isSymbolicLink(file) && file.getCanonicalPath() == null)
		if(Files.isSymbolicLink(file)) {
			logger.trace("SymLink " + file.toString());
			logger.trace("FileRealPath " + fileRealPath);
			if(fileRealPath == null) {
				logger.warn("Unresolved Sym Link " + file.toString());
				hasUnresolvedSymLink = true;
				return CONTINUE;
			}
		}
		
//		if(fileName.length() > 100)
//			hasAnyFileNameGt100Chrs = true;

		String filePathName = stagedFileName + StringUtils.substringAfter(file.toString(),stagedFileName);
		if(filePathName.length() > 3072) {
			logger.warn("FilePathname > 3072 " + filePathName);
			hasAnyFilePathNameGt3072Chrs = true;
			return CONTINUE;
		}
		
		fileCount++;
		totalSize = totalSize + FileUtils.sizeOf(file.toFile());

		String fileName = file.getFileName().toString();
		String fileExtn = FilenameUtils.getExtension(fileName);
		// validate against the supported list of extensions in the system we have
		if(!supportedExtns.contains(fileExtn.toLowerCase()))
			unSupportedExtns.add(fileExtn);

		return CONTINUE;
	}


	@Override
	public FileVisitResult visitFileFailed(Path file,
			IOException exc) {
		if (exc instanceof FileSystemLoopException) {
			logger.warn("Cycle detected: " + file);
			hasSymLinkLoop = true;
			return CONTINUE;// TODO: TERMINATE or CONTINUE???...
		}
		return CONTINUE;
	}

	private boolean isJunk(Path path) {
		boolean isJunk=false;
		for (Iterator<Pattern> iterator2 = excludedFileNamesRegexList.iterator(); iterator2.hasNext();) {
			// TODO : See if we can use PathMatcher than regex.Matcher
			Pattern nthJunkFilesFinderRegexPattern = iterator2.next();
			Matcher m = nthJunkFilesFinderRegexPattern.matcher(path.getFileName().toString());
			if(m.matches()) {
				isJunk=true;
//			} else {
//				Matcher filePathMatcher = nthJunkFilesFinderRegexPattern.matcher(nthFilePath);
//				if(filePathMatcher.find())
//					isJunk=true;
			}
		}
		return isJunk;
	}
}
