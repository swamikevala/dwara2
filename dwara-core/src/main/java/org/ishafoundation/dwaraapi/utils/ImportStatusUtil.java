package org.ishafoundation.dwaraapi.utils;

import java.util.List;

import org.ishafoundation.dwaraapi.enumreferences.ImportStatus;

public class ImportStatusUtil {
	
	public static ImportStatus getStatus(List<ImportStatus> entityStatusList) {
		boolean hasFailures = false;
		boolean isAllSkipped = true;
					
		for (ImportStatus status : entityStatusList) {
			switch (status) {
				case failed:
					hasFailures = true;
					isAllSkipped = false;
					break;
				case completed:
					isAllSkipped = false;
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
		ImportStatus status = ImportStatus.completed;
		if(hasFailures) {
//			if(hasAnyCompleted)
//				status = ImportStatus.completed_failures;
//			else
				status = ImportStatus.failed;
		}
		else if(isAllSkipped) {
			status = ImportStatus.skipped;
		}

		return status;
	}


}
