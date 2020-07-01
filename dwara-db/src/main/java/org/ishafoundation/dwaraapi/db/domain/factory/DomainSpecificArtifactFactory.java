package org.ishafoundation.dwaraapi.db.domain.factory;

import java.util.HashMap;
import java.util.Map;

import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.DomainAttributeConverter;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.enumreferences.Domain;

public class DomainSpecificArtifactFactory {
	// https://medium.com/@kousiknath/design-patterns-different-approaches-to-use-factory-pattern-to-choose-objects-dynamically-at-run-71449bceecef
    private static final Map<String, Class<? extends Artifact>> instances = new HashMap<>();

    public static void register(String domainSpecificEntity, Class<? extends Artifact> instance) {
        if (domainSpecificEntity != null && instance != null) {
            instances.put(domainSpecificEntity, instance);
        }
    }

    public static Artifact getInstance(Domain domain) {
    	DomainAttributeConverter domainAttributeConverter = new DomainAttributeConverter();
		String domainAsString = domainAttributeConverter.convertToDatabaseColumn(domain);
		String domainSpecificArtifactTableName = Artifact.TABLE_NAME_PREFIX + domainAsString;

        if (instances.containsKey(domainSpecificArtifactTableName)) {
        	Class<? extends Artifact> entityAsClass = instances.get(domainSpecificArtifactTableName);
        	
        	Artifact entity = null;
        	try {
				//entity = entityAsClass.newInstance(); // https://stackoverflow.com/questions/195321/why-is-class-newinstance-evil
				entity = entityAsClass.getConstructor().newInstance();
			} catch (Exception e) {
				// swallow it...
			}
        	return entity;
        }
        return null;
    }
}