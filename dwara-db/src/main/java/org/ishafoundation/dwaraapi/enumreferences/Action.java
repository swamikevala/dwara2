package org.ishafoundation.dwaraapi.enumreferences;

/**
 * type of Request/Actions that can be performed in the system... - 
 * We use this strategy to map this enum with the DB https://thoughts-on-java.org/hibernate-enum-mappings/#customizedMapping
 * 
 * if this enum is changed please ensure ActionAttributeConverter reflects the change too.
 */
public enum Action {
	rename_staged,
	ingest,
	write,
	restore,
	verify,
	list,
	rename,
	hold,
	release,
	cancel,
	abort,
	delete,
	rewrite,
	migrate,
	process,
	restore_process,
	initialize,
	finalize,
	import_,
	map_tapedrives,
	diagnostics
}
