package org.ishafoundation.dwaraapi.enumreferences;

/* 
 * This enum is just a convenience class
 * NOTE: Though Domain is configurable in DB table, the system needs to have as many configured domains specific implementations to work... 
 * Right now by default we just support two... If more domains are needed please refer DomainUtil for the list of entity objects that need to be created... 
 * The domain names in this enum might differ with whats configured in the table but thats ok as its converted to the domain ids. Pls refer DomainAttributeConverter
 */ 
public enum Domain {
	ONE,
	TWO
}
