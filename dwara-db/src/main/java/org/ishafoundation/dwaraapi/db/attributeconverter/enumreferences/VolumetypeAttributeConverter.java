package org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.enumreferences.Volumetype;

@Converter(autoApply = true)
public class VolumetypeAttributeConverter implements AttributeConverter<Volumetype, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Volumetype attribute) {
		if (attribute == null)
			return null;

		switch (attribute) {
		case group:
			return 1;
		case physical:
			return 2;
		case provisioned:
			return 3;			
		default:
			throw new IllegalArgumentException(attribute + " not supported.");
		}
	}

	@Override
	public Volumetype convertToEntityAttribute(Integer dbData) {
		if (dbData == null)
			return null;

		switch (dbData) {
		case 1:
			return Volumetype.group;
		case 2:
			return Volumetype.physical;
		case 3:
			return Volumetype.provisioned;
		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}