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
		case one:
			return "1";
		case two:
			return "2";
		case three:
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
			return Domain.one;
		case "2":
			return Domain.two;
		case "3":
			return Domain.three;

		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}