package org.ishafoundation.dwaraapi.staged.scan;


import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
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
	
	private String artifactSrcPathLocation = null;
	private String stagedFileName = null;
	private String junkFilesStagedDirName = null;
	private List<Pattern> excludedFileNamesRegexList = null;
	private Set<String> supportedExtns =  null;
	private boolean isMove = false;
	
	private int fileCount = 0;
	private long totalSize = 0;
	
	private Set<String> unresolvedSymLinks = new TreeSet<String>();
	private Set<String> symLinkLoops = new TreeSet<String>();
	private Set<String> unSupportedExtns = new TreeSet<String>();
	private Set<String> filePathNamesGt4096Chrs = new TreeSet<String>();
	private Set<String> fileNamesWithNonUnicodeChrs = new TreeSet<String>();
	

	StagedFileVisitor(String stagedFileName, String junkFilesStagedDirName, List<Pattern> excludedFileNamesRegexList, Set<String> supportedExtns) {
		this.stagedFileName = stagedFileName;
		this.junkFilesStagedDirName = junkFilesStagedDirName;
		this.excludedFileNamesRegexList = excludedFileNamesRegexList;
		this.supportedExtns = supportedExtns;
	}

	
	StagedFileVisitor(String artifactSrcPathLocation, String stagedFileName, String junkFilesStagedDirName, List<Pattern> excludedFileNamesRegexList, Set<String> supportedExtns, boolean isMove) {
		this.artifactSrcPathLocation = artifactSrcPathLocation;
		this.stagedFileName = stagedFileName;
		this.junkFilesStagedDirName = junkFilesStagedDirName;
		this.excludedFileNamesRegexList = excludedFileNamesRegexList;
		this.supportedExtns = supportedExtns;
		this.isMove = isMove;
	}

	public int getFileCount() {
		return fileCount;
	}

	public long getTotalSize() {
		return totalSize;
	}

	public Set<String> getUnresolvedSymLinks() {
		return unresolvedSymLinks;
	}

	public Set<String> getSymLinkLoops() {
		return symLinkLoops;
	}

	public Set<String> getUnSupportedExtns() {
		return unSupportedExtns;
	}

	public Set<String> getFilePathNamesGt4096Chrs() {
		return filePathNamesGt4096Chrs;
	}

	public Set<String> getFileNamesWithNonUnicodeChrs() {
		return fileNamesWithNonUnicodeChrs;
	}	


	@Override
	public FileVisitResult preVisitDirectory(Path dir,
			BasicFileAttributes attrs) {
		
		if(dir.getFileName().toString().equals(this.junkFilesStagedDirName)) {
			logger.trace("Skipped " + junkFilesStagedDirName);
			return SKIP_SUBTREE;
		}
		
		if(isJunk(dir)) {
			if(isMove) {
				move(dir);
			}
			else
				logger.trace("Skipped junk dir " + dir.toString());
			return SKIP_SUBTREE;
		}
		
		if(hasFileNameContainsNonUnicodeChrs(dir.getFileName().toString())) {
			fileNamesWithNonUnicodeChrs.add(dir.getFileName().toString());
		}
			
		return CONTINUE;
	}
	
	@Override
	public FileVisitResult visitFile(Path file,
			BasicFileAttributes attrs) {

		String filePathName = file.toString();
		
		if(isJunk(file)) {
			if(isMove) {
				move(file);
			}
			else
				logger.trace("Skipped junk file " + filePathName);
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
			
			logger.trace("SymLink " + filePathName);
			logger.trace("FileRealPath " + fileRealPath);
			if(fileRealPath == null) {
				logger.warn("Unresolved Sym Link " + filePathName);
				unresolvedSymLinks.add(filePathName);
				return CONTINUE;
			}
		}
		
//		if(fileName.length() > 100)
//			hasAnyFileNameGt100Chrs = true;

		String stagedFilePathName = stagedFileName + StringUtils.substringAfter(filePathName,stagedFileName);
		if(stagedFilePathName.length() > 4096) {
			logger.warn("FilePathname > 4096 " + stagedFilePathName);
			filePathNamesGt4096Chrs.add(filePathName);
			return CONTINUE;
		}
		
		fileCount++;
		totalSize = totalSize + FileUtils.sizeOf(file.toFile());

		String fileName = file.getFileName().toString();
		String fileExtn = FilenameUtils.getExtension(fileName);
		// validate against the supported list of extensions in the system we have
		if(!supportedExtns.contains(fileExtn.toLowerCase()))
			unSupportedExtns.add(fileExtn);
		
		if(hasFileNameContainsNonUnicodeChrs(fileName)) {
			fileNamesWithNonUnicodeChrs.add(fileName);
		}

		return CONTINUE;
	}


	@Override
	public FileVisitResult visitFileFailed(Path file,
			IOException exc) {
		
		String filePathName = file.toString();
		boolean isJunk = isJunk(file);
//		if (exc instanceof FileSystemLoopException) {
//			logger.warn("Cycle detected: " + file);
//			if(isJunk) {
//				if(isMove) {
//					move(file);
//				}
//				else
//					logger.trace("Skipped junk visit failed file " + filePathName);
//			}else {
//				symLinkLoops.add(filePathName);
//				logger.trace("Skipped visit failed file " + filePathName);
//			}
//		}
//		else {
//			if(isJunk) {
//				if(isMove) {
//					move(file);
//				}
//				else
//					logger.trace("Skipped junk visit failed file " + filePathName);
//			}
//			else
//				logger.trace("Skipped visit failed file " + filePathName);
//		}
		
		if (exc instanceof FileSystemLoopException) {
			logger.warn("Cycle detected: " + file);
			if(isJunk)
				logger.trace("Skipped junk visit failed file " + filePathName);
			else
				symLinkLoops.add(filePathName);
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
			}
		}
		return isJunk;
	}
	
	private FileVisitResult move(Path path){
		String destPath = path.toString().replace(artifactSrcPathLocation, artifactSrcPathLocation + File.separator + junkFilesStagedDirName);
		File destDir = new File(destPath);					
		try {
			if(path.toFile().isDirectory())
				Files.createDirectories(Paths.get(destPath));
			else
				Files.createDirectories(Paths.get(FilenameUtils.getFullPathNoEndSeparator(destPath)));	
			
			if(Files.isSymbolicLink(path)) {
				Files.move(path, destDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
//				Path target = Files.readSymbolicLink(path);
//				logger.trace(path + " symlink target - " + target);
//				Path newSymLink = Paths.get(FilenameUtils.getFullPathNoEndSeparator(destPath), path.getFileName().toString());
//				Files.createSymbolicLink(newSymLink, target);
//				logger.trace(newSymLink + " new symlink created " + target);
//				Files.delete(path);
//				logger.trace("old symlink deleted - " + path);
			}	
			else
				Files.move(path, destDir.toPath(), StandardCopyOption.ATOMIC_MOVE);
			logger.trace("Moved junk " + path.toString());
		}catch (Exception e) {
			logger.error("Unable to move " + path + " to " + destPath + " as " + e.getMessage(), e);
			//return TERMINATE; // TODO TERMINATE??? throw exception?
		}
		return CONTINUE;
	}
	

	private boolean hasFileNameContainsNonUnicodeChrs(String fileName) {
		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
		try {
			decoder.decode(ByteBuffer.wrap(fileName.getBytes()));		           
		} catch (CharacterCodingException ex) {		        
			return true;
		} 
		return false;
	}
}
