package org.ishafoundation.dwara.misc.watcher;


import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
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
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwara.misc.common.Constants;
import org.ishafoundation.dwara.misc.common.MoveUtil;
import org.ishafoundation.dwara.misc.common.Status;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is responsible for watching for files copied in prasadcorp workspace and when completed moves the file to dwara's user specific artifactclass location and calls the ingest 
 */

public class DirectoryWatcher implements Runnable{

	private static Logger logger = LoggerFactory.getLogger(DirectoryWatcher.class);

	private final WatchService watchService;
	private final Path watchedDir;
	private final Map<WatchKey,Path> keys;
	private final Map<Path, Long> expirationTimes = new HashMap<Path, Long>();
	private Long newFileWait = 10000L;
	private static Executor executor = null;
	
	private Set<Path> copyingArtifacts = new TreeSet<Path>();
	private Set<Path> verifyPendingArtifacts = new TreeSet<Path>();
	private Path miscDirPath = null; // Hardcoded - will be watchedDir + failedDirName;
	private Path completedDirPath = null; // Hardcoded - will be watchedDir + completedDirName;
	private Path failedDirPath = null; // Hardcoded - will be watchedDir + failedDirName;
	private Path copiedDirPath = null;
	private Path copyFailedDirPath = null;
	private Map<Path, Integer> artifact_Retrycount_Map = new HashMap<Path, Integer>();
	
	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>)event;
	}

	/**
	 * Creates a WatchService and registers the given directory
	 */
	DirectoryWatcher(Path watchedDirPath, Long waitTime) throws IOException {
		this.watchService = FileSystems.getDefault().newWatchService();
		this.watchedDir = watchedDirPath;
		this.newFileWait = waitTime;
		this.keys = new HashMap<WatchKey,Path>();
		this.miscDirPath = Paths.get(watchedDir.toString(), Constants.miscDirName);
		this.failedDirPath = Paths.get(watchedDir.toString(), Constants.failedDirName);
		this.completedDirPath = Paths.get(watchedDir.toString(), Constants.completedDirName);
		this.copiedDirPath = Paths.get(watchedDir.toString(), Constants.copiedDirName);
		this.copyFailedDirPath = Paths.get(watchedDir.toString(), Constants.copyFailedDirName);
	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void register(Path artifactDirPath) throws IOException {
		copyingArtifacts.add(artifactDirPath);
		expirationTimes.put(artifactDirPath, System.currentTimeMillis()+newFileWait);
		
		WatchKey key = artifactDirPath.register(watchService, ENTRY_MODIFY);

		if (logger.isTraceEnabled()) {
			Path prev = keys.get(key);
			if (prev == null) {
				logger.info(String.format("register: %s\n", artifactDirPath));
			} else {
				if (!artifactDirPath.equals(prev)) {
					logger.info(String.format("update: %s -> %s\n", prev, artifactDirPath));
				}
			}
		}
		keys.put(key, artifactDirPath);
		
		// if there are folders with no events pending, trigger a modify event so that we handleExpiredTimes 
		CommandLineExecuterImpl clei = new CommandLineExecuterImpl();
		try {
			if(!artifactDirPath.toString().endsWith("mxf"))
				updateStatus(artifactDirPath, Status.copying);
			clei.executeCommand("chmod -R 777 " + artifactDirPath, false);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Process all events for keys queued to the watchService
	 */
	public void run(){
		for (;;) { //infinite loop

			// wait for key to be signalled
			WatchKey key;
			try {
				key = watchService.take();
			} catch (InterruptedException x) {
				return;
			}

			for(;;) { // infinite loop for timed polling to find if any pending events are not and check on expiry...
				try {
					long currentTime = System.currentTimeMillis();
	
					if(key != null) { // if events are still pending...
	
						Path registeredDirPath = keys.get(key);
						if (registeredDirPath == null) {
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
							Path child = registeredDirPath.resolve(name);
	
							// print out event
							if (logger.isTraceEnabled())
								logger.trace(String.format("%s: %s\n", event.kind().name(), child));
	
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
	
					handleExpiredWaitTimes(currentTime);
	
					// If there are no files left stop polling and block on .take()
					if(expirationTimes.isEmpty())
						break;
	
					long minExpiration = Collections.min(expirationTimes.values());
					long timeout = minExpiration - currentTime;
					if (logger.isTraceEnabled())
						logger.trace("timeout: "+timeout);
					key = watchService.poll(timeout, TimeUnit.MILLISECONDS);
				}catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	private String getArtifactName(Path child){
		String filePathMinusWatchDirPrefix = child.toString().replace(watchedDir.toString() + File.separator, "");
		String artifactName = StringUtils.substringBefore(filePathMinusWatchDirPrefix, File.separator);
		return artifactName;
	}

	private void handleExpiredWaitTimes(Long currentTime) throws Exception {
		// Start import for files for which the expirationtime has passed
		Iterator<Entry<Path, Long>> it = expirationTimes.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<Path, Long> item = (Entry<Path, Long>) it.next();
			Long expiryTime = (Long) item.getValue();
			Path filePath = item.getKey();

			if(expiryTime <= currentTime) {
				if(filePath.getFileName().toString().endsWith(".mxf")) {
					updateStatus(filePath, Status.copy_complete);
					createTaskAndExecute(filePath);
				}
				it.remove();
			}
		}
	}

	private void createTaskAndExecute(Path mxfFilePath) throws Exception{
		Task task = new Task();
		task.setMxfFilePath(mxfFilePath);
		copyingArtifacts.remove(mxfFilePath.getParent());
		verifyPendingArtifacts.add(mxfFilePath.getParent());
		executor.execute(task);
	}

	public class Task implements Runnable{

		private Path mxfFilePathname; // will have something like /data/prasad-staging/H122/H122.mxf

		public void setMxfFilePath(Path mxfFilePathname) {
			this.mxfFilePathname = mxfFilePathname; 
		}
		
		@Override
		public void run() {
			Path artifactPath = mxfFilePathname.getParent(); 
			File artifactFileObj = artifactPath.toFile();

			if(!artifactFileObj.isDirectory())
				return;

			String artifactName = artifactFileObj.getName();
			try {
				ArtifactValidationResponse avr = ArtifactValidator.validateName(artifactName);
				if(!avr.isValid()) { 
					move(artifactPath, false, avr.getFailureReason());
					return;
				}
				
				avr = ArtifactValidator.validateFiles(artifactFileObj);
				if(!avr.isValid()) { 
					move(artifactPath, false, avr.getFailureReason());
					return;
				}
				

				Collection<File> neededFiles = FileUtils.listFiles(artifactFileObj, ArtifactValidator.justNeededExtns, false);
				avr = ArtifactValidator.neededFiles(artifactFileObj, neededFiles);
				boolean isChecksumValid = ArtifactValidator.validateChecksum(artifactPath, neededFiles);
				if(avr.isValid() && isChecksumValid){
					move(artifactPath, true);	
				}else {
					Integer retrycount = artifact_Retrycount_Map.get(artifactPath);
					if(retrycount == null) {
						retrycount = 1;
						artifact_Retrycount_Map.put(artifactPath, retrycount);
					}
					else
						artifact_Retrycount_Map.put(artifactPath, retrycount = retrycount + 1);
	
					if(retrycount <= 3) {
						try {
							logger.info("Could be because mxf is still not copied in full, but wait times expired[network latency etc.,]. Re-registering " + artifactPath + " - " + retrycount);
							register(artifactPath);
						}catch (Exception e) {
							logger.error("Unable to re-register - " + e.getMessage(), e);
						}
					}else {// inspite of retries if side car files missing means source folder missing the mandatory files or if checksum fails, move it to "FAILED" folder
						String failureReason = !isChecksumValid ? "MD5 mismatch" : avr.getFailureReason();
						move(artifactPath, false, failureReason);
					}
				}
				verifyPendingArtifacts.remove(artifactPath);
			}
			catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private void move(Path artifactPath, boolean completed) throws Exception{
		move(artifactPath, completed, null);
	}
	
	private void move(Path artifactPath, boolean completed, String failureReason) throws Exception{
		String artifactName = getArtifactName(artifactPath);
		String csvFileName = Constants.failedCsvName;
		String destRootPath = failedDirPath.toString();
		Status status = Status.moved_to_failed_dir;
		// move it to completed folder
		if(completed) {
			destRootPath = completedDirPath.toString();
			status = Status.moved_to_validated_dir;
			csvFileName = Constants.completedCsvName;
		}
		Path destPath = Paths.get(destRootPath, artifactName);
		Path csvFilePath = Paths.get(watchedDir.toString(), csvFileName);
		try {
			MoveUtil.move(artifactPath, destPath);
			updateStatus(artifactPath, status);
			updateCSV(csvFilePath, artifactName, failureReason);
		}catch (Exception e) {
			logger.error("Unable to move " + artifactPath + " to " + destPath, e);
			try {
				updateStatus(artifactPath, Status.move_failed);
				return;
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
	}
	
	private void updateCSV(Path csvFilePath, String artifactName, String failureReason) {
		StringBuilder sb = new StringBuilder();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		String timestamp = LocalDateTime.now().format(formatter);
		sb.append(timestamp);
		sb.append(",");
		sb.append(artifactName);

		if(failureReason != null) {
			sb.append(",");
			sb.append(failureReason);
		}
			
		try {
			FileUtils.write(csvFilePath.toFile(), sb.toString() + "\n", true);
			logger.info(csvFilePath.getFileName() + " updated");
		} catch (IOException e) {
			logger.error("Unable to write csv " + csvFilePath +  " : " + e.getMessage(), e);
		}
		
	}

	private void updateStatus(Path child, Status status) throws Exception {
		logger.info(String.format("%s %s", child, status.name()));
	}

	static void usage() {
		System.err.println("usage: java -cp dwara-watcher-2.0.jar org.ishafoundation.dwara.misc.watcher.DirectoryWatcher <dirToBeWatched(\"/data/prasad-staging\")> folderAgeInMts folderAgePollingIntervalInMts noOfThreadsForChksumValidation watchEventExpiryWaitTimeInSecs");
		System.exit(-1);
	}
	
	public static void main(String[] args) throws Exception {
		// parse arguments
		if (args.length != 5)
			usage();

		// register directory and process its events
		final Path dirToBeWatched = Paths.get(args[0]);
		final int folderAgeInMts = Integer.parseInt(args[1]);
		int folderAgePollingIntervalInMts = Integer.parseInt(args[2]);
		int noOfThreadsForChksumValidation = Integer.parseInt(args[3]);
		long waitTimes = Long.parseLong(args[4]) * 1000;

		final DirectoryWatcher dirwatcher = new DirectoryWatcher(dirToBeWatched, waitTimes);
		Thread thread = new Thread(dirwatcher);
		thread.setDaemon(true);
		thread.start();
		
		executor = new ThreadPoolExecutor(noOfThreadsForChksumValidation, noOfThreadsForChksumValidation, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

		for(;;) {
			try {
				Files.walkFileTree(dirToBeWatched, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs)
							throws IOException
					{
						String dirPath = path.toString();
						/* We shouldnt register for the following
						 * 1) configured main watched dir itself
						 * 2) predefined subfolders like MISC,Validated,ValFailed,Copied,CopyFailed
						 * 3) already registered artifacts in the previous poll which are either getting copied or verified segments 
						 * 
						 * In other words we should only register artifact directories that are aged than configured folderAgeInMts and that too only once
						*/ 
						if(!dirPath.equals(dirToBeWatched.toString()) && 
								!dirPath.startsWith(dirwatcher.miscDirPath.toString()) && !dirPath.startsWith(dirwatcher.failedDirPath.toString()) && 
								!dirPath.startsWith(dirwatcher.completedDirPath.toString()) && !dirPath.startsWith(dirwatcher.copiedDirPath.toString()) && 
								!dirPath.startsWith(dirwatcher.copyFailedDirPath.toString()) && 
								!dirwatcher.verifyPendingArtifacts.contains(path) && !dirwatcher.copyingArtifacts.contains(path)) { // Dont register the failed and completed dir...
							FileTime lastModified = attrs.lastModifiedTime();
							long mtsInMillis = folderAgeInMts * 60 * 1000;
							if(System.currentTimeMillis() > lastModified.toMillis() + mtsInMillis) {
								dirwatcher.register(path);
							}
						}
						return FileVisitResult.CONTINUE;
					}
				});
				
				Thread.sleep(folderAgePollingIntervalInMts * 60 * 1000);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}

