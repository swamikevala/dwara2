package org.ishafoundation.dwaraapi.process.helpers;


import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FiletypePathnameReqexVisitor extends SimpleFileVisitor<Path> {
	
	private static final Logger logger = LoggerFactory.getLogger(FiletypePathnameReqexVisitor.class);

	String inputArtifactPath = null;
	Pattern pathnameRegexPattern = null;
	private Collection<File> matchedFiles = new TreeSet<File>();
	private Set<String> extns = new TreeSet<String>();
	
	public FiletypePathnameReqexVisitor(String inputArtifactPath, String pathnameRegex) {
		this.inputArtifactPath = inputArtifactPath;
		logger.trace("inputArtifactPath - " + inputArtifactPath);
		this.pathnameRegexPattern = Pattern.compile(pathnameRegex);
	}

	/*
	 * Last minute requirement change - The pathname regex need to filter just specific files and not folders mentioned in the reqex pattern...
	 * 
	@Override
	public FileVisitResult preVisitDirectory(Path dir,
			BasicFileAttributes attrs) {
		
		Matcher pathnameRegexMatcher = pathnameRegexPattern.matcher(dir.toString());
		
		if(pathnameRegexMatcher.matches()) {
			paths.add(dir.toString());
			return SKIP_SUBTREE;
		}
		return CONTINUE;
	}
	*/

	@Override
	public FileVisitResult visitFile(Path file,
			BasicFileAttributes attrs) {
		logger.trace("visited file - " + file.toString());
		
		Matcher pathnameRegexMatcher = pathnameRegexPattern.matcher(file.toString().replace(inputArtifactPath + File.separator, "")); // only in relative to the artifact path 
		
		if(pathnameRegexMatcher.matches()) {
			logger.trace("matches regex - " + pathnameRegexPattern);
			matchedFiles.add(file.toFile());
			extns.add(FilenameUtils.getExtension(file.getFileName().toString()));
		}
		return CONTINUE;
	}
	
	public Collection<File> getMatchedFiles() {
		return matchedFiles;
	}

	public void setMatchedFiles(Collection<File> matchedFiles) {
		this.matchedFiles = matchedFiles;
	}

	public Set<String> getExtns() {
		return extns;
	}

	public void setExtns(Set<String> extns) {
		this.extns = extns;
	}

	public static void main(String[] args) {
		String inputArtifactPath = args[0];
		inputArtifactPath = "C:\\data\\ingested\\P22197_prasad-artifact-1";
		
		//String pathnameRegex = ".*\\\\Output";
		//String pathnameRegex = ".*\\\\Video\\\\Output";
		//String pathnameRegex = ".*\\\\Out.*";
		//String pathnameRegex = ".*\\\\Output\\\\[^\\\\]*.mov$";
		//String pathnameRegex = "\\\\.mxf$";
		String pathnameRegex = ".*/(Video Output/|Output_)[^/]+\\.(mov|mp4)$";
		FiletypePathnameReqexVisitor filetypePathnameReqexVisitor = new FiletypePathnameReqexVisitor(inputArtifactPath, pathnameRegex);
		try {
			Files.walkFileTree(Paths.get(inputArtifactPath), filetypePathnameReqexVisitor);
		} catch (IOException e) {
			// swallow for now
		}

		if(filetypePathnameReqexVisitor != null) {
			System.out.println(filetypePathnameReqexVisitor.getMatchedFiles());
			System.out.println(filetypePathnameReqexVisitor.getExtns());
		}
	}
}
