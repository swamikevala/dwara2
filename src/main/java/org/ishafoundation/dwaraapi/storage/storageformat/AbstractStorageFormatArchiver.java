package org.ishafoundation.dwaraapi.storage.storageformat;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storageformat.bru.response.components.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractStorageFormatArchiver {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractStorageFormatArchiver.class);
	
	public ArchiveResponse write(StorageJob storageJob){
		// TODO
		String librarypathToBeCopied = storageJob.getLibrarypathToBeCopied();
		logger.trace("will be writing" + librarypathToBeCopied);
		// dbUpdates
		//ArchiveResponse ar = archive();
		archive();
		
		// TODO - this should come from the specific archivers...
		ArchiveResponse ar = new ArchiveResponse();
		List<File> fileList = new ArrayList<File>();
		
		File file1 = new File();
		file1.setFileName("99999_Shivanga-Ladies_Sharing_English_Avinashi_10-Dec-2017_Panasonic-AG90A\\1 CD\\DJI_0001.MOV");
		file1.setBlockNumber(100);
		fileList.add(file1);
		
		File file2 = new File();
		file2.setFileName("99999_Shivanga-Ladies_Sharing_English_Avinashi_10-Dec-2017_Panasonic-AG90A\\2 CD\\IMG_0277.MOV");
		file2.setBlockNumber(200);
		fileList.add(file2);
		
		File file3 = new File();
		file3.setFileName("99999_Shivanga-Ladies_Sharing_English_Avinashi_10-Dec-2017_Panasonic-AG90A\\2 CD\\Sadhguru Playing Cricket in Chennai.mp4");
		file3.setBlockNumber(300);
		fileList.add(file3);
		
		ar.setFileList(fileList);
		// dbupdates using the ArchiveResponse recd..
		return ar;
	}
	
	public ArchiveResponse read(StorageJob archiveJob){
		// dbUpdates
		ArchiveResponse ar = restore();
		// dbupdates
		return ar;
	}	
	protected abstract ArchiveResponse archive();
	protected abstract ArchiveResponse restore();
}
