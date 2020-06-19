package org.ishafoundation.dwaraapi.db.domain.factory;

import java.util.HashMap;
import java.util.Map;

import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;

public class DomainSpecificArtifactFactory {
	// https://medium.com/@kousiknath/design-patterns-different-approaches-to-use-factory-pattern-to-choose-objects-dynamically-at-run-71449bceecef
    private static final Map<String, Class<? extends Artifact>> instances = new HashMap<>();

    public static void register(String domainSpecificEntity, Class<? extends Artifact> instance) {
        if (domainSpecificEntity != null && instance != null) {
            instances.put(domainSpecificEntity, instance);
        }
    }

    public static Artifact getInstance(String domainSpecificEntity) {
        if (instances.containsKey(domainSpecificEntity)) {
        	Class<? extends Artifact> entityAsClass = instances.get(domainSpecificEntity);
        	
        	Artifact entity = null;
        	try {
				entity = entityAsClass.newInstance();
			} catch (Exception e) {
				// swallow it...
			}
        	return entity;
        }
        return null;
    }
}