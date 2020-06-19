package org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.enumreferences.Storagelevel;

@Converter(autoApply = true)
public class StoragelevelAttributeConverter implements AttributeConverter<Storagelevel, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Storagelevel attribute) {
		if (attribute == null)
			return null;

		switch (attribute) {
		case block:
			return 1;
		case file:
			return 2;
		case object:
			return 3;			
		default:
			throw new IllegalArgumentException(attribute + " not supported.");
		}
	}

	@Override
	public Storagelevel convertToEntityAttribute(Integer dbData) {
		if (dbData == null)
			return null;

		switch (dbData) {
		case 1:
			return Storagelevel.block;
		case 2:
			return Storagelevel.file;
		case 3:
			return Storagelevel.object;
		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}