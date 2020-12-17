package org.ishafoundation.dwaraapi.status;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.junit.Test;

public class StatusUpdaterTests {
//
//	
//	private Status getSRStatus(List<Job> nthRequestJobs) {
//		
//		boolean anyQueued = false;
//		boolean anyInProgress = false;
//		boolean anyComplete = false;
//		boolean anySkipped = false;
//		boolean hasFailures = false;
//		boolean anyMarkedCompleted = false;
//		boolean isAllQueued = true;
//		boolean isAllComplete = true;
//		boolean isAllCancelled = true;
//					
//		for (Job nthJob : nthRequestJobs) {
//			Status status = nthJob.getStatus();
//			switch (status) {
//				case queued:
//					anyQueued = true;
//					isAllComplete = false;
//					isAllCancelled = false;
//					break;
//				case in_progress:
//					anyInProgress = true;
//					isAllQueued = false;
//					isAllComplete = false;
//					isAllCancelled = false;
//					break;
//				case completed:
//					anyComplete = true;
//					isAllQueued = false;
//					isAllCancelled = false;
//					break;						
//				case cancelled:
//					isAllQueued = false;
//					isAllComplete = false;
//					break;
//				case failed:
//					hasFailures = true;
//					isAllQueued = false;
//					isAllComplete = false;
//					isAllCancelled = false;						
//					break;
//				case completed_failures:
//				case marked_completed:
//					anyMarkedCompleted = true;
//					isAllQueued = false;
//					isAllComplete = false;
//					isAllCancelled = false;						
//					break;
//				default:
//					break;
//			}
//		}
//
//		
//		Status status = Status.queued;
//		if(isAllQueued) {
//			status = Status.queued; 
//		}
//		else if(isAllCancelled) {
//			status = Status.cancelled;
//		}
//		else if(isAllComplete) { // All jobs have successfully completed.
//			status = Status.completed; 
//		}
//		else if(anyQueued || anyInProgress) {
//			status = Status.in_progress;
//		}
//		else if(hasFailures) {
//			status = Status.failed;
//		}
//		else if(anyComplete && anyMarkedCompleted) { // Some jobs have successfully completed, and some were skipped, or failed and then marked completed.
//			status = Status.partially_completed; 
//		}
//
//		System.out.println("status " + status);
//		return status;
//	}
//
//	@Test
//	public void test_inprogress_1() {  // Some jobs are queued, and none are in progress
//		System.out.println("test_inprogress_1");
//		List<Job> jobList = new ArrayList<Job>();
//		
//		Job job1 = new Job();
//		job1.setStatus(Status.queued);
//		jobList.add(job1);
//		
//		Job job2 = new Job();
//		job2.setStatus(Status.completed);
//		jobList.add(job2);
//		
//		Job job3 = new Job();
//		job3.setStatus(Status.partially_completed);
//		jobList.add(job3);
//		
//		
//		Status status = getSRStatus(jobList);
//		assertEquals(status, Status.in_progress);
//	}
//	
//	@Test
//	public void test_inprogress_2() { // Some jobs are running
//		System.out.println("test_inprogress_2");
//		List<Job> jobList = new ArrayList<Job>();
//		
//		Job job1 = new Job();
//		job1.setStatus(Status.queued);
//		jobList.add(job1);
//		
//		Job job2 = new Job();
//		job2.setStatus(Status.in_progress);
//		jobList.add(job2);
//		
//		Job job3 = new Job();
//		job3.setStatus(Status.completed);
//		jobList.add(job3);
//		
//		Status status = getSRStatus(jobList);
//		assertEquals(status, Status.in_progress);
//	}
//	
//	@Test
//	public void test_completed() {
//		System.out.println("test_completed");
//		List<Job> jobList = new ArrayList<Job>();
//		
//		Job job1 = new Job();
//		job1.setStatus(Status.completed);
//		jobList.add(job1);
//		
//		Job job2 = new Job();
//		job2.setStatus(Status.completed);
//		jobList.add(job2);
//		
//		Job job3 = new Job();
//		job3.setStatus(Status.completed);
//		jobList.add(job3);
//		
//		Status status = getSRStatus(jobList);
//		assertEquals(status, Status.completed);
//	}
//	
//	@Test
//	public void test_cancelled() {
//		System.out.println("test_cancelled");
//		List<Job> jobList = new ArrayList<Job>();
//		
//		Job job1 = new Job();
//		job1.setStatus(Status.cancelled);
//		jobList.add(job1);
//		
//		Job job2 = new Job();
//		job2.setStatus(Status.cancelled);
//		jobList.add(job2);
//		
//		Job job3 = new Job();
//		job3.setStatus(Status.cancelled);
//		jobList.add(job3);
//		
//		Status status = getSRStatus(jobList);
//		assertEquals(status, Status.cancelled);
//	}
//	
//	@Test
//	public void test_partially_completed() {
//		System.out.println("test_partially_completed");
//		List<Job> jobList = new ArrayList<Job>();
//		
//		Job job1 = new Job();
//		job1.setStatus(Status.completed_failures);
//		jobList.add(job1);
//		
//		Job job2 = new Job();
//		job2.setStatus(Status.completed);
//		jobList.add(job2);
//		
//		Job job3 = new Job();
//		job3.setStatus(Status.completed);
//		jobList.add(job3);
//		
//		Status status = getSRStatus(jobList);
//		assertEquals(status, Status.partially_completed);
//	}
//	
//	@Test
//	public void test_failed() {
//		System.out.println("test_failed");
//		List<Job> jobList = new ArrayList<Job>();
//		
//		Job job1 = new Job();
//		job1.setStatus(Status.failed);
//		jobList.add(job1);
//		
//		Job job2 = new Job();
//		job2.setStatus(Status.completed);
//		jobList.add(job2);
//		
//		Job job3 = new Job();
//		job3.setStatus(Status.completed);
//		jobList.add(job3);
//		
//		Job job4 = new Job();
//		job4.setStatus(Status.skipped);
//		jobList.add(job4);
//		
//		Job job5 = new Job();
//		job5.setStatus(Status.marked_completed);
//		jobList.add(job5);
//		
//		Job job6 = new Job();
//		job6.setStatus(Status.partially_completed);
//		jobList.add(job6);
//		
//		Status status = getSRStatus(jobList);
//		assertEquals(status, Status.failed);
//	}
//
//	@Test
//	public void test() {
//		System.out.println("general_test");
//		List<Job> jobList = new ArrayList<Job>();
//		
//		Job job1 = new Job();
//		job1.setStatus(Status.failed);
//		jobList.add(job1);
//		
//		Job job2 = new Job();
//		job2.setStatus(Status.in_progress);
//		jobList.add(job2);
//		
//		Job job3 = new Job();
//		job3.setStatus(Status.completed);
//		jobList.add(job3);
//		
//		Job job4 = new Job();
//		job4.setStatus(Status.skipped);
//		jobList.add(job4);
//		
//		Job job5 = new Job();
//		job5.setStatus(Status.marked_completed);
//		jobList.add(job5);
//		
//		Job job6 = new Job();
//		job6.setStatus(Status.partially_completed);
//		jobList.add(job6);
//		
//		Job job7 = new Job();
//		job7.setStatus(Status.failed);
//		jobList.add(job7);
//		
//		Status status = getSRStatus(jobList);
//		System.out.println(status);
//	}
}
