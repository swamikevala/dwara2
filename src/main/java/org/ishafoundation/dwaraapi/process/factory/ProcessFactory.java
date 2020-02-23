package org.ishafoundation.dwaraapi.process.factory;

import java.util.HashMap;
import java.util.Map;

import org.ishafoundation.dwaraapi.process.thread.task.IProcessor;
import org.springframework.context.ApplicationContext;

public class ProcessFactory {
	// https://medium.com/@kousiknath/design-patterns-different-approaches-to-use-factory-pattern-to-choose-objects-dynamically-at-run-71449bceecef
//    private static final Map<String, Class<? extends Process_ThreadTask>> instances = new HashMap<>();
//
//    public static void register(String processName, Class<? extends Process_ThreadTask> instance) {
//        if (processName != null && instance != null) {
//            instances.put(processName, instance);
//        }
//    }
//
//    public static Process_ThreadTask getInstance(ApplicationContext context, String processName) {
//        if (instances.containsKey(processName)) {
//            return context.getBean(instances.get(processName));
//        }
//        return null;
//    }
	
    private static final Map<String, Class<? extends IProcessor>> instances = new HashMap<>();

    public static void register(String processName, Class<? extends IProcessor> instance) {
        if (processName != null && instance != null) {
            instances.put(processName, instance);
        }
    }

    public static IProcessor getInstance(ApplicationContext context, String processName) {
        if (instances.containsKey(processName)) {
            return context.getBean(instances.get(processName));
        }
        return null;
    }
}