package org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.enumreferences.Devicetype;

@Converter(autoApply = true)
public class DevicetypeAttributeConverter implements AttributeConverter<Devicetype, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Devicetype attribute) {
		if (attribute == null)
			return null;

		switch (attribute) {
		case tape_drive:
			return 1;
		case tape_autoloader:
			return 2;
		default:
			throw new IllegalArgumentException(attribute + " not supported.");
		}
	}

	@Override
	public Devicetype convertToEntityAttribute(Integer dbData) {
		if (dbData == null)
			return null;

		switch (dbData) {
		case 1:
			return Devicetype.tape_drive;
		case 2:
			return Devicetype.tape_autoloader;
		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}