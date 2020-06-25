package org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.springframework.stereotype.Component;

@Component
@Converter(autoApply = true)
public class ActionAttributeConverter implements AttributeConverter<Action, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Action attribute) {
		if (attribute == null)
			return null;

		switch (attribute) {
		case ingest:
			return 1;
		case write:
			return 2;
		case restore:
			return 3;
		case verify:
			return 4;
		case list:
			return 5;
		case rename:
			return 6;
		case hold:
			return 7;
		case release:
			return 8;
		case cancel:
			return 9;
		case abort:
			return 10;
		case delete:
			return 11;
		case rewrite:
			return 12;
		case migrate:
			return 13;
		case process:
			return 14;
		case restore_process:
			return 15;
		case format:
			return 16;
		case finalize:
			return 17;
		case import_:
			return 18;
		case map_tapedrives:
			return 19;
		case diagnostics:
			return 20;
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
			return Action.write;
		case 3:
			return Action.restore;
		case 4:
			return Action.verify;
		case 5:
			return Action.list;
		case 6:
			return Action.rename;
		case 7:
			return Action.hold;
		case 8:
			return Action.release;
		case 9:
			return Action.cancel;
		case 10:
			return Action.abort;
		case 11:
			return Action.delete;
		case 12:
			return Action.rewrite;
		case 13:
			return Action.migrate;
		case 14:
			return Action.process;
		case 15:
			return Action.restore_process;
		case 16:
			return Action.format;
		case 17:
			return Action.finalize;
		case 18:
			return Action.import_;
		case 19:
			return Action.map_tapedrives;
		case 20:
			return Action.diagnostics;
		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}