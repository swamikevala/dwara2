package org.ishafoundation.dwaraapi.utils;

import java.util.List;

import org.ishafoundation.dwaraapi.enumreferences.Status;

public class StatusUtil {
	
	public static Status getStatus(List<Status> entityStatusList) {
		boolean anyInProgress = false;
		boolean anyQueued = false;
		boolean anyOnHold = false;
		boolean anyCancelled = false;
		boolean anyCompletedWithFailures = false;
		boolean anyMarkedFailed = false;
		boolean hasFailures = false;
		boolean anyMarkedCompleted = false;
		boolean isAllComplete = true;
					
		for (Status status : entityStatusList) {
			switch (status) {
				case in_progress:
					anyInProgress = true;
					isAllComplete = false;
					break;
				case queued:
					anyQueued = true;
					isAllComplete = false;
					break;
				case on_hold:
					anyOnHold = true;
					isAllComplete = false;
					break;
				case cancelled:
					anyCancelled = true;
					isAllComplete = false;
					break;
				case completed_failures:
					anyCompletedWithFailures = true;
					isAllComplete = false;
					break;
				case marked_failed:	
					anyMarkedFailed = true;
					isAllComplete = false;
					break;
				case failed:
					hasFailures = true;
					isAllComplete = false;
					break;
				case marked_completed:
					anyMarkedCompleted = true;
					isAllComplete = false;
					break;
				case completed:
					break;						
				default:
					break;
			}
		}
		
		/**
		 * 
		 * in_progress
			queued
			on_hold
			cancelled
			failed
			marked_failed
			completed_failures
			marked_completed
			completed
			*/
		Status status = Status.queued;
		if(anyInProgress) {
			status = Status.in_progress;
		}
		else if(anyQueued) {
			status = Status.queued; 
		}
		else if(anyOnHold) {
			status = Status.on_hold; 
		}
		else if(anyCancelled) {
			status = Status.cancelled;
		}
		else if(hasFailures) {
			status = Status.failed;
		}
		else if(anyMarkedFailed) {
			status = Status.marked_failed;
		}
		else if(anyCompletedWithFailures) {
			status = Status.completed_failures; 
		}
		else if(anyMarkedCompleted) {
			status = Status.marked_completed;
		}
		else if(isAllComplete) { // All jobs have successfully completed.
			status = Status.completed; 
		}
		
		return status;
	}


}
