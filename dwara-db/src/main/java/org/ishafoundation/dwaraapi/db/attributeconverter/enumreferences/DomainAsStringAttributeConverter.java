package org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.enumreferences.Domain;

public class DomainAsStringAttributeConverter implements AttributeConverter<Domain, String> {

	@Override
	public String convertToDatabaseColumn(Domain attribute) {
		if (attribute == null)
			return null;

		return attribute.name();
	}

	@Override
	public Domain convertToEntityAttribute(String dbData) {
		if (dbData == null)
			return null;

		return Domain.valueOf(dbData);
	}

}