package org.ishafoundation.dwaraapi.process.helpers;


import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FiletypePathnameReqexVisitor extends SimpleFileVisitor<Path> {
	
	private static final Logger logger = LoggerFactory.getLogger(FiletypePathnameReqexVisitor.class);

	Pattern pathnameRegexPattern = null;
	private Set<String> paths = new TreeSet<String>();
	private Set<String> extns = new TreeSet<String>();
	
	//FiletypePathnameReqexVisitor(Pattern pathnameRegexPatter) {
	public FiletypePathnameReqexVisitor(String pathnameRegex) {
		pathnameRegexPattern = Pattern.compile(pathnameRegex);
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
		Matcher pathnameRegexMatcher = pathnameRegexPattern.matcher(file.toString());
		
		if(pathnameRegexMatcher.matches()) {
			logger.trace("matches regex - " + pathnameRegexPattern);
			paths.add(file.getParent().toString());
			extns.add(FilenameUtils.getExtension(file.getFileName().toString()));
		}
		return CONTINUE;
	}
	
	public Set<String> getPaths() {
		return paths;
	}

	public void setPaths(Set<String> paths) {
		this.paths = paths;
	}

	
	public Set<String> getExtns() {
		return extns;
	}

	public void setExtns(Set<String> extns) {
		this.extns = extns;
	}

	public static void main(String[] args) {
		String inputArtifactPath = args[0];
		inputArtifactPath = "C:\\data\\staged\\P22197_prasad-artifact-1_1608311147396";
		
		//String pathnameRegex = ".*\\\\Output";
		//String pathnameRegex = ".*\\\\Video\\\\Output";
		//String pathnameRegex = ".*\\\\Out.*";
		//String pathnameRegex = ".*\\\\Output\\\\[^\\\\]*.mov$";
		String pathnameRegex = "\\\\.mxf$";
		FiletypePathnameReqexVisitor filetypePathnameReqexVisitor = new FiletypePathnameReqexVisitor(pathnameRegex);
		try {
			Files.walkFileTree(Paths.get(inputArtifactPath), filetypePathnameReqexVisitor);
		} catch (IOException e) {
			// swallow for now
		}

		if(filetypePathnameReqexVisitor != null) {
			System.out.println(filetypePathnameReqexVisitor.getPaths());
			System.out.println(filetypePathnameReqexVisitor.getExtns());
		}
	}
}
