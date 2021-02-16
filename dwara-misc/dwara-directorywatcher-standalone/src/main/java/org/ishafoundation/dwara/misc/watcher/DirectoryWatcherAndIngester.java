package org.ishafoundation.dwara.misc.watcher;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwara.misc.ingest.Categoryelement;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.ishafoundation.dwaraapi.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is responsible for watching for files copied in prasadcorp workspace and when completed moves the file to dwara's user specific artifactclass location and calls the ingest 
 */

public class DirectoryWatcherAndIngester extends DirectoryWatcher_Old{

	private static Logger logger = LoggerFactory.getLogger(DirectoryWatcherAndIngester.class);

	private static Path rootIngestLoc = null;
	private static String ingestEndpointUrl = null;

	private static Pattern categoryNamePrefixPattern = Pattern.compile("^([A-Z]{1,2})\\d+");

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>)event;
	}

	/**
	 * Creates a WatchService and registers the given directory
	 */
	DirectoryWatcherAndIngester(Path dir, Long waitTime) throws IOException {
		super(dir, waitTime);
	}

	/*
	 * verifyChecksumAndMoveAndIngest
	 */
	protected void executeOnCopyCompletion(WatchKey watchKey, Path artifactPath, Collection<File> files) throws Exception{
		String expectedMd5 = null;
		String actualMd5 = null;
		for (File file : files) {
			String fileName = file.getName();
			if(fileName.endsWith(".md5")) {
				expectedMd5 = StringUtils.substringBefore(FileUtils.readFileToString(file), "  ").trim().toUpperCase();
			}
			else if(fileName.endsWith(".mxf")) {
				updateStatus(artifactPath, Status.verifying);

				byte[] digest =  ChecksumUtil.getChecksum(file, Checksumtype.md5);
				actualMd5 = DatatypeConverter.printHexBinary(digest).toUpperCase();
			}
		}
		logger.info(artifactPath + " expectedMd5 " + expectedMd5 + " actualMd5 " + actualMd5);
		if(expectedMd5.equals(actualMd5)) {
			updateStatus(artifactPath, Status.verified);
			// Move the folder from prasad area to ingest user area
			Path destArtifactPath = moveFolderToIngestUserArea(watchKey, artifactPath);
			// now trigger ingest
			ingest(destArtifactPath);
		}else {
			logger.error(artifactPath + "MD5 expected != actual, Now what??? ");
			updateStatus(artifactPath, Status.md5_mismatch);
		}
	}

	private Path moveFolderToIngestUserArea(WatchKey watchKey, Path srcPath) throws Exception {
		Path destArtifactPath = null;
		try {
			if(watchKey!=null)
				watchKey.cancel();

			String artifactName = getArtifactName(srcPath);

			String artifactNamePrefix = null;
			Matcher m = categoryNamePrefixPattern.matcher(artifactName);  		
			if(m.find())
				artifactNamePrefix = m.group(1);

			Categoryelement category = null;

			try {
				category = Categoryelement.valueOf(artifactNamePrefix);
			}
			catch (Exception e) {
				String msg = "Not able to categorize " + artifactNamePrefix + ":" + artifactName;
				logger.error(msg);
				throw new Exception(msg);
			}

			String artifactClassFolderName = null;
			if(category.getCategory().equals("Public"))
				artifactClassFolderName = "video-digi-2020-pub";
			else if(category.getCategory().equals("Private1"))
				artifactClassFolderName = "video-digi-2020-priv1";
			else if(category.getCategory().equals("Private2"))
				artifactClassFolderName = "video-digi-2020-priv2";

			if(artifactClassFolderName == null) {
				String msg = "Not able to categorize " + artifactNamePrefix + ":" + artifactName;
				logger.error(msg);
				throw new Exception(msg);
			}
			destArtifactPath = Paths.get(rootIngestLoc.toString(), artifactClassFolderName, artifactName);
			final Path dest = Paths.get(destArtifactPath.toString(), "mxf");
			move(srcPath, dest);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			updateStatus(srcPath, Status.move_failed);
		}
		return destArtifactPath;
	}

	private void ingest(Path path) {
		if(path.getNameCount() > 7) { // Expecting path = /data/user/pgurumurthy/ingest/prasad-pub/prasad-artifact-1
			System.err.println("File Path with more than 7 elements is not supported. Expected something like /data/dwara/user/pgurumurthy/ingest/prasad-pub/prasad-artifact-1 but actual is " + path);
		}
		else {
			String artifactBasePath = path.getParent().toString();
			String artifactName = path.getFileName().toString();
			String artifactclass = path.getName(path.getNameCount() - 2).toString();


			String payload = "{\"artifactclass\":\"<<Artifactclass>>\",\"stagedFiles\":[{\"path\":\"<<Path>>\",\"name\":\"<<ArtifactName>>\"}]}";
			payload = payload.replace("<<Artifactclass>>", artifactclass);
			payload = payload.replace("<<Path>>", artifactBasePath);
			payload = payload.replace("<<ArtifactName>>", artifactName);
			logger.debug("payload "+ payload);

			String response = null;
			try {
				response = HttpClientUtil.postIt(ingestEndpointUrl, null, payload);
				updateStatus(path, Status.ingested);
				logger.info(path + " Ingest response from dwara : " + response);
			}catch (Exception e) {
				logger.error("Error on invoking ingest endpoint - " + e.getMessage());
			}
		}			
	}

	static void usage() {
		System.err.println("usage: java DirectoryWatcher [waitTimeInSecs] <dirToBeWatched(\"/data/prasad-staging\")> <rootIngestLocation(\"/data/dwara/user/prasadcorp/ingest\")> noOfThreadsForChecksumVerification <ingestEndpointUrl(\"http://pgurumurthy:ShivaShambho@172.18.1.213:8080/api/staged/ingest\")>");
		System.exit(-1);
	}

	public static void main(String[] args) throws Exception {
		// parse arguments
		if (args.length == 0 || args.length > 5)
			usage();

		int dirArg = 0;
		long waitTimes = 0L;
		if (args.length == 5) {
			waitTimes = Long.parseLong(args[0]) * 1000;
			dirArg++;
		}

		// register directory and process its events
		Path dir = Paths.get(args[dirArg]);

		rootIngestLoc = Paths.get(args[dirArg + 1]);

		int noOfThreads = Integer.parseInt(args[dirArg + 2]);
		executor = new ThreadPoolExecutor(noOfThreads, noOfThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

		ingestEndpointUrl = args[dirArg + 3]; // "http://pgurumurthy:ShivaShambho@172.18.1.213:8080/api/staged/ingest";

		new DirectoryWatcherAndIngester(dir, waitTimes).processEvents();
	}

}