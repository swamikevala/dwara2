package org.ishafoundation.dwara.misc.ingest;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ishafoundation.dwara.misc.common.MoveUtil;
import org.ishafoundation.dwara.misc.common.Status;
import org.ishafoundation.dwaraapi.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ingester {

	private static Logger logger = LoggerFactory.getLogger(Ingester.class);
	
	private static Path rootIngestLoc = null;
	private static String ingestEndpointUrl = null;
	private static Pattern categoryNamePrefixPattern = Pattern.compile("^([A-Z]{1,2})\\d+");
	
	private static Path moveFolderToIngestUserArea(Path artifactPath) throws Exception {
		Path destArtifactPath = null;
		try {
			String artifactName = artifactPath.getFileName().toString();

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
			MoveUtil.move(artifactPath, dest);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			updateStatus(artifactPath, Status.move_failed);
			throw e;
		}
		return destArtifactPath;
	}

	private static void ingest(Path path) {
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

	private static void updateStatus(Path child, Status status) throws Exception {
		logger.info(String.format("%s %s", child, status.name()));
	}
	
	private static void usage() {
		System.err.println("usage: java Ingester "
				+ "<dirToBePolled(\"/data/prasad-staging/validated\")> "
				+ "validatedFolderPollingIntervalInSecs "
				+ "<rootIngestLocation(\"/data/dwara/user/prasadcorp/ingest\")> "
				+ "<ingestEndpointUrl(\"http://pgurumurthy:ShivaShambho@172.18.1.213:8080/api/staged/ingest\")>");
		
		System.err.println("args[0] - dirToBePolled - The directory where this service needs to look for files");
		System.err.println("args[1] - validatedFolderPollingIntervalInSecs - The polling interval to check if there are directories ready");
		System.err.println("args[2] - rootIngestLocation - Dwara's user specific ingest directory from where ingests are launched. Note Artifactclass gets appended dynamically to the path");
		System.err.println("args[3] - ingestEndpointUrl - Ingest api url");
		
		System.exit(-1);
	}
	
	public static void main(String[] args) {

		// parse arguments
		if (args.length != 4)
			usage();

		final Path dir = Paths.get(args[0]);
		int validatedFolderPollingIntervalInSecs = Integer.parseInt(args[1]);
		rootIngestLoc = Paths.get(args[2]);
		ingestEndpointUrl = args[3]; // "http://pgurumurthy:ShivaShambho@172.18.1.213:8080/api/staged/ingest";
		

		for(;;) {
			try {
				if(dir.toFile().exists()) {
					Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs)
								throws IOException
						{
							String dirPath = path.toString();
							if(!dirPath.equals(dir.toString())) {
								// Move the folder from prasad area to ingest user area
								Path destArtifactPath;
								try {
									destArtifactPath = moveFolderToIngestUserArea(path);
									// now trigger ingest
									ingest(destArtifactPath);
								} catch (Exception e) {
									logger.error("Unable to move " + path + ". Skipping ingesting it");
									return FileVisitResult.CONTINUE;
								}
							}
							return FileVisitResult.CONTINUE;
						}
					});
				}else {
					logger.warn(dir.toString() + " doesnt exist");
				}
				Thread.sleep(validatedFolderPollingIntervalInSecs * 1000);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	
	}
}
