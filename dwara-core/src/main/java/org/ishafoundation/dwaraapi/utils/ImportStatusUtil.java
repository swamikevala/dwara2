package org.ishafoundation.dwaraapi.utils;

import java.util.List;

import org.ishafoundation.dwaraapi.enumreferences.ImportStatus;

public class ImportStatusUtil {
	
	public static ImportStatus getStatus(List<ImportStatus> entityStatusList) {
		boolean hasFailures = false;
		boolean isAllSkipped = true;
		boolean isAllComplete = true;
					
		for (ImportStatus status : entityStatusList) {
			switch (status) {
				case failed:
					hasFailures = true;
					isAllSkipped = false;
					isAllComplete = false;
					break;
				default:
					break;
			}
		}
		
		/**
		 * 
			failed
			completed
			skipped
			*/
		ImportStatus status = ImportStatus.skipped;
		if(hasFailures) {
//			if(hasAnyCompleted)
//				status = ImportStatus.completed_failures;
//			else
				status = ImportStatus.failed;
		}
		else if(isAllSkipped) {
			status = ImportStatus.skipped;
		}
		else if(isAllComplete) { 
			status = ImportStatus.completed; 
		}
		return status;
	}


}
