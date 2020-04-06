package org.ishafoundation.dwaraapi.process.factory;

import java.util.HashMap;
import java.util.Map;

import org.ishafoundation.dwaraapi.process.thread.task.ITasktypeExecutor;
import org.springframework.context.ApplicationContext;

public class TasktypeFactory {
    private static final Map<String, Class<? extends ITasktypeExecutor>> instances = new HashMap<>();

    public static void register(String tasktypeName, Class<? extends ITasktypeExecutor> instance) {
        if (tasktypeName != null && instance != null) {
            instances.put(tasktypeName, instance);
        }
    }

    public static ITasktypeExecutor getInstance(ApplicationContext context, String tasktypeName) {
        if (instances.containsKey(tasktypeName)) {
            return context.getBean(instances.get(tasktypeName));
        }
        return null;
    }
}