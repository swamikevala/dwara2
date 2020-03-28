package org.ishafoundation.dwaraapi.constants;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class RequesttypeAttributeConverter implements AttributeConverter<Requesttype, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Requesttype attribute) {
		if (attribute == null)
			return null;

		switch (attribute) {
		case ingest:
			return 1;
		case restore:
			return 2;
		case scan:
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
		case diagnostics:
			return 12;

		default:
			throw new IllegalArgumentException(attribute + " not supported.");
		}
	}

	@Override
	public Requesttype convertToEntityAttribute(Integer dbData) {
		if (dbData == null)
			return null;

		switch (dbData) {
		case 1:
			return Requesttype.ingest;
		case 2:
			return Requesttype.restore;
		case 3:
			return Requesttype.scan;
		case 4:
			return Requesttype.list;
		case 5:
			return Requesttype.rename;
		case 6:
			return Requesttype.hold;
		case 7:
			return Requesttype.release;
		case 8:
			return Requesttype.cancel;
		case 9:
			return Requesttype.abort;
		case 10:
			return Requesttype.delete;
		case 11:
			return Requesttype.rewrite;
		case 12:
			return Requesttype.diagnostics;

		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}