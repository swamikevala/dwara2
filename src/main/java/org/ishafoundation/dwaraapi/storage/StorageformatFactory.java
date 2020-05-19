package org.ishafoundation.dwaraapi.storage;

import java.util.HashMap;
import java.util.Map;

import org.ishafoundation.dwaraapi.storage.storageformat.AbstractStorageformatArchiver;
import org.springframework.context.ApplicationContext;

public class StorageformatFactory {
	// https://medium.com/@kousiknath/design-patterns-different-approaches-to-use-factory-pattern-to-choose-objects-dynamically-at-run-71449bceecef
    private static final Map<String, Class<? extends AbstractStorageformatArchiver>> instances = new HashMap<>();

    public static void register(String format, Class<? extends AbstractStorageformatArchiver> instance) {
        if (format != null && instance != null) {
            instances.put(format, instance);
        }
    }

    public static AbstractStorageformatArchiver getInstance(ApplicationContext context, String format) {
        if (instances.containsKey(format)) {
            return context.getBean(instances.get(format));
        }
        return null;
    }
}