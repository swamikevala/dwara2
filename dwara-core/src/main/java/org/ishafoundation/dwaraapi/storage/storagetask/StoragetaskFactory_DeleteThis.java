package org.ishafoundation.dwaraapi.storage.storagetask;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;

public class StoragetaskFactory_DeleteThis {
	// https://medium.com/@kousiknath/design-patterns-different-approaches-to-use-factory-pattern-to-choose-objects-dynamically-at-run-71449bceecef
    private static final Map<String, Class<? extends AbstractStoragetaskAction>> instances = new HashMap<>();

    public static void register(String storagetaskName, Class<? extends AbstractStoragetaskAction> instance) {
        if (storagetaskName != null && instance != null) {
            instances.put(storagetaskName, instance);
        }
    }

    public static AbstractStoragetaskAction getInstance(ApplicationContext context, String storagetaskName) {
        if (instances.containsKey(storagetaskName)) {
            return context.getBean(instances.get(storagetaskName));
        }
        return null;
    }
}