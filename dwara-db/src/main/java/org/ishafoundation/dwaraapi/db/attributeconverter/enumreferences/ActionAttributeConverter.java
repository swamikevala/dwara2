package org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences;

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
		case restore:
			return 2;
		case verify:
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
		case migrate:
			return 12;
		case process:
			return 13;
		case restore_process:
			return 14;
		case format:
			return 15;
		case finalize:
			return 16;
		case import_:
			return 17;
		case map_tapedrives:
			return 18;
		case diagnostics:
			return 19;
		case write:
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
			return Action.restore;
		case 3:
			return Action.verify;
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
			return Action.migrate;
		case 13:
			return Action.process;
		case 14:
			return Action.restore_process;
		case 15:
			return Action.format;
		case 16:
			return Action.finalize;
		case 17:
			return Action.import_;
		case 18:
			return Action.map_tapedrives;
		case 19:
			return Action.diagnostics;			
		case 20:
			return Action.write;			
		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}