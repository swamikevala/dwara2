package org.ishafoundation.dwaraapi.db.attributeconverter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.enumreferences.Action;

@Converter(autoApply = true)
public class ActionAttributeConverter implements AttributeConverter<Action, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Action attribute) {
		if (attribute == null)
			return null;

		switch (attribute) {
		case ingest:
			return 1;
		case rerun:
			return 2;
		case restore:
			return 3;
		case list:
			return 4;
		case rename:
			return 5;
		case hold:
			return 6;
		case release:
			return 7;
		case cancel:
			return 8;
		case abort:
			return 9;
		case delete:
			return 10;
		case rewrite:
			return 11;
		case format_tape:
			return 12;
		case finalize_tape:
			return 13;
		case import_tape:
			return 14;
		case migrate_tape:
			return 15;
		case map_tapedrives:
			return 16;
		case diagnostics:
			return 17;
		default:
			throw new IllegalArgumentException(attribute + " not supported.");
		}
	}

	@Override
	public Action convertToEntityAttribute(Integer dbData) {
		if (dbData == null)
			return null;

		switch (dbData) {
		case 1:
			return Action.ingest;
		case 2:
			return Action.rerun;
		case 3:
			return Action.restore;
		case 4:
			return Action.list;
		case 5:
			return Action.rename;
		case 6:
			return Action.hold;
		case 7:
			return Action.release;
		case 8:
			return Action.cancel;
		case 9:
			return Action.abort;
		case 10:
			return Action.delete;
		case 11:
			return Action.rewrite;
		case 12:
			return Action.format_tape;
		case 13:
			return Action.finalize_tape;
		case 14:
			return Action.import_tape;
		case 15:
			return Action.migrate_tape;
		case 16:
			return Action.map_tapedrives;
		case 17:
			return Action.diagnostics;
		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}