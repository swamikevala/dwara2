package org.ishafoundation.dwara.misc.watcher;


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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
	private final Path watchedDirPath;
	private final Path csvDirPath;
	private final Map<WatchKey,Path> keys;
	private final Map<Path, Long> expirationTimes = new HashMap<Path, Long>();
	private Long newFileWait = 10000L;
	private boolean isChecksumVerificationNeeded = false;
	private static Executor executor = null;
	
	private Set<Path> copyingArtifacts = new TreeSet<Path>();
	private Set<Path> verifyPendingArtifacts = new TreeSet<Path>();
	private Path miscDirPath = null;
	private Path validatedDirPath = null;
	private Path failedDirPath = null;
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
	DirectoryWatcher(Path watchedDir,  Long waitTime, boolean isChecksumVerificationNeeded, Path systemDir, Path csvDir) throws IOException {
		this.watchService = FileSystems.getDefault().newWatchService();
		this.watchedDirPath = watchedDir;
		
		this.csvDirPath = csvDir;
		this.newFileWait = waitTime;
		this.keys = new HashMap<WatchKey,Path>();
		this.isChecksumVerificationNeeded = isChecksumVerificationNeeded;
		this.miscDirPath = Paths.get(watchedDir.toString(), Constants.miscDirName);
		this.failedDirPath = Paths.get(watchedDir.toString(), Constants.failedDirName);
		
		this.validatedDirPath = Paths.get(systemDir.toString(), Constants.validatedDirName);
		this.copiedDirPath = Paths.get(systemDir.toString(), Constants.copiedDirName);
		this.copyFailedDirPath = Paths.get(systemDir.toString(), Constants.copyFailedDirName);
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

			List<String> setFilePermissionsCommandParamsList = new ArrayList<String>();
			setFilePermissionsCommandParamsList.add("chmod");
			setFilePermissionsCommandParamsList.add("-R");
			setFilePermissionsCommandParamsList.add("777");
			setFilePermissionsCommandParamsList.add(artifactDirPath.toString());
			clei.executeCommand(setFilePermissionsCommandParamsList, false);
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
	
							if(expirationTimes.get(child.getParent()) != null)
								expirationTimes.put(child.getParent(), System.currentTimeMillis()+newFileWait);
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
		String filePathMinusWatchDirPrefix = child.toString().replace(watchedDirPath.toString() + File.separator, "");
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
			Path artifactPath = item.getKey();
			
			if(expiryTime <= currentTime) {
				updateStatus(artifactPath, Status.copy_complete);
				createTaskAndExecute(artifactPath);
				it.remove();
			}
		}
	}

	private void createTaskAndExecute(Path artifactPath) throws Exception{
		Task task = new Task();
		task.setArtifactPath(artifactPath);
		copyingArtifacts.remove(artifactPath);
		verifyPendingArtifacts.add(artifactPath);
		executor.execute(task);
	}

	public class Task implements Runnable{

		private Path artifactPath; // will have something like /data/prasad-staging/H122/H122.mxf

		public void setArtifactPath(Path artifactPath) {
			this.artifactPath = artifactPath; 
		}
		
		@Override
		public void run() {
			//Path artifactPath = mxfFilePathname.getParent(); 
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
				
				if(!avr.isValid())
					retryOrMove(avr.getFailureReason());
				else if(avr.isValid() && !isChecksumVerificationNeeded){
					move(artifactPath, true);	// if checksum verify is not needed move it to valid folder from here...
				}else if(isChecksumVerificationNeeded) {
					boolean isChecksumValid = ArtifactValidator.validateChecksum(artifactPath, neededFiles);
					if(isChecksumValid){
						move(artifactPath, true);		
					}else {
						retryOrMove("md5 mismatch");
					}
				}
			
				verifyPendingArtifacts.remove(artifactPath);
			}
			catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		private void retryOrMove(String failureReason) throws Exception
		{
			Integer retrycount = artifact_Retrycount_Map.get(artifactPath);
			if(retrycount == null) {
				retrycount = 1;
				artifact_Retrycount_Map.put(artifactPath, retrycount);
			}
			else
				artifact_Retrycount_Map.put(artifactPath, retrycount = retrycount + 1);
		
			if(retrycount <= 3) {
				try {
					logger.info("Could be because artifact is still not copied in full, but wait times expired[network latency etc.,]. Re-registering " + artifactPath + " - " + retrycount);
					register(artifactPath);
				}catch (Exception e) {
					logger.error("Unable to re-register - " + e.getMessage(), e);
				}
			}else {// inspite of retries if side car files missing means source folder missing the mandatory files or if checksum fails, move it to "FAILED" folder
				move(artifactPath, false, failureReason);
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
			destRootPath = validatedDirPath.toString();
			status = Status.moved_to_validated_dir;
			csvFileName = Constants.validatedCsvName;
		}
		Path destPath = Paths.get(destRootPath, artifactName);
		Path csvFilePath = Paths.get(csvDirPath.toString(), csvFileName);
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
		System.err.println("usage: java -cp dwara-watcher-2.0.jar org.ishafoundation.dwara.misc.watcher.DirectoryWatcher "
				+ "<dirToBeWatched(\"/data/prasad-staging\")> "
				+ "folderAgeInSecs "
				+ "folderAgePollingIntervalInSecs "
				+ "watchEventExpiryWaitTimeInSecs "
				+ "isChecksumVerificationNeeded "
				+ "systemDirPath "
				+ "csvsDirPath "
				+ "noOfThreadsForChksumValidation");
		System.err.println("where,");
		System.err.println("args[0] - dirToBeWatched - The directory where this service needs to watched for modify events on files");
		System.err.println("args[1] - folderAgeInSecs - The wait period only after which the files are watched for events");
		System.err.println("args[2] - folderAgePollingIntervalInSecs - The polling interval to check if there are directories in <dirToBeWatched> that are past <folderAgeInSecs>");
		System.err.println("args[3] - watchEventExpiryWaitTimeInSecs - Timeout Interval for waiting on watching with no events");
		System.err.println("args[4] - isChecksumVerificationNeeded - Boolean - Do we need to verify the checksum of the Mxf?");
		System.err.println("args[5] - systemDirPath - The directory where sub directories needed by system like \"Validated\", \"Copied\", \"CopyFailed\" need to be");
		System.err.println("args[6] - csvsDirPath - The directory in which csv files need to be created");
		System.err.println("args[7] - noOfThreadsForParallelProcessing - no of parallel processing threads after copy is complete");
		System.exit(-1);
	}
	
	/**
	 * 

	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// parse arguments
		if (args.length != 8)
			usage();

		// register directory and process its events
		final Path dirToBeWatched = Paths.get(args[0]);
		final long folderAgeInSecs = Long.parseLong(args[1]) * 1000;
		long folderAgePollingIntervalInSecs = Long.parseLong(args[2]) * 1000;
		long waitTimes = Long.parseLong(args[3]) * 1000;
		Boolean isChecksumVerificationNeeded = Boolean.parseBoolean(args[4]);
		final Path systemDirLocation = Paths.get(args[5]);
		final Path csvLocation = Paths.get(args[6]); 
		int noOfThreadsForParallelProcessing = Integer.parseInt(args[7]);
		
		Path validatedCSVFilePath = Paths.get(csvLocation.toString(), Constants.validatedCsvName);
		validatedCSVFilePath.toFile().createNewFile();
		Path failedCSVFilePath = Paths.get(csvLocation.toString(), Constants.failedCsvName);
		failedCSVFilePath.toFile().createNewFile();
		
		final DirectoryWatcher dirwatcher = new DirectoryWatcher(dirToBeWatched, waitTimes, isChecksumVerificationNeeded, systemDirLocation, csvLocation);
		Thread thread = new Thread(dirwatcher);
		thread.setDaemon(true);
		thread.start();
		
		executor = new ThreadPoolExecutor(noOfThreadsForParallelProcessing, noOfThreadsForParallelProcessing, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

		for(;;) {
			try {
				if(dirToBeWatched.toFile().exists()) {
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
									!dirPath.startsWith(dirwatcher.validatedDirPath.toString()) && !dirPath.startsWith(dirwatcher.copiedDirPath.toString()) && 
									!dirPath.startsWith(dirwatcher.copyFailedDirPath.toString()) && 
									!dirwatcher.verifyPendingArtifacts.contains(path) && !dirwatcher.copyingArtifacts.contains(path)) { // Dont register the failed and completed dir...
								FileTime lastModified = attrs.lastModifiedTime();
								if(System.currentTimeMillis() > lastModified.toMillis() + folderAgeInSecs) {
									dirwatcher.register(path);
								}
							}
							return FileVisitResult.CONTINUE;
						}
					});
				}else {
					logger.warn(dirToBeWatched.toString() + " doesnt exist");
				}
				
				Thread.sleep(folderAgePollingIntervalInSecs);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}

