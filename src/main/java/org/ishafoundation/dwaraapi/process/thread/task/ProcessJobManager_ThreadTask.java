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
		logger.trace("managing process job - " + job.getJobId());
		int libraryId = job.getInputLibraryId();
		Library library = libraryDao.findById(libraryId).get();
		String libraryName = library.getName();
		Libraryclass libraryclass = libraryclassDao.findById(library.getLibraryclassId()).get();
		String category = libraryclass.getCategory();
		String inputLibraryPath = libraryclass.getPath() + File.separator + libraryName;
		
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
		
		
		String outputLibraryName = null;
		String outputLibraryPathname = null; // holds where to generate the files in the physical system...
		int taskId = job.getTaskId();
		Libraryclass outputLibraryclass = libraryclassDao.findByTaskId(taskId); //  the task output's resultant libraryclass
		if(outputLibraryclass != null) {
			outputLibraryName = getOutputLibraryName(outputLibraryclass, libraryName);
			outputLibraryPathname = getOutputLibraryPathname(outputLibraryclass, outputLibraryName);
		}
		
		HashMap<String, Integer> filePathToId = getFilePathToId(libraryId);
		
		Collection<LogicalFile> selectedFileList = getLogicalFileList(libraryclass, inputLibraryPath);
		
		for (Iterator<LogicalFile> iterator = selectedFileList.iterator(); iterator.hasNext();) {
			LogicalFile logicalFile = (LogicalFile) iterator.next(); // would have an absolute file like C:\data\ingested\14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A\1 CD\00018.MTS and its sidecar files
			logger.info("Now kicking off - " + job.getJobId() + " " + logicalFile.getAbsolutePath() + " task " + job.getTaskId());
			// TODO - Need to work on this for Audio where its just file..
			String x = FilenameUtils.getFullPath(logicalFile.getAbsolutePath()) + FilenameUtils.getBaseName(logicalFile.getAbsolutePath());
			if(logicalFile.getAbsolutePath().equals(x)) { 
				// means input library is a file and not a directory
				
			}
			String filePathnameWithoutLibraryNamePrefixed = logicalFile.getAbsolutePath().replace(inputLibraryPath + File.separator, ""); // would hold 1 CD\00018.MTS
			String libraryNamePrefixedFilePathname = logicalFile.getAbsolutePath().replace(inputLibraryPath + File.separator, libraryName + File.separator); // would hold 14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A\1 CD\00018.MTS
//				if(path.contains(configuration.getJunkFilesStagedDirName())) // skipping junk files
//					continue;			
			
			//logger.info("Now processing - " + path);
			int fileId = 0;
			if(filePathToId.containsKey(libraryNamePrefixedFilePathname))
				fileId = filePathToId.get(libraryNamePrefixedFilePathname);
			
			String outputFilePath = null;
			if(outputLibraryPathname != null)
				outputFilePath = outputLibraryPathname + File.separator + FilenameUtils.getFullPathNoEndSeparator(filePathnameWithoutLibraryNamePrefixed); 
				
			org.ishafoundation.dwaraapi.db.model.master.workflow.Process processDBEntity = processCacheUtil.getProcess(processId);
			String processName = processDBEntity.getName().toUpperCase();

			Process_ThreadTask process = applicationContext.getBean(Process_ThreadTask.class);
			process.setJobId(job.getJobId());
			process.setFileId(fileId);
			process.setLogicalFile(logicalFile);
			
			process.setInputLibraryId(libraryId);
			process.setInputLibraryName(libraryName);
			process.setLibraryCategory(category);

			process.setOutputLibraryName(outputLibraryName);
			process.setOutputLibraryPathname(outputLibraryPathname);
			process.setDestinationDirPath(outputFilePath);
			
			Executor executor = DwaraApiApplication.processName_executor_map.get(processName.toLowerCase());
			executor.execute(process);
		}
		// TODO if no. of errors in the process reach the configured max_errors threshold then we stop further processing.... count(*) on failures for the job_id...


	}


	private String getOutputLibraryName(Libraryclass outputLibraryclass, String inputLibraryName){
		int outputLibraryClassSequenceId = outputLibraryclass.getSequenceId();
		String outputLibraryNamePrefix = sequenceDao.findById(outputLibraryClassSequenceId).get().getPrefix();
		return outputLibraryNamePrefix + inputLibraryName;
	}

	private String getOutputLibraryPathname(Libraryclass outputLibraryclass, String outputLibraryName) {
		return outputLibraryclass.getPath() + java.io.File.separator + outputLibraryName;
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
