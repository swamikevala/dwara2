package org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.enumreferences.Storagesubtype;
import org.springframework.stereotype.Component;

@Component
@Converter(autoApply = true)
public class StoragesubtypeAttributeConverter implements AttributeConverter<Storagesubtype, String> {

	@Override
	public String convertToDatabaseColumn(Storagesubtype attribute) {
		if (attribute == null)
			return null;

		return attribute.getJavaStyleStoragesubtype();
	}

	@Override
	public Storagesubtype convertToEntityAttribute(String dbData) {
		if (dbData == null)
			return null;

		return Storagesubtype.getStoragesubtype(dbData);
	}

}