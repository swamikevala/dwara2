/*
 * package org.ishafoundation.dwaraapi.db.domain.factory;
 * 
 * import java.util.HashMap; import java.util.Map;
 * 
 * import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.
 * DomainAttributeConverter; import
 * org.ishafoundation.dwaraapi.db.model.transactional.Volume; import
 * org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.
 * FileVolume; import org.ishafoundation.dwaraapi.enumreferences.Domain;
 * 
 * public class DomainSpecificFileVolumeFactory { //
 * https://medium.com/@kousiknath/design-patterns-different-approaches-to-use-
 * factory-pattern-to-choose-objects-dynamically-at-run-71449bceecef private
 * static final Map<String, Class<? extends FileVolume>> instances = new
 * HashMap<>();
 * 
 * public static void register(String domainSpecificEntity, Class<? extends
 * FileVolume> instance) { if (domainSpecificEntity != null && instance != null)
 * { instances.put(domainSpecificEntity, instance); } }
 * 
 * public static FileVolume getInstance(Domain domain, int fileId, Volume
 * volume) { DomainAttributeConverter domainAttributeConverter = new
 * DomainAttributeConverter(); Integer domainId =
 * domainAttributeConverter.convertToDatabaseColumn(domain); String
 * domainSpecificFileVolumeTableName =
 * FileVolume.TABLE_NAME.replace("<<DOMAIN>>", domainId+"");
 * 
 * if (instances.containsKey(domainSpecificFileVolumeTableName)) { Class<?
 * extends FileVolume> entityAsClass =
 * instances.get(domainSpecificFileVolumeTableName);
 * 
 * FileVolume entity = null; try { Class[] cArg = new Class[2]; //Our
 * constructor has 3 arguments cArg[0] = int.class; //First argument is of
 * *primitive* type int cArg[1] = Volume.class; //Second argument is of *object*
 * type Volume
 * 
 * entity = entityAsClass.getDeclaredConstructor(cArg).newInstance(fileId,
 * volume); } catch (Exception e) { // swallow it... e.printStackTrace(); }
 * return entity; } return null; } }
 */