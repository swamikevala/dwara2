package org.ishafoundation.dwaraapi.constants;

/**
 * Processing Status - We use this strategy to map this enum with the DB https://thoughts-on-java.org/hibernate-enum-mappings/#customizedMapping
 */
public enum Requesttype {
	ingest,
	restore,
	scan,
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
