package org.ishafoundation.dwaraapi.db.attributeconverter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.constants.Action;

@Converter(autoApply = true)
public class ActionAttributeConverter implements AttributeConverter<Action, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Action attribute) {
		if (attribute == null)
			return null;

		switch (attribute) {
		case ingest:
			return 8001;
		case restore:
			return 8002;
		case list:
			return 8003;
		case rename:
			return 8004;
		case hold:
			return 8005;
		case release:
			return 8006;
		case cancel:
			return 8007;
		case abort:
			return 8008;
		case delete:
			return 8009;
		case rewrite:
			return 8010;
		case diagnostics:
			return 8011;
		case tapedrivemapping:
			return 8012;
		case format:
			return 8013;			
		default:
			throw new IllegalArgumentException(attribute + " not supported.");
		}
	}

	@Override
	public Action convertToEntityAttribute(Integer dbData) {
		if (dbData == null)
			return null;

		switch (dbData) {
		case 8001:
			return Action.ingest;
		case 8002:
			return Action.restore;
		case 8003:
			return Action.list;
		case 8004:
			return Action.rename;
		case 8005:
			return Action.hold;
		case 8006:
			return Action.release;
		case 8007:
			return Action.cancel;
		case 8008:
			return Action.abort;
		case 8009:
			return Action.delete;
		case 8010:
			return Action.rewrite;
		case 8011:
			return Action.diagnostics;
		case 8012:
			return Action.tapedrivemapping;
		case 8013:
			return Action.format;

		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}