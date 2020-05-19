package org.ishafoundation.dwaraapi.enumreferences;

/**
 * type of Request/Actions that can be performed in the system... - 
 * We use this strategy to map this enum with the DB https://thoughts-on-java.org/hibernate-enum-mappings/#customizedMapping
 * 
 * if this enum is changed please ensure ActionAttributeConverter reflects the change too.
 */
public enum Action {
	ingest,
	rerun,
	restore,
	list,
	rename,
	hold,
	release,
	cancel,
	abort,
	delete,
	rewrite,
	format_tape,
	finalize_tape,
	import_tape,
	migrate_tape,
	map_tapedrives,
	diagnostics
}
