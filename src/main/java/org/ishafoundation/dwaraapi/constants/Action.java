package org.ishafoundation.dwaraapi.constants;

/**
 * type of Request - We use this strategy to map this enum with the DB https://thoughts-on-java.org/hibernate-enum-mappings/#customizedMapping
 */
public enum Action {
	ingest,
	restore,
	list,
	rename,
	hold,
	release,
	cancel,
	abort,
	delete,
	rewrite,
	diagnostics
}
