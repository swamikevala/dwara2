package org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.enumreferences.Storagetype;

@Converter(autoApply = true)
public class StoragetypeAttributeConverter implements AttributeConverter<Storagetype, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Storagetype attribute) {
		if (attribute == null)
			return null;

		switch (attribute) {
		case tape:
			return 1;
		case disk:
			return 2;
		case cloud:
			return 3;			
		default:
			throw new IllegalArgumentException(attribute + " not supported.");
		}
	}

	@Override
	public Storagetype convertToEntityAttribute(Integer dbData) {
		if (dbData == null)
			return null;

		switch (dbData) {
		case 1:
			return Storagetype.tape;
		case 2:
			return Storagetype.disk;
		case 3:
			return Storagetype.cloud;
		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}