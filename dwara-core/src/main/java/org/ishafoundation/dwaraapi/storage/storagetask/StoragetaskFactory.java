package org.ishafoundation.dwaraapi.storage.storagetask;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;

public class StoragetaskFactory {
	// https://medium.com/@kousiknath/design-patterns-different-approaches-to-use-factory-pattern-to-choose-objects-dynamically-at-run-71449bceecef
    private static final Map<String, Class<? extends AbstractStoragetask>> instances = new HashMap<>();

    public static void register(String storagetaskName, Class<? extends AbstractStoragetask> instance) {
        if (storagetaskName != null && instance != null) {
            instances.put(storagetaskName, instance);
        }
    }

    public static AbstractStoragetask getInstance(ApplicationContext context, String storagetaskName) {
        if (instances.containsKey(storagetaskName)) {
            return context.getBean(instances.get(storagetaskName));
        }
        return null;
    }
}