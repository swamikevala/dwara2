package org.ishafoundation.dwaraapi.db.attributeconverter;

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
		case verify_crc:
			return 3;
		case verify:
			return 4;
		case archive:
			return 5;
		case rewrite:
			return 6;
		case format_tape:
			return 7;
		case finalize_tape:
			return 8;
		case import_tape:
			return 9;
		case migrate_tape:
			return 10;
		case map_tapedrives:
			return 11;
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
			return Storagetask.verify_crc;
		case 4:
			return Storagetask.verify;
		case 5:
			return Storagetask.archive;
		case 6:
			return Storagetask.rewrite;
		case 7:
			return Storagetask.format_tape;
		case 8:
			return Storagetask.finalize_tape;
		case 9:
			return Storagetask.import_tape;
		case 10:
			return Storagetask.migrate_tape;
		case 11:
			return Storagetask.map_tapedrives;
		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}