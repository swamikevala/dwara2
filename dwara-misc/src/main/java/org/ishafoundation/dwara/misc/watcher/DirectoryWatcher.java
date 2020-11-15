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
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class DirectoryWatcher {

	private final WatchService watchService;
	private final Path watchedDir;
	private final Map<WatchKey,Path> keys;
	private boolean trace = true;
	private final Map<Path, Long> expirationTimes = new HashMap<Path, Long>();
	private Long newFileWait = 10000L;

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

		System.out.format("Scannnning %s ...\n", dir);
		registerAll(dir);
		System.out.println("Done.");

		// enable trace after initial registration
		this.trace = true;
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
				register(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void register(Path dir) throws IOException {
		WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		if (trace) {
			Path prev = keys.get(key);
			if (prev == null) {
				System.out.format("register: %s\n", dir);
			} else {
				if (!dir.equals(prev)) {
					System.out.format("update: %s -> %s\n", prev, dir);
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
						System.err.println("WatchKey not recognized!!");
						continue;
					}

					for (WatchEvent<?> event: key.pollEvents()) {
						Kind<?> kind = event.kind();

						// TBD - provide example of how OVERFLOW event is handled
						if (kind == OVERFLOW) {
							continue;
						}

						// Context for directory entry event is the file name of entry
						WatchEvent<Path> ev = cast(event);
						Path name = ev.context();
						Path child = dir.resolve(name);

						// print out event
						if (trace)
							System.out.format("%s: %s\n", event.kind().name(), child);

						// if directory is created, and watching recursively, then
						// register it and its sub-directories
						if (kind == ENTRY_CREATE) {
							try {
								if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
									registerAll(child);
								}
							} catch (IOException x) {
								// ignore to keep sample readbale
							}
						}
						else if (kind == ENTRY_MODIFY) {
							Path artifactPath = getArtifactPath(child);
							if (trace)
								System.out.println("artifactPath " + artifactPath);
							// Update modified time
							BasicFileAttributes attrs = null;
							try {
								attrs = Files.readAttributes(child, BasicFileAttributes.class, NOFOLLOW_LINKS);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								// e.printStackTrace();
								continue; // Happens when copy complete, but permissions set by stagedservice triggered this event but by then the stagedservice thread moved this file to /data/staged...
							}
							FileTime lastModified = attrs.lastModifiedTime();
							if (trace)
								System.out.println("lastModified " + lastModified);
							expirationTimes.put(artifactPath, lastModified.toMillis()+newFileWait);
						}
					}

					// reset key and remove from set if directory no longer accessible
					boolean valid = key.reset();
					if (!valid) {
						if (trace) {
							System.out.println(key + " not valid");
							System.out.println("Will be removing " + keys.get(key) + " from key list");
						}
						keys.remove(key);

						// all directories are inaccessible
						if (keys.isEmpty()) {
							break;
						}
					}
				}
				else {
					if (trace)
						System.out.println("No pending events");
				}

				handleExpiredWaitTimes(currentTime);

				// If there are no files left stop polling and block on .take()
				if(expirationTimes.isEmpty())
					break;

				long minExpiration = Collections.min(expirationTimes.values());
				long timeout = minExpiration - currentTime;
				if (trace)
					System.out.println("timeout: "+timeout);
				key = watchService.poll(timeout, TimeUnit.MILLISECONDS);
			}
		}
	}

	private Path getArtifactPath(Path child){
		String filePathMinusWatchDirPrefix = child.toString().replace(watchedDir.toString() + File.separator, "");
		String artifactName = StringUtils.substringBefore(filePathMinusWatchDirPrefix, File.separator);
		if (trace)
			System.out.println("artifactName " + artifactName);
		return Paths.get(watchedDir.toString(), artifactName); 
	}

	private void handleExpiredWaitTimes(Long currentTime) {
		// Start import for files for which the expirationtime has passed

		Iterator<Entry<Path, Long>> it = expirationTimes.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<Path, Long> item = (Entry<Path, Long>) it.next();
			Long expiryTime = (Long) item.getValue();
			if(expiryTime <= currentTime) {
				action(item.getKey());
				// System.out.println("expired " + item.getKey());
				// do something with the file
				it.remove();
			}
		}

		//		for(Entry<Path, Long> entry : expirationTimes.entrySet()) {
		//			if(entry.getValue() <= currentTime) {
		//				System.out.println("expired " + entry);
		//				// do something with the file
		//				expirationTimes.remove(entry.getKey());
		//			}
		//		}
	}

    private void action(Path child){
		System.out.println(child + " copy completed");
		System.out.println("Method assumes Prasad supplies a \"MD5.txt\" and the video file ending with \".mxf\" in the supplied folder");
		String expectedMd5 = null;
		String actualMd5 = null;
		
		try {
			Collection<File> files = FileUtils.listFiles(child.toFile(), null, false);
			for (File file : files) {
				String fileName = file.getName();
				//System.out.println(fileName);
				if(fileName.equals("MD5.txt")) {
					expectedMd5 = FileUtils.readFileToString(file).trim().toUpperCase();
					System.out.println("expectedMd5 " + expectedMd5);
				}
				else if(fileName.endsWith(".mxf")) {
				    MessageDigest md = MessageDigest.getInstance("MD5");
				    md.update(Files.readAllBytes(file.toPath()));
				    byte[] digest = md.digest();
				    actualMd5 = DatatypeConverter.printHexBinary(digest).toUpperCase();
				    System.out.println("actualMd5 " + actualMd5);
				}
			}
			if(expectedMd5.equals(actualMd5)) {
				System.out.println("MD5 expected = actual, call dwara api for " + child);
				invokeIngest(child);
			}else {
				System.out.println(child + "MD5 expected != actual, Now what??? ");
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    }
    
    private void invokeIngest(Path child) {
    	String payload = "{\"artifactclass\":\"prasad-pub\",\"stagedFiles\":[{\"path\":\"/data/user/pgurumurthy/ingest/prasad-pub\",\"name\":\"<<DummyArtifactName>>\"}]}";
    	String artifactName = StringUtils.substringAfterLast(child.toString(), File.separator);
    	payload = payload.replace("<<DummyArtifactName>>", artifactName);
    	System.out.println("payload "+ payload);
    	String endpointUrl = "http://pgurumurthy:ShivaShambho@172.18.1.213:8080/api/staged/ingest";


		String response = null;
		try {
			response = postIt(endpointUrl, null, payload);
			System.out.println("resp " + response);
		}catch (Exception e) {
			// TODO: handle exception
		}
    }
    
	static void usage() {
		System.err.println("usage: java DirectoryWatcher [waitTimeInSecs] <dirToBeWatched>");
		System.exit(-1);
	}

	public static void main(String[] args) throws Exception {
		// parse arguments
		if (args.length == 0 || args.length > 2)
			usage();

		int dirArg = 0;
		long waitTimes = 0L;
		if (args.length == 2) {
			waitTimes = Long.parseLong(args[0]) * 1000;
			dirArg++;
		}

		// register directory and process its events
		Path dir = Paths.get(args[dirArg]);

		new DirectoryWatcher(dir, waitTimes).processEvents();
	}
	
	public static String postIt(String endpointUrl, String authHeader, String postBodyPayload) throws Exception{
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String responseBody = executePost(httpClient, endpointUrl, authHeader, postBodyPayload);
        return responseBody;
	}
	
	private static String executePost(CloseableHttpClient httpClient, String endpointUrl, String authHeader, String postBodyPayload) throws Exception {
        String responseBody = null;
        try {
	        StringEntity reqEntity = new StringEntity(postBodyPayload, "UTF-8");
	
	        HttpPost httpPost = new HttpPost(endpointUrl);
	        httpPost.setEntity(reqEntity);
	        if(authHeader != null)
	        	httpPost.setHeader(HttpHeaders.AUTHORIZATION, authHeader);    
	        httpPost.setHeader("Content-Type", "application/json");        
	        
	        ResponseHandler<String> responseHandler = createResponseHandler();
	
	        
	        responseBody = httpClient.execute(httpPost, responseHandler);
	
		}
		catch (Exception e) {
			System.err.println("httpCall failed - " + e.getMessage());
			throw e;
		} finally {
	        httpClient.close();
	    }
        return responseBody;		
	}
	
	private static ResponseHandler<String> createResponseHandler(){
        // Create a custom response handler
        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
            public String handleResponse(
                    final HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                System.out.println("Status: " + status);
                HttpEntity entity = response.getEntity();
                String resp = entity != null ? EntityUtils.toString(entity) : null;
                if (status >= 200 && status < 300) {
                    return resp;
                } else {
                    throw new ClientProtocolException("Unexpected response status : " + status + ". Resp body is : " + resp);
                }
            }
        };
		return responseHandler;
	}
}