package org.ishafoundation.dwaraapi.constants;

/**
 * Processing Status - We use this strategy to map this enum with the DB https://thoughts-on-java.org/hibernate-enum-mappings/#customizedMapping
 */
public enum Status {
	queued,
	in_progress,
	completed,
	partially_completed,
	completed_with_failures,
	on_hold,
	skipped,
	cancelled,
	aborted,
	failed,
	marked_completed
}
