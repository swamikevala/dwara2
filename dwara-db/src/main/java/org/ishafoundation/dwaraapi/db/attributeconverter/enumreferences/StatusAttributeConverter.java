package org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.enumreferences.Status;

@Converter(autoApply = true)
public class StatusAttributeConverter implements AttributeConverter<Status, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Status attribute) {
		if (attribute == null)
			return null;

		switch (attribute) {
		case queued:
			return 10001;
		case in_progress:
			return 10002;
		case completed:
			return 10003;
		case partially_completed:
			return 10004;
		case completed_failures:
			return 10005;
		case on_hold:
			return 10006;
		case skipped:
			return 10007;
		case cancelled:
			return 10008;
		case aborted:
			return 10009;
		case failed:
			return 10010;
		case marked_completed:
			return 10011;

		default:
			throw new IllegalArgumentException(attribute + " not supported.");
		}
	}

	@Override
	public Status convertToEntityAttribute(Integer dbData) {
		if (dbData == null)
			return null;

		switch (dbData) {
		case 10001:
			return Status.queued;
		case 10002:
			return Status.in_progress;
		case 10003:
			return Status.completed;
		case 10004:
			return Status.partially_completed;
		case 10005:
			return Status.completed_failures;
		case 10006:
			return Status.on_hold;
		case 10007:
			return Status.skipped;
		case 10008:
			return Status.cancelled;
		case 10009:
			return Status.aborted;
		case 10010:
			return Status.failed;
		case 10011:
			return Status.marked_completed;

		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}