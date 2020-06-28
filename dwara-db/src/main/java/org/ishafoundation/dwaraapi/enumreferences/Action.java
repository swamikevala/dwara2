package org.ishafoundation.dwaraapi.enumreferences;

import java.util.HashMap;
import java.util.Map;

/**
 * type of Request/Actions that can be performed in the system... - 
 * We use this strategy to map this enum with the DB https://thoughts-on-java.org/hibernate-enum-mappings/#customizedMapping
 * 
 * if this enum is changed please ensure ActionAttributeConverter reflects the change too.
 */
public enum Action {
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
	format,
	finalize,
	import_,
	map_tapedrives,
	diagnostics

	
//	INGEST("ingest"),
//	WRITE("write"),
//	RESTORE("restore"),
//	VERIFY("verify"),
//	LIST("list"),
//	RENAME("rename"),
//	HOLD("hold"),
//	RELEASE("release"),
//	CANCEL("cancel"),
//	ABORT("abort"),
//	DELETE("delete"),
//	REWRITE("rewrite"),
//	MIGRATE("migrate"),
//	PROCESS("process"),
//	RESTORE_PROCESS("restore_process"),
//	FORMAT("format"),
//	FINALIZE("finalize"),
//	IMPORT("import"),
//	MAP_TAPEDRIVES("map_tapedrives"),
//	DIAGNOSTICS("diagnostics");
//	
//	private String dbAction; // action import in lowercase is not compatible in Java as its a keyword, but we need all actions in lowercase for DB and UI
//	
//	private Action(String actionLowercase) {
//		this.dbAction = actionLowercase;
//	}
//	
//	public String getActionForDb() {
//		return dbAction;
//	}
//	
//	// Caching the mapping
//    private static final Map<String, Action> VALUE_ACTION_MAP = new HashMap<>();
//    
//    static {
//        for (Action action: values()) {
//            VALUE_ACTION_MAP.put(action.dbAction, action);
//        }
//    }
//
// 
//    public static Action getAction(String actionLowercase) {
//        return VALUE_ACTION_MAP.get(actionLowercase);
//    }
}
