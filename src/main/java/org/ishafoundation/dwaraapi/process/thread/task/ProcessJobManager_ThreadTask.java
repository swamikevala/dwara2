package org.ishafoundation.dwaraapi.process.thread.task;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.DwaraApiApplication;
import org.ishafoundation.dwaraapi.db.cacheutil.ProcessCacheUtil;
import org.ishafoundation.dwaraapi.db.dao.master.ingest.LibraryclassDao;
import org.ishafoundation.dwaraapi.db.dao.master.ingest.SequenceDao;
import org.ishafoundation.dwaraapi.db.dao.master.process.FiletypeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.LibraryDao;
import org.ishafoundation.dwaraapi.db.model.master.ingest.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.process.Filetype;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;
import org.ishafoundation.dwaraapi.helpers.LogicalFileHelper;
import org.ishafoundation.dwaraapi.model.LogicalFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

//		is process filelevel?
//			what does it mean?
		
//		has dependent tasks?
//			means outputlibraryclass is needed
			
//		has a prerequisite task?
//			means ouputlibrary of the prerequisite task is the input of the task

@Component
@Scope("prototype")
public class ProcessJobManager_ThreadTask implements Runnable{

	private static final Logger logger = LoggerFactory.getLogger(ProcessJobManager_ThreadTask.class);
	
	
	@Autowired
    private LibraryclassDao libraryclassDao;
    
	@Autowired
    private LibraryDao libraryDao;
	
	@Autowired
	private SequenceDao sequenceDao;
	
	@Autowired
    private FileDao fileDao;
	
	@Autowired
	private FiletypeDao filetypeDao;
	
	@Autowired
	private	LogicalFileHelper fileHelper;
	
	@Autowired
	private ProcessCacheUtil processCacheUtil;	
	
	@Autowired
	private ApplicationContext applicationContext;

	
	private Job job;
	
	private int processId;

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public int getProcessId() {
		return processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}	

	@Override
    public void run() {
		System.out.println("jobid - " + job.getJobId());
		int libraryId = job.getInputLibraryId();
		Library library = libraryDao.findById(libraryId).get();
		String libraryName = library.getName();
		Libraryclass libraryclass = libraryclassDao.findById(library.getLibraryclassId()).get();
		String inputLibraryPath = libraryclass.getPathPrefix() + java.io.File.separator + libraryName;

//			String inputLibraryPath = null;
//			if(libraryclass.isSource()) {
//				inputLibraryPath = libraryPathPrefix;
//			}
//			else {
//				inputLibraryPath = libraryPathPrefix + java.io.File.separator + libraryName;
//			}

		// For the task getting processed 
		// 1) if there are any dependent tasks 
		// 2) and is responsible for generating a libraryclass
		
		// then it possibly means the output of the current task is the input for the dependent task
		String outputLibraryPath = getOutputLibraryPath(job, libraryName);

		HashMap<String, Integer> filePathToId = getFilePathToId(libraryId);
		
		Collection<LogicalFile> selectedFileList = getLogicalFileList(libraryclass, inputLibraryPath);
		
		for (Iterator<LogicalFile> iterator = selectedFileList.iterator(); iterator.hasNext();) {
			LogicalFile logicalFile = (LogicalFile) iterator.next();
			logger.info("Now processing - " + job.getJobId() + " " + logicalFile.getAbsolutePath() + " task " + job.getTaskId());
			String path = logicalFile.getAbsolutePath().replace(inputLibraryPath + File.separator, libraryName + File.separator);
//				if(path.contains(configuration.getJunkFilesStagedDirName())) // skipping junk files
//					continue;			
			
			//logger.info("Now processing - " + path);
			int fileId = 0;
			if(filePathToId.containsKey(path))
				fileId = filePathToId.get(path);
			
			String outputFilePath = null;
			if(outputLibraryPath != null)
				outputFilePath = outputLibraryPath + File.separator + FilenameUtils.getFullPathNoEndSeparator(path); 
				
			org.ishafoundation.dwaraapi.db.model.master.workflow.Process processDBEntity = processCacheUtil.getProcess(processId);
			String processName = processDBEntity.getName().toUpperCase();

			Process_ThreadTask process = applicationContext.getBean(Process_ThreadTask.class);
			process.setLibraryId(libraryId);
			process.setJobId(job.getJobId());
			process.setFileId(fileId);
			process.setLogicalFile(logicalFile);
			process.setOutputLibraryPath(outputLibraryPath);
			process.setDestinationFilePath(outputFilePath);
			
			Executor executor = DwaraApiApplication.processName_executor_map.get(processName.toLowerCase());
			executor.execute(process);
			// TODO : create threadpools on processes on bootstrap and make use of them here
//			ProcessThreadPoolExecutor threadExecutorInstance = ProcessThreadPoolExecutorFactory.getInstance(applicationContext, processName);
//			threadExecutorInstance.getExecutor().execute(process);

		}
		
		
//		//*** TODO : @Swami 
//		// TODO if no. of errors in the process reach the configured max_errors threshold then we stop further processing.... count(*) on failures for the job_id...
//		FilesSelector fileSelector = FilesSelectorFactory.getInstance(applicationContext, filetypeName);
//		if(libraryclass.isSource()){ 
//			@SuppressWarnings("unchecked")
//			Collection<File> selectedFileList = (Collection<File>) fileSelector.getFiles(inputLibraryPath);
//			
//			for (Iterator<File> iterator = selectedFileList.iterator(); iterator.hasNext();) {
//				File nthToBeProcessedFile = (File) iterator.next();
//				
//				String path = nthToBeProcessedFile.getAbsolutePath();
////					if(path.contains(configuration.getJunkFilesStagedDirName())) // skipping junk files
////						continue;			
//				
//				path = path.replace(inputLibraryPath + File.separator, "");
//				logger.info("Now processing - " + path);
//
//				int fileId = 0;
//				if(filePathToId.containsKey(path))
//					fileId = filePathToId.get(path);
//				
//				String outputFilePath = outputLibraryPath + File.separator + FilenameUtils.getFullPathNoEndSeparator(path); 
//				
//				process(libraryId, fileId, nthToBeProcessedFile, outputFilePath);
//			}
//		}else {
//			@SuppressWarnings("unchecked")
//
////				HashMap<String, ProxyFileCollection> filepathName_to_proxyFileCollection = (HashMap<String, ProxyFileCollection>) fileSelector.getFiles(inputLibraryPath);
////				Set<String> keySet = filepathName_to_proxyFileCollection.keySet();
////				for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
////					String key = (String) iterator.next();
////					ProxyFileCollection proxyFileCollection = filepathName_to_proxyFileCollection.get(key);
////					
////					process(libraryId, fileId, nthToBeProcessedFileSet, outputFilePath);
////				}
//			HashMap<String, Set<File>> filepathName_to_proxyFileSet = (HashMap<String, Set<File>>)  fileSelector.getFiles(inputLibraryPath);
//			Set<String> keySet = filepathName_to_proxyFileSet.keySet();
//			for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
//				String keyName = (String) iterator.next();
//				Set<File> nthToBeProcessedFileSet = filepathName_to_proxyFileSet.get(keyName);
//				
//				process(libraryId, -9999, nthToBeProcessedFileSet, null); // TODO : fileId will not be available for proxygen step as the noFileRecords is set to true
//			}	
//				
//		}
			
		// TODO Where to create these Library and file entries???
		// If here in the framework means We are trying to create a library entry even before the items are processed in their threads at their own pace. Tighten up...
		int outputLibraryId = 0;

	}

	
	private String getOutputLibraryPath(Job job, String libraryName) {
		int taskId = job.getTaskId();
		Libraryclass outputLibraryclass = libraryclassDao.findByTaskId(taskId); //  the task output's resultant libraryclass
		
		// frames where to generate the files in the physical system...
		String outputLibraryPath = null;
		if(outputLibraryclass != null) {
			int outputLibraryClassSequenceId = outputLibraryclass.getSequenceId();
			String outputLibraryNamePrefix = sequenceDao.findById(outputLibraryClassSequenceId).get().getPrefix();
			outputLibraryPath = outputLibraryclass.getPathPrefix() + java.io.File.separator + outputLibraryNamePrefix + libraryName;
		}
		return outputLibraryPath;
	}
	
	private HashMap<String, Integer> getFilePathToId(int libraryId) {
		HashMap<String, Integer> filePathToId = new HashMap<String, Integer>();
		
		List<org.ishafoundation.dwaraapi.db.model.transactional.File> libraryFileList = fileDao.findAllByLibraryId(libraryId);
		for (Iterator<org.ishafoundation.dwaraapi.db.model.transactional.File> iterator = libraryFileList.iterator(); iterator.hasNext();) {
			org.ishafoundation.dwaraapi.db.model.transactional.File nthFile = iterator.next();
			filePathToId.put(nthFile.getPathname(), nthFile.getFileId());
		}
		return filePathToId;
	}
	
	private Collection<LogicalFile> getLogicalFileList(Libraryclass libraryclass, String inputLibraryPath){
		int filetypeId = libraryclass.getFiletypeId();
		Filetype filetype = filetypeDao.findById(filetypeId).get();
		return fileHelper.getFiles(inputLibraryPath, filetype.getExtensionsAsArray(), filetype.isIncludeSidecarFiles(), filetype.getSidecarExtensionsAsArray());
	}
}
