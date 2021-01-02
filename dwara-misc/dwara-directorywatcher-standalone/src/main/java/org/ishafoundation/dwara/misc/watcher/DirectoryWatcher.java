package org.ishafoundation.dwara.misc.watcher;


import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuterImpl;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.ishafoundation.dwaraapi.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is responsible for watching for files copied in prasadcorp workspace and when completed moves the file to dwara's user specific artifactclass location and calls the ingest 
 */

public class DirectoryWatcher {

	private static Logger logger = LoggerFactory.getLogger(DirectoryWatcher.class);
	
	private final WatchService watchService;
	private final Path watchedDir;
	private final Map<WatchKey,Path> keys;
	private final Map<Path, Long> expirationTimes = new HashMap<Path, Long>();
	private Long newFileWait = 10000L;
	private static Path rootIngestLoc = null;
	private static Executor executor = null;
	private static String ingestEndpointUrl = null;

	private static Pattern categoryNamePrefixPattern = Pattern.compile("^([A-Z]{1,2})\\d+");
	
//	{"A","B","D","E","F","M","P","Q","R","AD","AG","AH","AK","AN","AZ","BD"}
//	{"W","X","Y"}
//	{"C","G","H","I","J","K","L","N","S","T","U","V","AA","AB","AC","AE","AF","AI","AJ","AL","AM","AR","AS","AW","AX","AY","BB","BC","BE","BG","BJ","TV","Z"}

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>)event;
	}

	/**
	 * Creates a WatchService and registers the given directory
	 */
	DirectoryWatcher(Path dir, Long waitTime) throws IOException {
		this.watchService = FileSystems.getDefault().newWatchService();
		this.watchedDir = dir;
		this.newFileWait = waitTime;
		this.keys = new HashMap<WatchKey,Path>();

		logger.info(String.format("Scanning %s ...\n", dir));
		registerAll(dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 */
	private void registerAll(final Path start) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
					throws IOException
			{
				if(!dir.toString().equals(watchedDir.toString())) {
					expirationTimes.put(dir, System.currentTimeMillis()+newFileWait);
				}
				register(dir);
				
				if(!dir.toString().equals(watchedDir.toString())) {// if there were folders already copied to the watch folder trigger a modify event so that we handleexpiredtimes 
					CommandLineExecuterImpl clei = new CommandLineExecuterImpl();
					try {
						clei.executeCommand("chmod -R 777 " + dir.toString(), false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void register(Path dir) throws IOException {
		WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		
		if (logger.isTraceEnabled()) {
			Path prev = keys.get(key);
			if (prev == null) {
				logger.info(String.format("register: %s\n", dir));
			} else {
				if (!dir.equals(prev)) {
					logger.info(String.format("update: %s -> %s\n", prev, dir));
				}
			}
		}
		keys.put(key, dir);
	}

	/**
	 * Process all events for keys queued to the watchService
	 */
	void processEvents() throws Exception{
		for (;;) { //infinite loop

			// wait for key to be signalled
			WatchKey key;
			try {
				key = watchService.take();
			} catch (InterruptedException x) {
				return;
			}

			for(;;) { // infinite loop for timed polling to find if any pending events are not and check on expiry...
				long currentTime = System.currentTimeMillis();

				if(key != null) { // if events are still pending...

					Path dir = keys.get(key);
					if (dir == null) {
						logger.error("WatchKey not recognized!!");
						continue;
					}

					for (WatchEvent<?> event: key.pollEvents()) {
						Kind<?> kind = event.kind();

						if (kind == OVERFLOW) {
							continue;
						}

						// Context for directory entry event is the file name of entry
						WatchEvent<Path> ev = cast(event);
						Path name = ev.context();
						Path child = dir.resolve(name);

						// print out event
						if (logger.isTraceEnabled())
							logger.trace(String.format("%s: %s\n", event.kind().name(), child));

						// if directory is created, and watching recursively, then
						// register it and its sub-directories
						if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
							if (kind == ENTRY_CREATE) {
								try {
									updateStatus(child, Status.copying);
	
									if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
										registerAll(child);
									}
	
								} catch (IOException x) {
									// ignore to keep sample readbale
								}
							}

							// Update modified time
							BasicFileAttributes attrs = null;
							try {
								attrs = Files.readAttributes(child, BasicFileAttributes.class, NOFOLLOW_LINKS);
							} catch (IOException e) {
								continue; // Happens when copy complete, but permissions set by stagedservice triggered this event but by then the stagedservice thread moved this file to /data/staged...
							}
							FileTime lastModified = attrs.lastModifiedTime();
							if (logger.isTraceEnabled())
								System.out.println(child + " lastModified " + lastModified);
							expirationTimes.put(child, lastModified.toMillis()+newFileWait);
						}
					}

					// reset key and remove from set if directory no longer accessible
					boolean valid = key.reset();
					if (!valid) {
						if (logger.isTraceEnabled()) {
							logger.trace(key + " not valid");
							logger.trace("Will be removing " + keys.get(key) + " from key list");
						}
						keys.remove(key);

						// all directories are inaccessible
						if (keys.isEmpty()) {
							break;
						}
					}
				}
				else {
					if (logger.isTraceEnabled())
						logger.trace("No pending events");
				}

				handleExpiredWaitTimes(key, currentTime);

				// If there are no files left stop polling and block on .take()
				if(expirationTimes.isEmpty())
					break;

				long minExpiration = Collections.min(expirationTimes.values());
				long timeout = minExpiration - currentTime;
				if (logger.isTraceEnabled())
					logger.trace("timeout: "+timeout);
				key = watchService.poll(timeout, TimeUnit.MILLISECONDS);
			}
		}
	}

	private Path getArtifactPath(Path child){
		String artifactName = getArtifactName(child);
		if (logger.isTraceEnabled())
			logger.trace("artifactName " + artifactName);
		return Paths.get(watchedDir.toString(), artifactName); 
	}

	private String getArtifactName(Path child){
		String filePathMinusWatchDirPrefix = child.toString().replace(watchedDir.toString() + File.separator, "");
		String artifactName = StringUtils.substringBefore(filePathMinusWatchDirPrefix, File.separator);
		return artifactName;
	}
	
	private void handleExpiredWaitTimes(WatchKey watchKey, Long currentTime) throws Exception {
		// Start import for files for which the expirationtime has passed
		Iterator<Entry<Path, Long>> it = expirationTimes.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<Path, Long> item = (Entry<Path, Long>) it.next();
			Long expiryTime = (Long) item.getValue();
			Path filePath = item.getKey();
			
			if(expiryTime <= currentTime) {
				if(filePath.getFileName().toString().endsWith(".mxf"))
					verifyChecksumAndMoveToOpsArea(watchKey, filePath);
				it.remove();
			}
		}
	}

    private void verifyChecksumAndMoveToOpsArea(WatchKey watchKey, Path path) throws Exception{
		VerifyChecksumAndMoveToOpsAreaTask checksumTask = new VerifyChecksumAndMoveToOpsAreaTask();//applicationContext.getBean(TapeTask.class); 
		checksumTask.setWatchKey(watchKey);
		checksumTask.setMxfFilePath(path);
		executor.execute(checksumTask);
    }
    
	public class VerifyChecksumAndMoveToOpsAreaTask implements Runnable{
		
		private Path mxfFilePathname;
		
		private WatchKey watchKey;
		
		public void setMxfFilePath(Path mxfFilePathname) {
			this.mxfFilePathname = mxfFilePathname;
		}
		
		public void setWatchKey(WatchKey watchKey) {
			this.watchKey = watchKey;
		}
		
		private String extns[] = {"qc","log", "md5", "mxf"};
		
		@Override
		public void run() {
			String expectedMd5 = null;
			String actualMd5 = null;
			Path artifactMxfSubfolderPath = mxfFilePathname.getParent();
			Path artifactPath = artifactMxfSubfolderPath.getParent();
			File artifactFileObj = artifactMxfSubfolderPath.toFile();
			
			for (int i = 0; i < 60; i++) {
				//logger.debug(mxfFilePathname + ":" + artifactPath);
				if(!artifactFileObj.isDirectory())
					return;
				Collection<File> files = FileUtils.listFiles(artifactFileObj, extns, false);

				if(files.size() == 4) {
					try {
						updateStatus(artifactPath, Status.copy_complete);
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
							Path destArtifactPath = moveFolderToOpsAreaAndOrganise(watchKey, artifactMxfSubfolderPath);
							ingest(destArtifactPath);
						}else {
							logger.error(artifactPath + "MD5 expected != actual, Now what??? ");
							updateStatus(artifactPath, Status.md5_mismatch);
						}
						return;
					}catch (Exception e) {
						e.printStackTrace();
						try {
							updateStatus(artifactPath, Status.move_failed);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
				else {
					try {
						logger.debug(artifactPath + " waiting for mxf sidecar files. Retry count " + i);
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
				}
			}
			logger.info("Giving up on " + artifactMxfSubfolderPath + ". Could be a networking issue... Please do it manually"); // Could be a networking issue...
		}
		
		private Path moveFolderToOpsAreaAndOrganise(WatchKey watchKey, Path srcPath) throws Exception {
			Path destArtifactPath = null;
			try {
				if(watchKey!=null)
					watchKey.cancel();

				String artifactName = getArtifactName(srcPath);
				
				String artifactNamePrefix = null;
				Matcher m = categoryNamePrefixPattern.matcher(artifactName);  		
				if(m.find())
					artifactNamePrefix = m.group(1);

				Categoryelement category = Categoryelement.valueOf(artifactNamePrefix);
				
				String artifactClassFolderName = null;
				if(category.getCategory().equals("Public"))
					artifactClassFolderName = "video-digi-2020-pub";
				else if(category.getCategory().equals("Private1"))
					artifactClassFolderName = "video-digi-2020-priv1";
				else if(category.getCategory().equals("Private2"))
					artifactClassFolderName = "video-digi-2020-priv2";
				
				if(artifactClassFolderName == null)
					throw new Exception("Not able to categorize " + artifactNamePrefix + ":" + artifactName);
				
				destArtifactPath = Paths.get(rootIngestLoc.toString(), artifactClassFolderName, artifactName);
				final Path dest = Paths.get(destArtifactPath.toString(), "mxf");
				Files.createDirectories(dest);
				Files.walkFileTree(srcPath, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.move(file, Paths.get(dest.toString(), file.getFileName().toString()), StandardCopyOption.ATOMIC_MOVE);
						return FileVisitResult.CONTINUE;
					}
				});
				
				logger.info(srcPath.getParent() + " moved succesfully to " + destArtifactPath);
				
//				CommandLineExecuterImpl clei = new CommandLineExecuterImpl();
//				clei.executeCommand("chmod -R 777 " + dest.getParent().toString(), false);
				
				FileUtils.deleteDirectory(srcPath.toFile());
			} catch (IOException e) {
				e.printStackTrace();
				updateStatus(srcPath, Status.move_failed);
			}
			return destArtifactPath;
		}
	}

	public void ingest(Path path) {
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
	
    private enum Status {
    	copying,
    	copy_complete,
    	verifying,
    	verified,
    	md5_mismatch,
    	moved,
    	move_failed,
    	ingested,
    	failed;
    }
    
	private void updateStatus(Path child, Status status) throws Exception {
		logger.info(String.format("%s %s", child, status.name()));
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
		
		new DirectoryWatcher(dir, waitTimes).processEvents();
	}
}
