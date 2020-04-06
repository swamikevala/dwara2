package org.ishafoundation.dwaraapi.process.factory;

import java.util.HashMap;
import java.util.Map;

import org.ishafoundation.dwaraapi.process.thread.task.ITaskExecutor;
import org.springframework.context.ApplicationContext;

public class TaskFactory {
    private static final Map<String, Class<? extends ITaskExecutor>> instances = new HashMap<>();

    public static void register(String taskName, Class<? extends ITaskExecutor> instance) {
        if (taskName != null && instance != null) {
            instances.put(taskName, instance);
        }
    }

    public static ITaskExecutor getInstance(ApplicationContext context, String taskName) {
        if (instances.containsKey(taskName)) {
            return context.getBean(instances.get(taskName));
        }
        return null;
    }
}