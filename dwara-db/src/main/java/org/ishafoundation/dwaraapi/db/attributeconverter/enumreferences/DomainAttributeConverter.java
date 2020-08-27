package org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.springframework.stereotype.Component;

@Component
@Converter(autoApply = true)
public class DomainAttributeConverter implements AttributeConverter<Domain, String> {

	@Override
	public String convertToDatabaseColumn(Domain attribute) {
		if (attribute == null)
			return null;

		switch (attribute) {
		case main:
			return "1";
		case other:
			return "2";
		case test:
			return "3";

		default:
			throw new IllegalArgumentException(attribute + " not supported.");
		}
	}

	@Override
	public Domain convertToEntityAttribute(String dbData) {
		if (dbData == null)
			return null;

		switch (dbData) {
		case "1":
			return Domain.main;
		case "2":
			return Domain.other;
		case "3":
			return Domain.test;

		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}