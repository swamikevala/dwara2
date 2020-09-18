package org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.springframework.stereotype.Component;

@Component
@Converter(autoApply = true)
public class ActionAttributeConverter implements AttributeConverter<Action, String> {

	@Override
	public String convertToDatabaseColumn(Action attribute) {
		if (attribute == null)
			return null;

		switch (attribute) {
		case import_:
			return "import";
		
		/*
		case INGEST:
			return 1;
		case WRITE:
			return 2;
		case RESTORE:
			return 3;
		case VERIFY:
			return 4;
		case LIST:
			return 5;
		case RENAME:
			return 6;
		case HOLD:
			return 7;
		case RELEASE:
			return 8;
		case CANCEL:
			return 9;
		case ABORT:
			return 10;
		case DELETE:
			return 11;
		case REWRITE:
			return 12;
		case MIGRATE:
			return 13;
		case PROCESS:
			return 14;
		case RESTORE_PROCESS:
			return 15;
		case FORMAT:
			return 16;
		case FINALIZE:
			return 17;
		case IMPORT:
			return 18;
		case MAP_TAPEDRIVES:
			return 19;
		case DIAGNOSTICS:
			return 20;
			*/
		default:
			return attribute.name();
			//throw new IllegalArgumentException(attribute + " not supported.");
		}
	}

	@Override
	public Action convertToEntityAttribute(String dbData) {
		if (dbData == null)
			return null;

		switch (dbData) {
		case "import":
			return Action.import_;
			
			/*
		case 1:
			return Action.INGEST;
		case 2:
			return Action.WRITE;
		case 3:
			return Action.RESTORE;
		case 4:
			return Action.VERIFY;
		case 5:
			return Action.LIST;
		case 6:
			return Action.RENAME;
		case 7:
			return Action.HOLD;
		case 8:
			return Action.RELEASE;
		case 9:
			return Action.CANCEL;
		case 10:
			return Action.ABORT;
		case 11:
			return Action.DELETE;
		case 12:
			return Action.REWRITE;
		case 13:
			return Action.MIGRATE;
		case 14:
			return Action.PROCESS;
		case 15:
			return Action.RESTORE_PROCESS;
		case 16:
			return Action.FORMAT;
		case 17:
			return Action.FINALIZE;
		case 18:
			return Action.IMPORT;
		case 19:
			return Action.MAP_TAPEDRIVES;
		case 20:
			return Action.DIAGNOSTICS;
		*/
			
		default:
			return Action.valueOf(dbData);
			//throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}