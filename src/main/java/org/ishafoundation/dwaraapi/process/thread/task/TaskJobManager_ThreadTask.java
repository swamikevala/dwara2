package org.ishafoundation.dwaraapi.process.thread.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.ishafoundation.dwaraapi.DwaraApiApplication;
import org.ishafoundation.dwaraapi.db.dao.master.LibraryclassDao;
import org.ishafoundation.dwaraapi.db.dao.master.SequenceDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.Task;
import org.ishafoundation.dwaraapi.db.model.master.Taskfiletype;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ExtensionTaskfiletype;
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

//		is tasktype filelevel?
//			what does it mean?
		
//		has dependent tasks?
//			means outputlibraryclass is needed
			
//		has a prerequisite task?
//			means ouputlibrary of the prerequisite task is the input of the task

@Component
@Scope("prototype")
public class TaskJobManager_ThreadTask implements Runnable{

	private static final Logger logger = LoggerFactory.getLogger(TaskJobManager_ThreadTask.class);
	
	
	@Autowired
    private LibraryclassDao libraryclassDao;
	
	@Autowired
	private SequenceDao sequenceDao;
	
	@Autowired
    private FileDao fileDao;

	@Autowired
	private	LogicalFileHelper fileHelper;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	

	
	private Job job;

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	@Override
    public void run() {
		logger.trace("managing tasktype job - " + job.getId());
		Library library = job.getInputLibrary();
		String libraryName = library.getName();
		Libraryclass libraryclass = library.getLibraryclass();
		String inputLibraryPath = libraryclass.getPath() + File.separator + libraryName;

		// For the task getting processed check
		// 1) if there are any dependent tasks 
		// 2) and is responsible for generating a libraryclass
		
		// then it means the output of the current task is the input for the dependent task
		
		
		String outputLibraryName = null;
		String outputLibraryPathname = null; // holds where to generate the files in the physical system...
		Task task = job.getTask();
		int taskId = task.getId();
		Libraryclass outputLibraryclass = libraryclassDao.findByGeneratorTaskId(taskId); //  the task output's resultant libraryclass

		if(outputLibraryclass != null) {
			outputLibraryName = getOutputLibraryName(outputLibraryclass, libraryName);
			outputLibraryPathname = getOutputLibraryPathname(outputLibraryclass, outputLibraryName);
		}
		
		HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.File> filePathToFileObj = getFilePathToFileObj(library.getId());
		
		Collection<LogicalFile> selectedFileList = getLogicalFileList(libraryclass, inputLibraryPath);
		
		for (Iterator<LogicalFile> iterator = selectedFileList.iterator(); iterator.hasNext();) {
			LogicalFile logicalFile = (LogicalFile) iterator.next(); // would have an absolute file like C:\data\ingested\14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A\1 CD\00018.MTS and its sidecar files
			logger.info("Now kicking off - " + job.getId() + " " + logicalFile.getAbsolutePath() + " task " + taskId);
			// TODO - Need to work on this for Audio where its just file..
			String x = FilenameUtils.getFullPath(logicalFile.getAbsolutePath()) + FilenameUtils.getBaseName(logicalFile.getAbsolutePath());
			if(logicalFile.getAbsolutePath().equals(x)) { 
				// means input library is a file and not a directory
				
			}
			String filePathnameWithoutLibraryNamePrefixed = logicalFile.getAbsolutePath().replace(inputLibraryPath + File.separator, ""); // would hold 1 CD\00018.MTS
			String libraryNamePrefixedFilePathname = logicalFile.getAbsolutePath().replace(inputLibraryPath + File.separator, libraryName + File.separator); // would hold 14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A\1 CD\00018.MTS
			// TODO
//			if(path.contains(configuration.getJunkFilesStagedDirName())) // skipping junk files
//				continue;			
			
			//logger.info("Now processing - " + path);
			org.ishafoundation.dwaraapi.db.model.transactional.File file = null;
			if(filePathToFileObj.containsKey(libraryNamePrefixedFilePathname))
				file = filePathToFileObj.get(libraryNamePrefixedFilePathname);
			
			String outputFilePath = null;
			if(outputLibraryPathname != null)
				outputFilePath = outputLibraryPathname + File.separator + FilenameUtils.getFullPathNoEndSeparator(filePathnameWithoutLibraryNamePrefixed); 

			Task_ThreadTask tasktype = applicationContext.getBean(Task_ThreadTask.class);
			tasktype.setJob(job);
			tasktype.setFile(file);
			tasktype.setLogicalFile(logicalFile);
			
			tasktype.setOutputLibraryName(outputLibraryName);
			tasktype.setOutputLibraryPathname(outputLibraryPathname);
			tasktype.setDestinationDirPath(outputFilePath);
			
			Executor executor = DwaraApiApplication.taskName_executor_map.get(task.getName().toLowerCase());
			executor.execute(tasktype);
		}
		// TODO if no. of errors in the tasktype reach the configured max_errors threshold then we stop further processing.... count(*) on failures for the job_id...


	}


	private String getOutputLibraryName(Libraryclass outputLibraryclass, String inputLibraryName){
		int outputLibraryClassSequenceId = outputLibraryclass.getSequenceId();
		String outputLibraryNamePrefix = sequenceDao.findById(outputLibraryClassSequenceId).get().getPrefix();
		return outputLibraryNamePrefix + inputLibraryName;
	}

	private String getOutputLibraryPathname(Libraryclass outputLibraryclass, String outputLibraryName) {
		return outputLibraryclass.getPath() + java.io.File.separator + outputLibraryName;
	}
	
	private HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.File> getFilePathToFileObj(int libraryId) {
		HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.File> filePathTofileObj = new HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.File>();
		
		List<org.ishafoundation.dwaraapi.db.model.transactional.File> libraryFileList = fileDao.findAllByLibraryId(libraryId);
		for (Iterator<org.ishafoundation.dwaraapi.db.model.transactional.File> iterator = libraryFileList.iterator(); iterator.hasNext();) {
			org.ishafoundation.dwaraapi.db.model.transactional.File nthFile = iterator.next();
			filePathTofileObj.put(nthFile.getPathname(), nthFile);
		}
		return filePathTofileObj;
	}
	
	private Collection<LogicalFile> getLogicalFileList(Libraryclass libraryclass, String inputLibraryPath){
		Taskfiletype taskfiletype = libraryclass.getTaskfiletype();
		
		List<ExtensionTaskfiletype> extn_Taskfiletype_List = taskfiletype.getExtensions(); //extensionTaskfiletypeDao.findAllByTaskfiletypeId(taskfiletype.getId());
		
		List<String> extensions = new ArrayList<String>();
		List<String> sidecarExtensions = new ArrayList<String>();
		boolean includeSidecarFiles = false;
		for (ExtensionTaskfiletype extensionTaskfiletype : extn_Taskfiletype_List) {
			// TODO - do i need to filter out on the taskfiletype or will the join automatically filter it based on the taskfiletype
			String extensionName = extensionTaskfiletype.getExtension().getName();
			
			if(extensionTaskfiletype.isSidecar()) {
				sidecarExtensions.add(extensionName);
				includeSidecarFiles = true;
			}
			else
				extensions.add(extensionName);
		}
		String[] extensionsArray = ArrayUtils.toStringArray(extensions.toArray());
		String[] sidecarExtensionsArray = ArrayUtils.toStringArray(sidecarExtensions.toArray());
		
		
		return fileHelper.getFiles(inputLibraryPath, extensionsArray, includeSidecarFiles, sidecarExtensionsArray);
	}
}
