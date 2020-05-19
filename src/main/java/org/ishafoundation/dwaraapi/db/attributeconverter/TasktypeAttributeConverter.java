package org.ishafoundation.dwaraapi.db.attributeconverter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.enumreferences.Tasktype;

@Converter(autoApply = true)
public class TasktypeAttributeConverter implements AttributeConverter<Tasktype, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Tasktype attribute) {
		if (attribute == null)
			return null;

		switch (attribute) {
		case storage:
			return 1;
		case processing:
			return 2;
		default:
			throw new IllegalArgumentException(attribute + " not supported.");
		}
	}

	@Override
	public Tasktype convertToEntityAttribute(Integer dbData) {
		if (dbData == null)
			return null;

		switch (dbData) {
		case 1:
			return Tasktype.storage;
		case 2:
			return Tasktype.processing;
		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}