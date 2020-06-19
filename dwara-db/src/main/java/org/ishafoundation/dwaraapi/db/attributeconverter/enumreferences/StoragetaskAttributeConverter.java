package org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.enumreferences.Storagetask;
import org.springframework.stereotype.Component;

@Component
@Converter(autoApply = true)
public class StoragetaskAttributeConverter implements AttributeConverter<Storagetask, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Storagetask attribute) {
		if (attribute == null)
			return null;

		switch (attribute) {
		case write:
			return 1;
		case restore:
			return 2;
		case verify:
			return 3;
		case rewrite:
			return 4;
		case migrate:
			return 5;
		case format:
			return 6;
		case finalize:
			return 7;
		case import_:
			return 8;
		case map_tapedrives:
			return 9;
		default:
			throw new IllegalArgumentException(attribute + " not supported.");
		}
	}

	@Override
	public Storagetask convertToEntityAttribute(Integer dbData) {
		if (dbData == null)
			return null;

		switch (dbData) {
		case 1:
			return Storagetask.write;
		case 2:
			return Storagetask.restore;
		case 3:
			return Storagetask.verify;
		case 4:
			return Storagetask.rewrite;
		case 5:
			return Storagetask.migrate;
		case 6:
			return Storagetask.format;
		case 7:
			return Storagetask.finalize;
		case 8:
			return Storagetask.import_;
		case 9:
			return Storagetask.map_tapedrives;
		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}