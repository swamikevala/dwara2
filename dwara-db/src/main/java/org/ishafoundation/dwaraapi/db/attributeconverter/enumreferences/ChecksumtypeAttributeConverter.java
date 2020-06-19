package org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;

@Converter(autoApply = true)
public class ChecksumtypeAttributeConverter implements AttributeConverter<Checksumtype, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Checksumtype attribute) {
		if (attribute == null)
			return null;

		switch (attribute) {
		case sha256:
			return 1;
		default:
			throw new IllegalArgumentException(attribute + " not supported.");
		}
	}

	@Override
	public Checksumtype convertToEntityAttribute(Integer dbData) {
		if (dbData == null)
			return null;

		switch (dbData) {
		case 1:
			return Checksumtype.sha256;
		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}