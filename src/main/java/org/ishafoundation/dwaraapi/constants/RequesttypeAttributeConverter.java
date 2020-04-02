package org.ishafoundation.dwaraapi.constants;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class RequesttypeAttributeConverter implements AttributeConverter<Requesttype, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Requesttype attribute) {
		if (attribute == null)
			return null;

		switch (attribute) {
		case ingest:
			return 8001;
		case restore:
			return 8002;
		case scan:
			return 8003;
		case list:
			return 8004;
		case rename:
			return 8005;
		case hold:
			return 8006;
		case release:
			return 8007;
		case cancel:
			return 8008;
		case abort:
			return 8009;
		case delete:
			return 8010;
		case rewrite:
			return 8011;
		case diagnostics:
			return 8012;

		default:
			throw new IllegalArgumentException(attribute + " not supported.");
		}
	}

	@Override
	public Requesttype convertToEntityAttribute(Integer dbData) {
		if (dbData == null)
			return null;

		switch (dbData) {
		case 8001:
			return Requesttype.ingest;
		case 8002:
			return Requesttype.restore;
		case 8003:
			return Requesttype.scan;
		case 8004:
			return Requesttype.list;
		case 8005:
			return Requesttype.rename;
		case 8006:
			return Requesttype.hold;
		case 8007:
			return Requesttype.release;
		case 8008:
			return Requesttype.cancel;
		case 8009:
			return Requesttype.abort;
		case 8010:
			return Requesttype.delete;
		case 8011:
			return Requesttype.rewrite;
		case 8012:
			return Requesttype.diagnostics;

		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}