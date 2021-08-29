/*
 * package org.ishafoundation.dwaraapi.db.domain.factory;
 * 
 * import java.util.HashMap; import java.util.Map;
 * 
 * import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.
 * DomainAttributeConverter; import
 * org.ishafoundation.dwaraapi.db.model.transactional.Volume; import
 * org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.
 * ArtifactVolume; import org.ishafoundation.dwaraapi.enumreferences.Domain;
 * 
 * public class DomainSpecificArtifactVolumeFactory { //
 * https://medium.com/@kousiknath/design-patterns-different-approaches-to-use-
 * factory-pattern-to-choose-objects-dynamically-at-run-71449bceecef private
 * static final Map<String, Class<? extends ArtifactVolume>> instances = new
 * HashMap<>();
 * 
 * public static void register(String domainSpecificEntity, Class<? extends
 * ArtifactVolume> instance) { if (domainSpecificEntity != null && instance !=
 * null) { instances.put(domainSpecificEntity, instance); } }
 * 
 * public static ArtifactVolume getInstance(Domain domain, int artifactId,
 * Volume volume) { DomainAttributeConverter domainAttributeConverter = new
 * DomainAttributeConverter(); Integer domainId =
 * domainAttributeConverter.convertToDatabaseColumn(domain); String
 * domainSpecificArtifactVolumeTableName =
 * ArtifactVolume.TABLE_NAME.replace("<<DOMAIN>>", domainId+"");
 * 
 * if (instances.containsKey(domainSpecificArtifactVolumeTableName)) { Class<?
 * extends ArtifactVolume> entityAsClass =
 * instances.get(domainSpecificArtifactVolumeTableName);
 * 
 * ArtifactVolume entity = null; try { Class[] cArg = new Class[2]; //Our
 * constructor has 3 arguments cArg[0] = int.class; //First argument is of
 * *primitive* type int cArg[1] = Volume.class; //Second argument is of *object*
 * type Volume
 * 
 * entity = entityAsClass.getDeclaredConstructor(cArg).newInstance(artifactId,
 * volume); } catch (Exception e) { // swallow it... e.printStackTrace(); }
 * return entity; } return null; } }
 */