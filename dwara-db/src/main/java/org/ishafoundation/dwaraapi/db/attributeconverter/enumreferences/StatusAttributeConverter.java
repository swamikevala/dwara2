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
			return 1;
		case in_progress:
			return 2;
		case completed:
			return 3;
		case partially_completed:
			return 4;
		case completed_failures:
			return 5;
		case on_hold:
			return 6;
		case skipped:
			return 7;
		case cancelled:
			return 8;
		case aborted:
			return 9;
		case failed:
			return 10;
		case marked_completed:
			return 11;

		default:
			throw new IllegalArgumentException(attribute + " not supported.");
		}
	}

	@Override
	public Status convertToEntityAttribute(Integer dbData) {
		if (dbData == null)
			return null;

		switch (dbData) {
		case 1:
			return Status.queued;
		case 2:
			return Status.in_progress;
		case 3:
			return Status.completed;
		case 4:
			return Status.partially_completed;
		case 5:
			return Status.completed_failures;
		case 6:
			return Status.on_hold;
		case 7:
			return Status.skipped;
		case 8:
			return Status.cancelled;
		case 9:
			return Status.aborted;
		case 10:
			return Status.failed;
		case 11:
			return Status.marked_completed;

		default:
			throw new IllegalArgumentException(dbData + " not supported.");
		}
	}

}