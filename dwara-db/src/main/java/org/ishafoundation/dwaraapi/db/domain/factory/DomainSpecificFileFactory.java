package org.ishafoundation.dwaraapi.db.domain.factory;

import java.util.HashMap;
import java.util.Map;

import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.DomainAttributeConverter;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.enumreferences.Domain;

public class DomainSpecificFileFactory {
	// https://medium.com/@kousiknath/design-patterns-different-approaches-to-use-factory-pattern-to-choose-objects-dynamically-at-run-71449bceecef
    private static final Map<String, Class<? extends File>> instances = new HashMap<>();

    public static void register(String domainSpecificEntity, Class<? extends File> instance) {
        if (domainSpecificEntity != null && instance != null) {
            instances.put(domainSpecificEntity, instance);
        }
    }

    public static File getInstance(Domain domain) {
    	DomainAttributeConverter domainAttributeConverter = new DomainAttributeConverter();
		Integer domainId = domainAttributeConverter.convertToDatabaseColumn(domain);
		String domainSpecificFileTableName = File.TABLE_NAME_PREFIX + domainId;

        if (instances.containsKey(domainSpecificFileTableName)) {
        	Class<? extends File> entityAsClass = instances.get(domainSpecificFileTableName);
        	
        	File entity = null;
        	try {
				entity = entityAsClass.getConstructor().newInstance();
			} catch (Exception e) {
				// swallow it...
			}
        	return entity;
        }
        return null;
    }
}