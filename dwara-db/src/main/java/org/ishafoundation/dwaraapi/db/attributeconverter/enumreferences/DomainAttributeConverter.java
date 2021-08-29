/*
 * package org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences;
 * 
 * import javax.persistence.AttributeConverter; import
 * javax.persistence.Converter;
 * 
 * import org.ishafoundation.dwaraapi.enumreferences.Domain; import
 * org.springframework.stereotype.Component;
 * 
 * @Component
 * 
 * @Converter(autoApply = true) public class DomainAttributeConverter implements
 * AttributeConverter<Domain, Integer> {
 * 
 * @Override public Integer convertToDatabaseColumn(Domain attribute) { if
 * (attribute == null) return null;
 * 
 * switch (attribute) { case ONE: return 1; case TWO: return 2;
 * 
 * default: throw new IllegalArgumentException(attribute + " not supported."); }
 * }
 * 
 * @Override public Domain convertToEntityAttribute(Integer dbData) { if (dbData
 * == null) return null;
 * 
 * switch (dbData) { case 1: return Domain.ONE; case 2: return Domain.TWO;
 * default: throw new IllegalArgumentException(dbData + " not supported."); } }
 * 
 * }
 */